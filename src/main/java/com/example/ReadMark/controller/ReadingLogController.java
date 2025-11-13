package com.example.ReadMark.controller;

import com.example.ReadMark.model.dto.DailyReadingDTO;
import com.example.ReadMark.model.dto.ReadingLogDTO;
import com.example.ReadMark.model.entity.ReadingLog;
import com.example.ReadMark.service.CalendarService;
import com.example.ReadMark.service.ReadingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/readinglogs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH}, allowedHeaders = "*", allowCredentials = "false")
public class ReadingLogController {
    
    private final ReadingLogService readingLogService;
    private final CalendarService calendarService;
    
    @PostMapping
    public ResponseEntity<?> createReadingLog(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            LocalDate readDate = LocalDate.parse(request.get("readDate").toString());
            int pagesRead = Integer.valueOf(request.get("pagesRead").toString());
            
            ReadingLog readingLog = readingLogService.createReadingLog(userId, readDate, pagesRead);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "독서 기록이 저장되었습니다.");
            response.put("logId", readingLog.getLogId());
            response.put("pagesRead", readingLog.getPagesRead());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "독서 기록 저장에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getReadingLogsByDateRange(@PathVariable Long userId,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                     @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<ReadingLogDTO> readingLogs = readingLogService.getReadingLogsByDateRange(userId, startDate, endDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("readingLogs", readingLogs);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "독서 기록 조회에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/user/{userId}/today")
    public ResponseEntity<?> getTodayPagesRead(@PathVariable Long userId) {
        try {
            Integer pagesRead = readingLogService.getTodayPagesRead(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("pagesRead", pagesRead != null ? pagesRead : 0);
            response.put("date", LocalDate.now());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "오늘 독서 페이지 수 조회에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<?> getReadingStats(@PathVariable Long userId) {
        try {
            Integer maxConsecutiveDays = readingLogService.getMaxConsecutiveReadingDays(userId);
            Integer totalReadingDays = readingLogService.getTotalReadingDays(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("maxConsecutiveDays", maxConsecutiveDays != null ? maxConsecutiveDays : 0);
            response.put("totalReadingDays", totalReadingDays != null ? totalReadingDays : 0);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "독서 통계 조회에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/user/{userId}/daily")
    public ResponseEntity<?> getDailyReadingStats(@PathVariable Long userId,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        try {
            List<DailyReadingDTO> dailyStats = readingLogService.getDailyReadingStats(userId, startDate, endDate);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("dailyStats", dailyStats);
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "일일 독서 통계 조회에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 특정 날짜의 독서 기록을 조회합니다.
     */
    @GetMapping("/user/{userId}/date/{date}")
    public ResponseEntity<?> getReadingLogByDate(@PathVariable Long userId,
                                                @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            // 해당 날짜의 독서 로그 조회
            List<ReadingLogDTO> readingLogs = readingLogService.getReadingLogsByDateRange(userId, date, date);
            
            // 해당 날짜의 독서 통계 조회
            List<DailyReadingDTO> dailyStats = readingLogService.getDailyReadingStats(userId, date, date);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("date", date.toString());
            response.put("readingLogs", readingLogs);
            response.put("dailyStats", dailyStats);
            response.put("count", readingLogs.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "특정 날짜 독서 기록 조회에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 특정 날짜의 읽은 시간(분)을 조회합니다.
     */
    @GetMapping("/user/{userId}/minutes/date/{date}")
    public ResponseEntity<?> getReadingMinutesByDate(@PathVariable Long userId,
                                                    @PathVariable @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        try {
            // CalendarService를 통해 해당 날짜의 독서 시간 조회 (분 단위)
            // CalendarService의 getTodayReadingMinutes 메서드를 활용
            Long readingMinutes = calendarService.getTodayReadingMinutes(userId, date);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("date", date.toString());
            response.put("readingMinutes", readingMinutes != null ? readingMinutes : 0);
            response.put("message", "특정 날짜 읽은 시간 조회 성공");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "특정 날짜 읽은 시간 조회에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * CORS OPTIONS 요청 처리
     */
    @RequestMapping(value = "/user/{userId}", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions(@PathVariable Long userId) {
        return ResponseEntity.ok().build();
    }
    
    /**
     * CORS OPTIONS 요청 처리 (일반 경로)
     */
    @RequestMapping(value = "/user", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptionsGeneral() {
        return ResponseEntity.ok().build();
    }
    
    /**
     * CORS OPTIONS 요청 처리 (루트 경로)
     */
    @RequestMapping(value = "", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptionsRoot() {
        return ResponseEntity.ok().build();
    }
    
    /**
     * CORS OPTIONS 요청 처리 (날짜 경로)
     */
    @RequestMapping(value = "/user/{userId}/date/{date}", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptionsDate(@PathVariable Long userId, @PathVariable String date) {
        return ResponseEntity.ok().build();
    }
    
    /**
     * CORS OPTIONS 요청 처리 (모든 경로)
     */
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptionsAll() {
        return ResponseEntity.ok().build();
    }
    
    /**
     * ReadingLogController 내의 지원되지 않는 GET 요청을 처리하는 매핑
     */
    @RequestMapping(value = "/readinglogs/**", method = RequestMethod.GET)
    public ResponseEntity<?> handleUnsupportedReadingLogGetRequests(HttpServletRequest request) {
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", "지원되지 않는 ReadingLog GET 요청: " + request.getRequestURI());
        response.put("availableEndpoints", Arrays.asList(
            "GET /api/readinglogs/user/{userId}?startDate={startDate}&endDate={endDate}",
            "GET /api/readinglogs/user/{userId}/today",
            "GET /api/readinglogs/user/{userId}/stats",
            "GET /api/readinglogs/user/{userId}/daily?startDate={startDate}&endDate={endDate}",
            "GET /api/readinglogs/user/{userId}/date/{date}"
        ));
        return ResponseEntity.badRequest().body(response);
    }
    
    /**
     * 누락된 도장들을 자동으로 생성합니다.
     */
    @PostMapping("/user/{userId}/generate-missing-stamps")
    public ResponseEntity<?> generateMissingStamps(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            readingLogService.generateMissingStamps(userId);
            
            response.put("success", true);
            response.put("message", "누락된 도장 생성이 완료되었습니다.");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "누락된 도장 생성 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}


