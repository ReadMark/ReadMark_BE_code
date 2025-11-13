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
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH}, allowedHeaders = "*", allowCredentials = "false")
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
     * 도장을 수동으로 생성합니다.
     */
    @PostMapping("/earn")
    public ResponseEntity<?> earnStamp(@RequestParam Long userId, 
                                      @RequestParam String date, 
                                      @RequestParam Integer pagesRead) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LocalDate earnedDate = LocalDate.parse(date);
            StampDTO stamp = stampService.earnStamp(userId, earnedDate, pagesRead);
            
            if (stamp != null) {
                response.put("success", true);
                response.put("stamp", stamp);
                response.put("message", "도장이 성공적으로 생성되었습니다.");
                
                log.info("수동 도장 생성 완료: 사용자 {}, 날짜 {}, 페이지 수 {}", userId, date, pagesRead);
                
                return ResponseEntity.ok(response);
            } else {
                response.put("success", false);
                response.put("message", "도장 생성 실패: 이미 해당 날짜에 도장이 있거나 20페이지 미만입니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
        } catch (Exception e) {
            log.error("수동 도장 생성 중 오류 발생: 사용자 {}, 날짜 {}", userId, date, e);
            response.put("success", false);
            response.put("message", "도장 생성 실패: " + e.getMessage());
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
    
    /**
     * OPTIONS 요청 처리 - CORS Preflight
     */
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }

}
