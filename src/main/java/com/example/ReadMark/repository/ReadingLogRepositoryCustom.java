package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.ReadingLog;

import java.time.LocalDate;
import java.util.List;

public interface ReadingLogRepositoryCustom {
    List<ReadingLog> findReadingLogsWithUserInfo(Long userId, LocalDate startDate, LocalDate endDate);
    Integer getMaxConsecutiveReadingDays(Long userId);
    Integer getTotalReadingDays(Long userId);
}
