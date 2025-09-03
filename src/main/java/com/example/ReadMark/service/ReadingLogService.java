package com.example.ReadMark.service;

import com.example.ReadMark.model.dto.DailyReadingDTO;
import com.example.ReadMark.model.dto.ReadingLogDTO;
import com.example.ReadMark.model.entity.ReadingLog;
import com.example.ReadMark.model.entity.User;
import com.example.ReadMark.repository.ReadingLogRepository;
import com.example.ReadMark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class ReadingLogService {
    
    private final ReadingLogRepository readingLogRepository;
    private final UserRepository userRepository;
    
    public ReadingLog createReadingLog(Long userId, LocalDate readDate, int pagesRead) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        // 같은 날짜에 이미 기록이 있는지 확인
        Optional<ReadingLog> existingLog = readingLogRepository.findByUserIdAndDate(userId, readDate);
        
        if (existingLog.isPresent()) {
            // 기존 기록에 페이지 수 추가
            ReadingLog log = existingLog.get();
            log.setPagesRead(log.getPagesRead() + pagesRead);
            return readingLogRepository.save(log);
        } else {
            // 새로운 기록 생성
            ReadingLog readingLog = new ReadingLog();
            readingLog.setUser(user);
            readingLog.setReadDate(readDate);
            readingLog.setPagesRead(pagesRead);
            return readingLogRepository.save(readingLog);
        }
    }
    
    public List<ReadingLogDTO> getReadingLogsByDateRange(Long userId, LocalDate startDate, LocalDate endDate) {
        List<ReadingLog> readingLogs = readingLogRepository.findReadingLogsWithUserInfo(userId, startDate, endDate);
        return readingLogs.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Integer getTodayPagesRead(Long userId) {
        return readingLogRepository.sumPagesReadByUserIdAndDate(userId, LocalDate.now());
    }
    
    public Integer getMaxConsecutiveReadingDays(Long userId) {
        return readingLogRepository.getMaxConsecutiveReadingDays(userId);
    }
    
    public Integer getTotalReadingDays(Long userId) {
        return readingLogRepository.getTotalReadingDays(userId);
    }
    
    public List<DailyReadingDTO> getDailyReadingStats(Long userId, LocalDate startDate, LocalDate endDate) {
        List<ReadingLog> readingLogs = readingLogRepository.findReadingLogsWithUserInfo(userId, startDate, endDate);
        return readingLogs.stream()
                .map(this::convertToDailyReadingDTO)
                .collect(Collectors.toList());
    }
    
    public ReadingLogDTO convertToDTO(ReadingLog readingLog) {
        ReadingLogDTO dto = new ReadingLogDTO();
        dto.setLogId(readingLog.getLogId());
        dto.setReadDate(readingLog.getReadDate());
        dto.setPagesRead(readingLog.getPagesRead());
        dto.setCreatedAt(readingLog.getCreatedAt());
        return dto;
    }
    
    public DailyReadingDTO convertToDailyReadingDTO(ReadingLog readingLog) {
        DailyReadingDTO dto = new DailyReadingDTO();
        dto.setDate(readingLog.getReadDate());
        dto.setPagesRead(readingLog.getPagesRead());
        dto.setReadingTime(0); // 현재는 페이지 수만 저장, 시간은 별도로 관리 필요
        return dto;
    }
}


