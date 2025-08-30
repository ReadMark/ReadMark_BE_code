package com.example.ReadMark.repository;

import com.example.ReadMark.entity.ReadingLog;
import com.example.ReadMark.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.util.List;

@Repository
public interface ReadingLogRepository extends JpaRepository<ReadingLog, Long> {
    List<ReadingLog> findByUserAndReadDateBetween(User user, LocalDate from, LocalDate to);
    List<ReadingLog> findByUser(User user);
}
