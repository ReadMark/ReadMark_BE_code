package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.Stamp;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;

public interface StampRepository extends JpaRepository<Stamp, Long> {
    
    /**
     * 사용자의 모든 도장 조회 (최신순)
     */
    List<Stamp> findByUser_UserIdOrderByEarnedDateDesc(Long userId);
    
    /**
     * 특정 날짜에 도장을 받았는지 확인
     */
    boolean existsByUser_UserIdAndEarnedDate(Long userId, LocalDate date);
    
    /**
     * 사용자의 총 도장 개수
     */
    long countByUser_UserId(Long userId);
    
    /**
     * 특정 기간의 도장 조회
     */
    @Query("SELECT s FROM Stamp s WHERE s.user.userId = :userId AND s.earnedDate BETWEEN :startDate AND :endDate ORDER BY s.earnedDate DESC")
    List<Stamp> findByUser_UserIdAndEarnedDateBetween(@Param("userId") Long userId, 
                                                      @Param("startDate") LocalDate startDate, 
                                                      @Param("endDate") LocalDate endDate);
    
    /**
     * 사용자의 최근 N개 도장 조회
     */
    @Query("SELECT s FROM Stamp s WHERE s.user.userId = :userId ORDER BY s.earnedDate DESC")
    List<Stamp> findTopNByUser_UserIdOrderByEarnedDateDesc(@Param("userId") Long userId);
}
