package com.example.ReadMark.controller;

import com.example.ReadMark.model.dto.StampDTO;
import com.example.ReadMark.service.StampService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/stamps")
@RequiredArgsConstructor
@Slf4j
public class StampController {
    
    private final StampService stampService;
    
    /**
     * 사용자의 모든 도장 조회
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserStamps(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<StampDTO> stamps = stampService.getUserStamps(userId);
            long totalCount = stampService.getTotalStampCount(userId);
            
            response.put("success", true);
            response.put("stamps", stamps);
            response.put("totalCount", totalCount);
            response.put("message", "도장 목록 조회 성공");
            
            log.info("사용자 도장 목록 조회 완료: 사용자 {}, 총 {}개", userId, totalCount);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("도장 목록 조회 중 오류 발생: 사용자 {}", userId, e);
            response.put("success", false);
            response.put("message", "도장 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 사용자의 최근 N개 도장 조회
     */
    @GetMapping("/user/{userId}/recent")
    public ResponseEntity<?> getRecentStamps(@PathVariable Long userId, 
                                           @RequestParam(defaultValue = "10") int limit) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<StampDTO> stamps = stampService.getRecentStamps(userId, limit);
            
            response.put("success", true);
            response.put("stamps", stamps);
            response.put("count", stamps.size());
            response.put("message", "최근 도장 조회 성공");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("최근 도장 조회 중 오류 발생: 사용자 {}", userId, e);
            response.put("success", false);
            response.put("message", "최근 도장 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 특정 기간의 도장 조회
     */
    @GetMapping("/user/{userId}/period")
    public ResponseEntity<?> getStampsByPeriod(@PathVariable Long userId,
                                             @RequestParam String startDate,
                                             @RequestParam String endDate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            List<StampDTO> stamps = stampService.getStampsByDateRange(userId, start, end);
            
            response.put("success", true);
            response.put("stamps", stamps);
            response.put("count", stamps.size());
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("message", "기간별 도장 조회 성공");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("기간별 도장 조회 중 오류 발생: 사용자 {}, 기간 {} ~ {}", userId, startDate, endDate, e);
            response.put("success", false);
            response.put("message", "기간별 도장 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 사용자의 총 도장 개수 조회
     */
    @GetMapping("/user/{userId}/count")
    public ResponseEntity<?> getStampCount(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            long count = stampService.getTotalStampCount(userId);
            
            response.put("success", true);
            response.put("totalStamps", count);
            response.put("message", "도장 개수 조회 성공");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("도장 개수 조회 중 오류 발생: 사용자 {}", userId, e);
            response.put("success", false);
            response.put("message", "도장 개수 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
