package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.ReadingSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface ReadingSessionRepository extends JpaRepository<ReadingSession, Long> {
    
    /**
     * 사용자의 활성 독서 세션을 조회합니다.
     */
    @Query("SELECT rs FROM ReadingSession rs WHERE rs.user.userId = :userId AND rs.endTime IS NULL ORDER BY rs.startTime DESC")
    Optional<ReadingSession> findActiveSessionByUserId(@Param("userId") Long userId);
    
    /**
     * 사용자의 모든 독서 세션을 조회합니다.
     */
    List<ReadingSession> findByUser_UserIdOrderByStartTimeDesc(Long userId);
    
    /**
     * 특정 기간의 독서 세션을 조회합니다.
     */
    List<ReadingSession> findByUser_UserIdAndStartTimeBetweenOrderByStartTimeAsc(
            Long userId, LocalDateTime startTime, LocalDateTime endTime);
    
    /**
     * 특정 날짜의 독서 세션을 조회합니다.
     */
    @Query("SELECT rs FROM ReadingSession rs WHERE rs.user.userId = :userId AND DATE(rs.startTime) = DATE(:date)")
    List<ReadingSession> findByUser_UserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDateTime date);
    
    /**
     * 사용자의 총 독서 세션 수를 조회합니다.
     */
    long countByUser_UserId(Long userId);
    
    /**
     * 사용자의 총 독서 시간을 조회합니다.
     */
    @Query("SELECT COALESCE(SUM(TIMESTAMPDIFF(MINUTE, rs.startTime, rs.endTime)), 0) FROM ReadingSession rs WHERE rs.user.userId = :userId AND rs.endTime IS NOT NULL")
    Long getTotalReadingMinutesByUserId(@Param("userId") Long userId);
}
