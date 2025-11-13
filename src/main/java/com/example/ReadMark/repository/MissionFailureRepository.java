package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.MissionFailure;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MissionFailureRepository extends JpaRepository<MissionFailure, Long> {
    
    /**
     * 특정 사용자의 특정 미션에 대한 오늘 실패 기록이 있는지 확인
     */
    boolean existsByUser_UserIdAndMission_MissionIdAndFailureDate(Long userId, Long missionId, LocalDate failureDate);
    
    /**
     * 특정 사용자의 모든 실패 기록 조회
     */
    List<MissionFailure> findByUser_UserIdOrderByFailedAtDesc(Long userId);
    
    /**
     * 특정 사용자의 특정 기간 실패 기록 조회
     */
    @Query("SELECT mf FROM MissionFailure mf WHERE mf.user.userId = :userId " +
           "AND mf.failureDate BETWEEN :startDate AND :endDate " +
           "ORDER BY mf.failedAt DESC")
    List<MissionFailure> findByUserAndDateRange(@Param("userId") Long userId, 
                                               @Param("startDate") LocalDate startDate, 
                                               @Param("endDate") LocalDate endDate);
    
    /**
     * 특정 미션의 실패 횟수 조회
     */
    long countByMission_MissionId(Long missionId);
    
    /**
     * 특정 사용자의 특정 미션 실패 횟수 조회
     */
    long countByUser_UserIdAndMission_MissionId(Long userId, Long missionId);
    
    /**
     * 특정 사용자의 특정 날짜 실패 기록 조회
     */
    List<MissionFailure> findByUser_UserIdAndFailureDate(Long userId, LocalDate failureDate);
}
