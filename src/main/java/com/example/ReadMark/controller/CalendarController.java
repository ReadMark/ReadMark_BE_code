package com.example.ReadMark.controller;

import com.example.ReadMark.model.dto.CalendarMonthDTO;
import com.example.ReadMark.model.dto.StampDTO;
import com.example.ReadMark.service.CalendarService;
import com.example.ReadMark.service.StampService;
import com.example.ReadMark.service.MissionService;
import com.example.ReadMark.service.ReadingLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH}, allowedHeaders = "*", allowCredentials = "false")
public class CalendarController {
    
    private final CalendarService calendarService;
    private final StampService stampService;
    private final MissionService missionService;
    private final ReadingLogService readingLogService;
    
    /**
     * 캘린더 통계 요약을 조회합니다.
     */
    @GetMapping("/{userId}/stats")
    public ResponseEntity<?> getCalendarStats(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LocalDate now = LocalDate.now();
            CalendarMonthDTO currentMonth = calendarService.getCalendarMonth(userId, now.getYear(), now.getMonthValue());
            
            // 이번 달 통계
            Map<String, Object> currentMonthStats = new HashMap<>();
            currentMonthStats.put("readingRate", currentMonth.getSummary().get("readingRate"));
            
            // 전체 통계 조회 (최대 연속 독서일, 총 읽은 날 수)
            Map<String, Object> totalStats = calendarService.getTotalReadingStats(userId);
            currentMonthStats.put("maxConsecutiveDays", totalStats.get("maxConsecutiveDays"));
            currentMonthStats.put("totalReadingDays", totalStats.get("totalReadingDays"));
            
            // 오늘 읽은 페이지 수
            Map<String, Object> todayStats = calendarService.getTodayReadingStats(userId, now);
            currentMonthStats.put("todayPagesRead", todayStats.get("todayPagesRead"));
            
            // 도장 관련 정보
            long totalStamps = stampService.getTotalStampCount(userId);
            currentMonthStats.put("totalStamps", totalStamps);
            
            // 모든 도장 받은 날짜 정보 (페이지 수 제거)
            List<StampDTO> allStamps = stampService.getUserStamps(userId);
            currentMonthStats.put("stampDates", allStamps.stream()
                .map(stamp -> stamp.getFormattedDate())
                .collect(java.util.stream.Collectors.toList()));
            
            // 현재 연속으로 안 읽은 날 수 계산
            int consecutiveNonReadingDays = calculateConsecutiveNonReadingDays(userId, now);
            currentMonthStats.put("consecutiveNonReadingDays", consecutiveNonReadingDays);
            
            response.put("success", true);
            response.put("currentMonth", currentMonthStats);
            response.put("message", "캘린더 통계 조회 성공");
            
            log.info("캘린더 통계 조회 완료: 사용자 {}", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("캘린더 통계 조회 중 오류 발생: 사용자 {}", userId, e);
            response.put("success", false);
            response.put("message", "캘린더 통계 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 총 독서 시간을 분 단위로 조회합니다.
     */
    @GetMapping("/{userId}/total-time")
    public ResponseEntity<?> getTotalReadingTime(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long minutes = calendarService.getTotalReadingMinutes(userId);
            response.put("success", true);
            response.put("totalMinutes", minutes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "총 독서 시간 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 특정 책의 총 독서 시간을 분 단위로 조회합니다.
     */
    @GetMapping("/{userId}/book/{bookId}/total-time")
    public ResponseEntity<?> getTotalReadingTimeByBook(@PathVariable Long userId, @PathVariable Long bookId) {
        Map<String, Object> response = new HashMap<>();
        try {
            Long minutes = calendarService.getTotalReadingMinutesByBook(userId, bookId);
            response.put("success", true);
            response.put("totalMinutes", minutes);
            response.put("bookId", bookId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "총 독서 시간(책별) 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 오늘의 총 독서 시간을 분 단위로 조회합니다.
     */
    @GetMapping("/{userId}/today-time")
    public ResponseEntity<?> getTodayReadingTime(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            LocalDate now = LocalDate.now();
            Long minutes = calendarService.getTodayReadingMinutes(userId, now);
            response.put("success", true);
            response.put("todayMinutes", minutes);
            response.put("date", now);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "오늘 총 독서 시간 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 현재 연속으로 안 읽은 날 수를 계산합니다.
     */
    private int calculateConsecutiveNonReadingDays(Long userId, LocalDate today) {
        try {
            // 오늘부터 역산하여 연속으로 독서하지 않은 날 수 계산
            int consecutiveNonReadingDays = 0;
            LocalDate checkDate = today;
            
            while (checkDate.isAfter(LocalDate.of(2020, 1, 1))) { // 너무 오래 전까지는 확인하지 않음
                // 해당 날짜에 독서 기록이 있는지 확인
                boolean hasReadingOnDate = calendarService.hasReadingOnDate(userId, checkDate);
                
                if (hasReadingOnDate) {
                    break; // 독서 기록이 있으면 중단
                }
                
                consecutiveNonReadingDays++;
                checkDate = checkDate.minusDays(1);
            }
            
            return consecutiveNonReadingDays;
            
        } catch (Exception e) {
            log.error("연속 비독서일 계산 중 오류 발생: 사용자 {}", userId, e);
            return 0;
        }
    }
    
    /**
     * 현재 연속으로 읽은 날 수를 조회합니다. (메인페이지용)
     * 하루 안 읽으면 기록이 깨지는 연속 독서일
     */
    @GetMapping("/{userId}/current-consecutive")
    public ResponseEntity<?> getCurrentConsecutiveDays(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LocalDate now = LocalDate.now();
            CalendarMonthDTO currentMonth = calendarService.getCalendarMonth(userId, now.getYear(), now.getMonthValue());
            
            int currentConsecutiveDays = currentMonth.getCurrentConsecutiveDays();
            
            response.put("success", true);
            response.put("currentConsecutiveDays", currentConsecutiveDays);
            response.put("message", "현재 연속 독서일 조회 성공");
            
            log.info("현재 연속 독서일 조회 완료: 사용자 {}, 연속일수 {}", userId, currentConsecutiveDays);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("현재 연속 독서일 조회 중 오류 발생: 사용자 {}", userId, e);
            response.put("success", false);
            response.put("message", "현재 연속 독서일 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 특정 날짜의 종합 정보를 조회합니다 (미션 성공여부 + 읽은 시간 + 읽은 페이지)
     */
    @GetMapping("/{userId}/daily-summary/{date}")
    public ResponseEntity<?> getDailySummary(@PathVariable Long userId, @PathVariable String date) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LocalDate targetDate = LocalDate.parse(date);
            
            // 1. 미션 성공여부 조회
            Map<String, Object> missionData = new HashMap<>();
            try {
                Map<String, Object> missionRecords = missionService.getMissionRecordsByDate(userId, targetDate);
                missionData.put("hasMissionData", true);
                missionData.put("completedMissions", missionRecords.get("completedCount"));
                missionData.put("failedMissions", missionRecords.get("failedCount"));
                missionData.put("totalMissions", missionRecords.get("totalMissions"));
                
                Integer totalMissions = (Integer) missionRecords.get("totalMissions");
                Integer completedMissions = (Integer) missionRecords.get("completedCount");
                Double successRate = totalMissions > 0 ? (double) completedMissions / totalMissions * 100.0 : 0.0;
                missionData.put("missionSuccessRate", successRate);
                
                missionData.put("missionStatusList", missionRecords.get("missionStatusList"));
            } catch (Exception e) {
                missionData.put("hasMissionData", false);
                missionData.put("error", "미션 데이터 조회 실패: " + e.getMessage());
                missionData.put("completedMissions", 0);
                missionData.put("failedMissions", 0);
                missionData.put("totalMissions", 0);
                missionData.put("missionSuccessRate", 0.0);
            }
            
            // 2. 읽은 시간 조회
            Long readingMinutes = calendarService.getTodayReadingMinutes(userId, targetDate);
            
            // 3. 읽은 페이지 수 조회
            Integer pagesRead = 0;
            try {
                // 특정 날짜의 페이지 수 조회를 위해 ReadingLogRepository 직접 사용
                pagesRead = readingLogService.getReadingLogsByDateRange(userId, targetDate, targetDate)
                    .stream()
                    .mapToInt(log -> log.getPagesRead())
                    .sum();
            } catch (Exception e) {
                log.warn("페이지 수 조회 실패: {}", e.getMessage());
                pagesRead = 0;
            }
            
            // 4. 독서 세션 정보 조회
            Map<String, Object> sessionData = new HashMap<>();
            try {
                // ReadingSessionService를 통해 세션 정보 조회
                sessionData.put("hasSessionData", true);
                sessionData.put("sessionCount", 0);
                sessionData.put("totalMinutes", readingMinutes != null ? readingMinutes : 0);
            } catch (Exception e) {
                sessionData.put("hasSessionData", false);
                sessionData.put("error", "세션 데이터 조회 실패: " + e.getMessage());
            }
            
            // 응답 구성
            Map<String, Object> dailySummary = new HashMap<>();
            dailySummary.put("date", targetDate.toString());
            dailySummary.put("readingMinutes", readingMinutes != null ? readingMinutes : 0);
            dailySummary.put("pagesRead", pagesRead);
            dailySummary.put("missionData", missionData);
            dailySummary.put("sessionData", sessionData);
            dailySummary.put("hasReading", (readingMinutes != null && readingMinutes > 0) || pagesRead > 0);
            
            response.put("success", true);
            response.put("dailySummary", dailySummary);
            response.put("message", "일일 종합 정보 조회 성공");
            
            log.info("일일 종합 정보 조회 완료: 사용자 {}, 날짜 {}", userId, date);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("일일 종합 정보 조회 중 오류 발생: 사용자 {}, 날짜 {}", userId, date, e);
            response.put("success", false);
            response.put("message", "일일 종합 정보 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 사용자의 모든 도장 받은 날짜 목록을 조회합니다.
     */
    @GetMapping("/{userId}/stamp-dates")
    public ResponseEntity<?> getStampDates(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 모든 도장 조회
            List<StampDTO> allStamps = stampService.getUserStamps(userId);
            
            // 날짜만 추출하여 반환
            List<String> stampDates = allStamps.stream()
                .map(StampDTO::getFormattedDate)
                .collect(java.util.stream.Collectors.toList());
            
            response.put("success", true);
            response.put("stampDates", stampDates);
            response.put("totalCount", allStamps.size());
            response.put("message", "도장 받은 날짜 목록 조회 성공");
            
            log.info("도장 받은 날짜 목록 조회 완료: 사용자 {}, 총 {}개", userId, allStamps.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("도장 받은 날짜 목록 조회 중 오류 발생: 사용자 {}", userId, e);
            response.put("success", false);
            response.put("message", "도장 받은 날짜 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 특정 날짜의 도장 정보를 조회합니다.
     */
    @GetMapping("/{userId}/stamp/{date}")
    public ResponseEntity<?> getStampForDate(@PathVariable Long userId, @PathVariable String date) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LocalDate targetDate = LocalDate.parse(date);
            
            // 해당 날짜에 도장이 있는지 확인
            boolean hasStamp = stampService.hasStampForDate(userId, targetDate);
            
            Map<String, Object> stampInfo = new HashMap<>();
            stampInfo.put("date", targetDate.toString());
            stampInfo.put("hasStamp", hasStamp);
            
            if (hasStamp) {
                // 해당 날짜의 도장 상세 정보 조회
                List<StampDTO> stampsForDate = stampService.getStampsByDateRange(userId, targetDate, targetDate);
                if (!stampsForDate.isEmpty()) {
                    StampDTO stamp = stampsForDate.get(0);
                    stampInfo.put("stampId", stamp.getStampId());
                    stampInfo.put("pagesRead", stamp.getPagesRead());
                    stampInfo.put("description", stamp.getDescription());
                    stampInfo.put("earnedDate", stamp.getEarnedDate());
                }
            }
            
            response.put("success", true);
            response.put("stampInfo", stampInfo);
            response.put("message", "도장 정보 조회 성공");
            
            log.info("도장 정보 조회 완료: 사용자 {}, 날짜 {}, 도장여부 {}", userId, date, hasStamp);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("도장 정보 조회 중 오류 발생: 사용자 {}, 날짜 {}", userId, date, e);
            response.put("success", false);
            response.put("message", "도장 정보 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * OPTIONS 요청 처리 - CORS Preflight
     */
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }
}
