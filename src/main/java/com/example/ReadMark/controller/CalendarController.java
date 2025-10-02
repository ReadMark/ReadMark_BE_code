package com.example.ReadMark.controller;

import com.example.ReadMark.model.dto.CalendarMonthDTO;
import com.example.ReadMark.model.dto.StampDTO;
import com.example.ReadMark.service.CalendarService;
import com.example.ReadMark.service.StampService;
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
@CrossOrigin(origins = "*")
public class CalendarController {
    
    private final CalendarService calendarService;
    private final StampService stampService;
    
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
}
