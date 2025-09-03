package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.ReadingLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReadingLogRepository extends JpaRepository<ReadingLog, Long>, ReadingLogRepositoryCustom {
    
    List<ReadingLog> findByUserIdAndReadDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT rl FROM ReadingLog rl WHERE rl.user.userId = :userId AND rl.readDate = :date")
    Optional<ReadingLog> findByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
    
    @Query("SELECT SUM(rl.pagesRead) FROM ReadingLog rl WHERE rl.user.userId = :userId AND rl.readDate = :date")
    Integer sumPagesReadByUserIdAndDate(@Param("userId") Long userId, @Param("date") LocalDate date);
}
