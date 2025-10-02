package com.example.ReadMark.service;

import com.example.ReadMark.model.dto.MissionDTO;
import com.example.ReadMark.model.entity.Mission;
import com.example.ReadMark.model.entity.MissionCompletion;
import com.example.ReadMark.model.entity.User;
import com.example.ReadMark.repository.MissionCompletionRepository;
import com.example.ReadMark.repository.MissionRepository;
import com.example.ReadMark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class MissionService {
    
    private final MissionRepository missionRepository;
    private final MissionCompletionRepository missionCompletionRepository;
    private final UserRepository userRepository;
    private final ReadingLogService readingLogService;
    
    /**
     * 사용자의 오늘의 미션을 조회합니다.
     */
    public List<MissionDTO> getTodayMissions(Long userId) {
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 오늘 완료한 미션들 조회
        LocalDate today = LocalDate.now();
        List<MissionCompletion> completedMissions = missionCompletionRepository
                .findByUser_UserIdAndCompletedDate(userId, today);
        
        Set<Long> completedMissionIds = completedMissions.stream()
                .map(mc -> mc.getMission().getMissionId())
                .collect(Collectors.toSet());
        
        // 모든 미션 조회
        List<Mission> allMissions = missionRepository.findAll();
        
        // 오늘의 미션 생성 (완료 여부 포함)
        List<MissionDTO> todayMissions = new ArrayList<>();
        
        for (Mission mission : allMissions) {
            MissionDTO dto = convertToDTO(mission);
            dto.setCompleted(completedMissionIds.contains(mission.getMissionId()));
            dto.setCompletedAt(completedMissions.stream()
                    .filter(mc -> mc.getMission().getMissionId().equals(mission.getMissionId()))
                    .findFirst()
                    .map(MissionCompletion::getCompletedAt)
                    .orElse(null));
            
            todayMissions.add(dto);
        }
        
        return todayMissions;
    }
    
    /**
     * 미션을 완료합니다.
     */
    public void completeMission(Long userId, Long missionId) {
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 미션 존재 확인
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("미션을 찾을 수 없습니다."));
        
        // 오늘 이미 완료했는지 확인
        LocalDate today = LocalDate.now();
        boolean alreadyCompleted = missionCompletionRepository
                .existsByUser_UserIdAndMission_MissionIdAndCompletedDate(userId, missionId, today);
        
        if (alreadyCompleted) {
            throw new RuntimeException("이미 완료한 미션입니다.");
        }
        
        // 미션 완료 기록 생성
        MissionCompletion completion = new MissionCompletion();
        completion.setUser(user);
        completion.setMission(mission);
        completion.setCompletedDate(today);
        completion.setCompletedAt(LocalDateTime.now());
        
        missionCompletionRepository.save(completion);
        
        log.info("미션 완료: 사용자 {}, 미션 {}", userId, missionId);
    }
    
    /**
     * 사용자의 미션 진행 현황을 조회합니다.
     */
    public Map<String, Object> getMissionProgress(Long userId) {
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        LocalDate today = LocalDate.now();
        
        // 오늘 완료한 미션 수
        long todayCompletedCount = missionCompletionRepository
                .countByUser_UserIdAndCompletedDate(userId, today);
        
        // 전체 미션 수
        long totalMissionCount = missionRepository.count();
        
        // 이번 주 완료한 미션 수
        LocalDate weekStart = today.minusDays(today.getDayOfWeek().getValue() - 1);
        long weekCompletedCount = missionCompletionRepository
                .countByUser_UserIdAndCompletedDateBetween(userId, weekStart, today);
        
        // 이번 달 완료한 미션 수
        LocalDate monthStart = today.withDayOfMonth(1);
        long monthCompletedCount = missionCompletionRepository
                .countByUser_UserIdAndCompletedDateBetween(userId, monthStart, today);
        
        // 연속 완료 일수
        long consecutiveDays = calculateConsecutiveCompletionDays(userId);
        
        Map<String, Object> progress = new HashMap<>();
        progress.put("todayCompleted", todayCompletedCount);
        progress.put("totalMissions", totalMissionCount);
        progress.put("weekCompleted", weekCompletedCount);
        progress.put("monthCompleted", monthCompletedCount);
        progress.put("consecutiveDays", consecutiveDays);
        progress.put("completionRate", totalMissionCount > 0 ? (double) todayCompletedCount / totalMissionCount * 100 : 0);
        
        return progress;
    }
    
    /**
     * 모든 미션 목록을 조회합니다.
     */
    public List<MissionDTO> getAllMissions() {
        List<Mission> missions = missionRepository.findAll();
        return missions.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    /**
     * 연속 완료 일수를 계산합니다.
     */
    private long calculateConsecutiveCompletionDays(Long userId) {
        LocalDate today = LocalDate.now();
        long consecutiveDays = 0;
        
        for (int i = 0; i < 365; i++) { // 최대 1년까지 확인
            LocalDate checkDate = today.minusDays(i);
            long completedCount = missionCompletionRepository
                    .countByUser_UserIdAndCompletedDate(userId, checkDate);
            
            if (completedCount > 0) {
                consecutiveDays++;
            } else {
                break;
            }
        }
        
        return consecutiveDays;
    }
    
    /**
     * Mission을 MissionDTO로 변환합니다.
     */
    private MissionDTO convertToDTO(Mission mission) {
        MissionDTO dto = new MissionDTO();
        dto.setMissionId(mission.getMissionId());
        dto.setTitle(mission.getTitle());
        dto.setDescription(mission.getDescription());
        dto.setType(mission.getType());
        dto.setTargetValue(mission.getTargetValue());
        dto.setUnit(mission.getUnit());
        dto.setReward(mission.getReward());
        dto.setCreatedAt(mission.getCreatedAt());
        return dto;
    }
}
