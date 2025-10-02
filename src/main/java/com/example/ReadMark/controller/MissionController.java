package com.example.ReadMark.controller;

import com.example.ReadMark.model.dto.MissionDTO;
import com.example.ReadMark.service.MissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/missions")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
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
     * 미션을 완료합니다.
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
}
