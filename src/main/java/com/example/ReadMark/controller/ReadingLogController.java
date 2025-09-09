package com.example.ReadMark.controller;

import com.example.ReadMark.model.dto.DailyReadingDTO;
import com.example.ReadMark.model.dto.ReadingLogDTO;
import com.example.ReadMark.model.entity.ReadingLog;
import com.example.ReadMark.service.ReadingLogService;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/readinglogs")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // CORS 설정
public class ReadingLogController {
    
    private final ReadingLogService readingLogService;
    
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
}


