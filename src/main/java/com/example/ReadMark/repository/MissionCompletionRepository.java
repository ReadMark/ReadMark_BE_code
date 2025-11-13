package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.MissionCompletion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface MissionCompletionRepository extends JpaRepository<MissionCompletion, Long> {
    
    List<MissionCompletion> findByUser_UserIdAndCompletedDate(Long userId, LocalDate completedDate);
    
    boolean existsByUser_UserIdAndMission_MissionIdAndCompletedDate(Long userId, Long missionId, LocalDate completedDate);
    
    long countByUser_UserIdAndCompletedDate(Long userId, LocalDate completedDate);
    
    long countByUser_UserIdAndCompletedDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    List<MissionCompletion> findByUser_UserIdOrderByCompletedAtDesc(Long userId);
    
    List<MissionCompletion> findByUser_UserIdAndCompletedDateBetweenOrderByCompletedAtDesc(Long userId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT mc FROM MissionCompletion mc " +
           "JOIN FETCH mc.user u " +
           "JOIN FETCH mc.mission m " +
           "WHERE u.userId = :userId " +
           "ORDER BY mc.completedAt DESC")
    List<MissionCompletion> findByUser_UserIdWithFetchJoin(@Param("userId") Long userId);
}
