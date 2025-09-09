package com.example.ReadMark.controller;

import com.example.ReadMark.service.ReadingSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/esp32")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ESP32SessionController {
    
    private final ReadingSessionService readingSessionService;
    
    /**
     * ESP32용 독서 세션 시작 (기본값: userId=1, bookId=1)
     */
    @PostMapping("/session/start")
    public ResponseEntity<?> startSession() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // ESP32용 기본값 사용
            Long userId = 1L;
            Long bookId = 1L;
            
            var session = readingSessionService.startReadingSession(userId, bookId);
            
            // 독서 기간 정보 계산
            Map<String, Object> readingPeriod = calculateReadingPeriod(userId);
            
            response.put("success", true);
            response.put("message", "독서 세션이 시작되었습니다.");
            response.put("sessionId", session.getSessionId());
            response.put("startTime", session.getStartTime());
            response.put("userId", userId);
            response.put("bookId", bookId);
            
            // 계산된 날짜 정보 추가
            response.put("date", readingPeriod.get("date"));
            response.put("readingPeriod", readingPeriod.get("period"));
            response.put("currentConsecutiveDays", readingPeriod.get("consecutiveDays"));
            
            log.info("ESP32 독서 세션 시작: 사용자 {}, 책 {}", userId, bookId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("ESP32 독서 세션 시작 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "세션 시작 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * ESP32용 독서 세션 종료 (기본값: userId=1)
     */
    @PostMapping("/session/end")
    public ResponseEntity<?> endSession() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // ESP32용 기본값 사용
            Long userId = 1L;
            
            var session = readingSessionService.endReadingSession(userId);
            
            if (session != null) {
                // 독서 기간 정보 계산
                Map<String, Object> readingPeriod = calculateReadingPeriod(userId);
                
                response.put("success", true);
                response.put("message", "독서 세션이 종료되었습니다.");
                response.put("totalPagesRead", session.getTotalPagesRead());
                response.put("totalNumbersRead", session.getTotalNumbersRead());
                response.put("readingDurationMinutes", session.getReadingDurationMinutes());
                response.put("endTime", session.getEndTime());
                response.put("userId", userId);
                
                // 계산된 날짜 정보 추가
                response.put("date", readingPeriod.get("date"));
                response.put("readingPeriod", readingPeriod.get("period"));
                response.put("currentConsecutiveDays", readingPeriod.get("consecutiveDays"));
            } else {
                response.put("success", false);
                response.put("message", "활성 독서 세션이 없습니다.");
                response.put("userId", userId);
            }
            
            log.info("ESP32 독서 세션 종료: 사용자 {}", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("ESP32 독서 세션 종료 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "세션 종료 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * ESP32용 간단한 통계 조회 (기본값: userId=1)
     */
    @GetMapping("/stats")
    public ResponseEntity<?> getStats() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // ESP32용 기본값 사용
            Long userId = 1L;
            
            Map<String, Object> stats = new HashMap<>();
            stats.put("maxConsecutiveDays", readingSessionService.getMaxConsecutiveReadingDays(userId));
            stats.put("totalReadingDays", readingSessionService.getTotalReadingDays(userId));
            stats.put("currentConsecutiveDays", readingSessionService.getCurrentConsecutiveDays(userId));
            
            response.put("success", true);
            response.put("stats", stats);
            response.put("userId", userId);
            
            log.info("ESP32 통계 조회: 사용자 {}", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("ESP32 통계 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "통계 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 독서 기간을 계산합니다.
     * ESP32에서 사용할 간단한 값과 전체 기간 정보를 반환합니다.
     */
    private Map<String, Object> calculateReadingPeriod(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 현재 연속 독서일 계산
            int consecutiveDays = readingSessionService.getCurrentConsecutiveDays(userId);
            
            // 독서 통계 가져오기
            Map<String, Object> readingStats = readingSessionService.getReadingHabitAnalysis(userId);
            
            // 현재 날짜
            LocalDate today = LocalDate.now();
            
            // 독서 시작일 계산 (연속 독서일 기준)
            LocalDate readingStartDate = today.minusDays(consecutiveDays - 1);
            
            // 날짜 형식 포맷터
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            
            // 기간 문자열 생성 (예: "2023.01.02 - 2023.01.05")
            String periodString = readingStartDate.format(formatter) + " - " + today.format(formatter);
            
            // ESP32에서 사용할 간단한 값 (연속 독서일 수)
            result.put("date", consecutiveDays);
            result.put("period", periodString);
            result.put("consecutiveDays", consecutiveDays);
            result.put("startDate", readingStartDate.format(formatter));
            result.put("endDate", today.format(formatter));
            result.put("totalReadingDays", readingStats.get("totalReadingDays"));
            
            log.info("독서 기간 계산 완료: 사용자 {}, 연속 {}일, 기간: {}", 
                    userId, consecutiveDays, periodString);
            
        } catch (Exception e) {
            log.error("독서 기간 계산 중 오류 발생", e);
            // 오류 시 기본값 설정
            result.put("date", 1);
            result.put("period", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
            result.put("consecutiveDays", 1);
            result.put("startDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
            result.put("endDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
            result.put("totalReadingDays", 1);
        }
        
        return result;
    }
}


