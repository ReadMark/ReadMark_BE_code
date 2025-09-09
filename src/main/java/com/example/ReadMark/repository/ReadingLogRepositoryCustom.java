package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.ReadingLog;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface ReadingLogRepositoryCustom {
    List<ReadingLog> findByUser_UserIdAndReadDateBetween(Long userId, LocalDate startDate, LocalDate endDate);
    Optional<ReadingLog> findByUserIdAndDate(Long userId, LocalDate date);
    Integer sumPagesReadByUserIdAndDate(Long userId, LocalDate date);
    List<ReadingLog> findReadingLogsWithUserInfo(Long userId, LocalDate startDate, LocalDate endDate);
    List<ReadingLog> findReadingLogsByPageRange(Long userId, int minPages, int maxPages);
    List<ReadingLog> findReadingLogsWithPagination(Long userId, int offset, int limit);
    List<LocalDate> findDistinctReadingDates(Long userId);
    Integer getMaxConsecutiveReadingDays(Long userId);
    Integer getTotalReadingDays(Long userId);
}
