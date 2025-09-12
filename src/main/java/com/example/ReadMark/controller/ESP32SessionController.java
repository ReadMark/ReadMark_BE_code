package com.example.ReadMark.controller;

import com.example.ReadMark.service.ReadingPeriodService;
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
    private final ReadingPeriodService readingPeriodService;
    
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
            Map<String, Object> readingPeriod = readingPeriodService.calculateReadingPeriod(userId);
            
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
                Map<String, Object> readingPeriod = readingPeriodService.calculateReadingPeriod(userId);
                
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
    
}


