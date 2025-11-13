package com.example.ReadMark.service;

import com.example.ReadMark.model.dto.MissionDTO;
import com.example.ReadMark.model.entity.Mission;
import com.example.ReadMark.model.entity.MissionCompletion;
import com.example.ReadMark.model.entity.MissionFailure;
import com.example.ReadMark.model.entity.User;
import com.example.ReadMark.repository.MissionCompletionRepository;
import com.example.ReadMark.repository.MissionFailureRepository;
import com.example.ReadMark.repository.MissionRepository;
import com.example.ReadMark.repository.UserRepository;
import com.example.ReadMark.service.StampService;
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
    private final MissionFailureRepository missionFailureRepository;
    private final UserRepository userRepository;
    private final ReadingLogService readingLogService;
    private final StampService stampService;
    
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
        
        // 오늘 실패한 미션들 조회
        List<MissionFailure> failedMissions = missionFailureRepository
                .findByUser_UserIdAndFailureDate(userId, today);
        
        Set<Long> completedMissionIds = completedMissions.stream()
                .map(mc -> mc.getMission().getMissionId())
                .collect(Collectors.toSet());
        
        Set<Long> failedMissionIds = failedMissions.stream()
                .map(mf -> mf.getMission().getMissionId())
                .collect(Collectors.toSet());
        
        // 모든 미션 조회
        List<Mission> allMissions = missionRepository.findAll();
        
        // 오늘의 미션 생성 (완료 여부 포함)
        List<MissionDTO> todayMissions = new ArrayList<>();
        
        for (Mission mission : allMissions) {
            MissionDTO dto = convertToDTO(mission);
            dto.setCompleted(completedMissionIds.contains(mission.getMissionId()));
            dto.setFailed(failedMissionIds.contains(mission.getMissionId()));
            dto.setCompletedAt(completedMissions.stream()
                    .filter(mc -> mc.getMission().getMissionId().equals(mission.getMissionId()))
                    .findFirst()
                    .map(MissionCompletion::getCompletedAt)
                    .orElse(null));
            
            // 실패한 미션의 실패 시간 설정
            if (failedMissionIds.contains(mission.getMissionId())) {
                dto.setFailedAt(failedMissions.stream()
                        .filter(mf -> mf.getMission().getMissionId().equals(mission.getMissionId()))
                        .findFirst()
                        .map(MissionFailure::getFailedAt)
                        .orElse(null));
            }
            
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
        
        // 미션 완료 시 도장 찍기 (20페이지 이상 읽은 경우)
        try {
            // 오늘 읽은 페이지 수 조회
            Integer todayPagesRead = readingLogService.getTodayPagesRead(userId);
            if (todayPagesRead != null && todayPagesRead >= 20) {
                // 도장 획득 처리
                stampService.earnStamp(userId, today, todayPagesRead);
                log.info("미션 완료로 인한 도장 획득: 사용자 {}, 날짜 {}, 페이지 수 {}", userId, today, todayPagesRead);
            }
        } catch (Exception e) {
            log.warn("미션 완료 시 도장 처리 중 오류 발생: 사용자 {}, 미션 {}", userId, missionId, e);
        }
        
        log.info("미션 완료: 사용자 {}, 미션 {}", userId, missionId);
    }
    
    /**
     * 미션 실패 기록을 저장합니다.
     */
    public void recordMissionFailure(Long userId, Long missionId, String failureReason) {
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 미션 존재 확인
        Mission mission = missionRepository.findById(missionId)
                .orElseThrow(() -> new RuntimeException("미션을 찾을 수 없습니다."));
        
        // 오늘 이미 실패 기록이 있는지 확인
        LocalDate today = LocalDate.now();
        boolean alreadyFailed = missionFailureRepository
                .existsByUser_UserIdAndMission_MissionIdAndFailureDate(userId, missionId, today);
        
        if (alreadyFailed) {
            throw new RuntimeException("이미 실패 기록이 있는 미션입니다.");
        }
        
        // 미션 실패 기록 생성
        MissionFailure failure = new MissionFailure();
        failure.setUser(user);
        failure.setMission(mission);
        failure.setFailureDate(today);
        failure.setFailedAt(LocalDateTime.now());
        failure.setFailureReason(failureReason);
        
        missionFailureRepository.save(failure);
        
        log.info("미션 실패 기록 저장: 사용자 {}, 미션 {}, 사유: {}", userId, missionId, failureReason);
    }
    
    /**
     * 사용자의 미션 실패 기록을 조회합니다.
     */
    public List<MissionFailure> getUserMissionFailures(Long userId) {
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return missionFailureRepository.findByUser_UserIdOrderByFailedAtDesc(userId);
    }
    
    /**
     * 사용자의 특정 기간 미션 실패 기록을 조회합니다.
     */
    public List<MissionFailure> getUserMissionFailuresByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return missionFailureRepository.findByUserAndDateRange(userId, startDate, endDate);
    }
    
    /**
     * 특정 날짜의 미션 기록을 조회합니다 (완료 + 실패).
     */
    public Map<String, Object> getMissionRecordsByDate(Long userId, LocalDate date) {
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 해당 날짜의 완료된 미션들 조회
        List<MissionCompletion> completedMissions = missionCompletionRepository
                .findByUser_UserIdAndCompletedDate(userId, date);
        
        // 해당 날짜의 실패한 미션들 조회
        List<MissionFailure> failedMissions = missionFailureRepository
                .findByUser_UserIdAndFailureDate(userId, date);
        
        // 모든 미션 목록 조회
        List<Mission> allMissions = missionRepository.findAll();
        
        // 결과 맵 생성
        Map<String, Object> result = new HashMap<>();
        result.put("date", date.toString());
        result.put("completedMissions", completedMissions);
        result.put("failedMissions", failedMissions);
        result.put("completedCount", completedMissions.size());
        result.put("failedCount", failedMissions.size());
        result.put("totalMissions", allMissions.size());
        
        // 미션별 상태 정보 생성
        List<Map<String, Object>> missionStatusList = new ArrayList<>();
        for (Mission mission : allMissions) {
            Map<String, Object> missionStatus = new HashMap<>();
            missionStatus.put("missionId", mission.getMissionId());
            missionStatus.put("title", mission.getTitle());
            missionStatus.put("description", mission.getDescription());
            missionStatus.put("type", mission.getType());
            missionStatus.put("targetValue", mission.getTargetValue());
            missionStatus.put("unit", mission.getUnit());
            missionStatus.put("reward", mission.getReward());
            
            // 완료 여부 확인
            boolean isCompleted = completedMissions.stream()
                    .anyMatch(cm -> cm.getMission().getMissionId().equals(mission.getMissionId()));
            
            // 실패 여부 확인
            boolean isFailed = failedMissions.stream()
                    .anyMatch(fm -> fm.getMission().getMissionId().equals(mission.getMissionId()));
            
            missionStatus.put("isCompleted", isCompleted);
            missionStatus.put("isFailed", isFailed);
            missionStatus.put("status", isCompleted ? "COMPLETED" : (isFailed ? "FAILED" : "PENDING"));
            
            // 완료/실패 시간 추가
            if (isCompleted) {
                completedMissions.stream()
                        .filter(cm -> cm.getMission().getMissionId().equals(mission.getMissionId()))
                        .findFirst()
                        .ifPresent(cm -> missionStatus.put("completedAt", cm.getCompletedAt()));
            }
            
            if (isFailed) {
                failedMissions.stream()
                        .filter(fm -> fm.getMission().getMissionId().equals(mission.getMissionId()))
                        .findFirst()
                        .ifPresent(fm -> {
                            missionStatus.put("failedAt", fm.getFailedAt());
                            missionStatus.put("failureReason", fm.getFailureReason());
                        });
            }
            
            missionStatusList.add(missionStatus);
        }
        
        result.put("missionStatusList", missionStatusList);
        
        return result;
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
     * 사용자의 모든 미션 완료 기록을 조회합니다.
     */
    public List<MissionCompletion> getUserMissionCompletions(Long userId) {
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // Fetch Join을 사용하여 N+1 문제 해결
        return missionCompletionRepository.findByUser_UserIdWithFetchJoin(userId);
    }
    
    /**
     * 사용자의 특정 기간 미션 완료 기록을 조회합니다.
     */
    public List<MissionCompletion> getUserMissionCompletionsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        // 사용자 존재 확인
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        return missionCompletionRepository.findByUser_UserIdAndCompletedDateBetweenOrderByCompletedAtDesc(userId, startDate, endDate);
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
