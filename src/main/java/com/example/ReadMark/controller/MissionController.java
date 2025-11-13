package com.example.ReadMark.controller;

import com.example.ReadMark.model.dto.MissionDTO;
import com.example.ReadMark.model.entity.MissionFailure;
import com.example.ReadMark.model.entity.MissionCompletion;
import com.example.ReadMark.service.MissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH}, allowedHeaders = "*", allowCredentials = "false")
public class MissionController {
    
    private final MissionService missionService;
    
    /**
     * 사용자의 오늘의 미션을 조회합니다.
     */
    @GetMapping("/user/{userId}/today")
    public ResponseEntity<?> getTodayMissions(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<MissionDTO> missions = missionService.getTodayMissions(userId);
            
            response.put("success", true);
            response.put("missions", missions);
            response.put("count", missions.size());
            response.put("message", "오늘의 미션 조회 성공");
            
            log.info("오늘의 미션 조회 완료: 사용자 {}, 미션 수 {}", userId, missions.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("오늘의 미션 조회 중 오류 발생: 사용자 {}", userId, e);
            response.put("success", false);
            response.put("message", "오늘의 미션 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 미션을 완료합니다. (POST)
     */
    @PostMapping("/{missionId}/complete")
    public ResponseEntity<?> completeMission(@PathVariable Long missionId, 
                                           @RequestParam Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            missionService.completeMission(userId, missionId);
            
            response.put("success", true);
            response.put("message", "미션이 완료되었습니다.");
            response.put("completedMissionId", missionId);
            
            log.info("미션 완료: 사용자 {}, 미션 {}", userId, missionId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("미션 완료 중 오류 발생: 사용자 {}, 미션 {}", userId, missionId, e);
            response.put("success", false);
            response.put("message", "미션 완료 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 사용자의 미션 완료 현황을 조회합니다.
     */
    @GetMapping("/user/{userId}/progress")
    public ResponseEntity<?> getMissionProgress(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> progress = missionService.getMissionProgress(userId);
            
            response.put("success", true);
            response.put("progress", progress);
            response.put("message", "미션 진행 현황 조회 성공");
            
            log.info("미션 진행 현황 조회 완료: 사용자 {}", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("미션 진행 현황 조회 중 오류 발생: 사용자 {}", userId, e);
            response.put("success", false);
            response.put("message", "미션 진행 현황 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 모든 미션 목록을 조회합니다.
     */
    @GetMapping
    public ResponseEntity<?> getAllMissions() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<MissionDTO> missions = missionService.getAllMissions();
            
            response.put("success", true);
            response.put("missions", missions);
            response.put("count", missions.size());
            response.put("message", "미션 목록 조회 성공");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("미션 목록 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "미션 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 미션을 완료합니다. (GET)
     */
    @GetMapping("/{missionId}/complete")
    public ResponseEntity<?> completeMissionGet(@PathVariable Long missionId, 
                                              @RequestParam Long userId) {
        // POST 메서드와 동일한 로직 사용
        return completeMission(missionId, userId);
    }
    
    /**
     * 미션 실패 기록을 저장합니다.
     */
    @PostMapping("/{missionId}/fail")
    public ResponseEntity<?> recordMissionFailure(@PathVariable Long missionId,
                                                @RequestParam Long userId,
                                                @RequestParam(required = false) String failureReason) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            missionService.recordMissionFailure(userId, missionId, failureReason);
            
            response.put("success", true);
            response.put("message", "미션 실패 기록이 저장되었습니다.");
            response.put("failedMissionId", missionId);
            
            log.info("미션 실패 기록 저장: 사용자 {}, 미션 {}", userId, missionId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("미션 실패 기록 저장 중 오류 발생: 사용자 {}, 미션 {}", userId, missionId, e);
            response.put("success", false);
            response.put("message", "미션 실패 기록 저장 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 사용자의 미션 실패 기록을 조회합니다.
     */
    @GetMapping("/user/{userId}/failures")
    public ResponseEntity<?> getUserMissionFailures(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            List<MissionFailure> failures = missionService.getUserMissionFailures(userId);
            
            response.put("success", true);
            response.put("failures", failures);
            response.put("count", failures.size());
            response.put("message", "미션 실패 기록 조회 성공");
            
            log.info("미션 실패 기록 조회 완료: 사용자 {}, 실패 수 {}", userId, failures.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("미션 실패 기록 조회 중 오류 발생: 사용자 {}", userId, e);
            response.put("success", false);
            response.put("message", "미션 실패 기록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 사용자의 특정 기간 미션 실패 기록을 조회합니다.
     */
    @GetMapping("/user/{userId}/failures/date-range")
    public ResponseEntity<?> getUserMissionFailuresByDateRange(@PathVariable Long userId,
                                                               @RequestParam String startDate,
                                                               @RequestParam String endDate) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LocalDate start = LocalDate.parse(startDate);
            LocalDate end = LocalDate.parse(endDate);
            
            List<MissionFailure> failures = missionService.getUserMissionFailuresByDateRange(userId, start, end);
            
            response.put("success", true);
            response.put("failures", failures);
            response.put("count", failures.size());
            response.put("startDate", startDate);
            response.put("endDate", endDate);
            response.put("message", "기간별 미션 실패 기록 조회 성공");
            
            log.info("기간별 미션 실패 기록 조회 완료: 사용자 {}, 기간 {} ~ {}, 실패 수 {}", userId, startDate, endDate, failures.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("기간별 미션 실패 기록 조회 중 오류 발생: 사용자 {}, 기간 {} ~ {}", userId, startDate, endDate, e);
            response.put("success", false);
            response.put("message", "기간별 미션 실패 기록 조회 실패: " + e.getMessage());
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
     * 특정 날짜의 미션 기록을 조회합니다 (완료 + 실패).
     */
    @GetMapping("/user/{userId}/date/{date}")
    public ResponseEntity<?> getMissionRecordsByDate(@PathVariable Long userId,
                                                   @PathVariable String date) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LocalDate targetDate = LocalDate.parse(date);
            Map<String, Object> records = missionService.getMissionRecordsByDate(userId, targetDate);
            
            response.put("success", true);
            response.put("data", records);
            response.put("message", "날짜별 미션 기록 조회 성공");
            
            log.info("날짜별 미션 기록 조회 완료: 사용자 {}, 날짜 {}", userId, date);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("날짜별 미션 기록 조회 중 오류 발생: 사용자 {}, 날짜 {}", userId, date, e);
            response.put("success", false);
            response.put("message", "날짜별 미션 기록 조회 실패: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * CORS OPTIONS 요청 처리
     */
    @RequestMapping(value = "/**", method = RequestMethod.OPTIONS)
    public ResponseEntity<?> handleOptions() {
        return ResponseEntity.ok().build();
    }
}
