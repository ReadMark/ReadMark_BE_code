package com.example.ReadMark.service;

import com.example.ReadMark.model.dto.DailyReadingDTO;
import com.example.ReadMark.model.dto.ReadingLogDTO;
import com.example.ReadMark.model.entity.ReadingLog;
import com.example.ReadMark.model.entity.User;
import com.example.ReadMark.repository.BookPageRepositoryCustom;
import com.example.ReadMark.repository.ReadingLogRepository;
import com.example.ReadMark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ReadingLogService {
    
    private final ReadingLogRepository readingLogRepository;
    private final UserRepository userRepository;
    private final BookPageRepositoryCustom bookPageRepository;
    private final StampService stampService;
    
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
    
    /**
     * 특정 날짜의 페이지 수를 자동으로 업데이트합니다.
     * BookPage가 생성될 때마다 호출되어 ReadingLog를 동기화합니다.
     * 20페이지 이상 읽으면 도장도 자동으로 부여합니다.
     */
    public void updateDailyPagesRead(Long userId, LocalDate date) {
        try {
            // 해당 날짜의 BookPage 수 조회
            int pagesRead = bookPageRepository.countByUserIdAndDate(userId, date);
            
            // 해당 날짜의 ReadingLog 조회
            Optional<ReadingLog> existingLog = readingLogRepository.findByUserIdAndDate(userId, date);
            
            if (existingLog.isPresent()) {
                // 기존 로그 업데이트
                ReadingLog log = existingLog.get();
                int oldPagesRead = log.getPagesRead();
                log.setPagesRead(pagesRead);
                readingLogRepository.save(log);
                
                // 페이지 수가 증가했고 20페이지 이상이면 도장 확인
                if (pagesRead > oldPagesRead && pagesRead >= 20) {
                    stampService.earnStamp(userId, date, pagesRead);
                }
            } else if (pagesRead > 0) {
                // 새 로그 생성
                ReadingLog newLog = new ReadingLog();
                newLog.setUser(userRepository.findById(userId).orElse(null));
                newLog.setReadDate(date);
                newLog.setPagesRead(pagesRead);
                readingLogRepository.save(newLog);
                
                // 20페이지 이상이면 도장 부여
                if (pagesRead >= 20) {
                    stampService.earnStamp(userId, date, pagesRead);
                }
            }
        } catch (Exception e) {
            log.error("일별 페이지 수 업데이트 실패: userId={}, date={}", userId, date, e);
        }
    }
    
    public Integer getMaxConsecutiveReadingDays(Long userId) {
        return readingLogRepository.getMaxConsecutiveReadingDays(userId);
    }
    
    /**
     * 이중 검증을 통한 최대 연속 독서일 조회
     * ReadingLog와 BookPage를 모두 확인하여 정확한 값 계산
     */
    @Transactional(readOnly = true)
    public Integer getMaxConsecutiveReadingDaysWithValidation(Long userId) {
        try {
            // 1. ReadingLog에서 조회 (메인)
            Integer readingLogMaxConsecutive = readingLogRepository.getMaxConsecutiveReadingDays(userId);
            if (readingLogMaxConsecutive == null) {
                readingLogMaxConsecutive = 0;
            }
            
            // 2. BookPage에서 조회 (백업) - 예외 발생 시 기본값 사용
            Integer bookPageMaxConsecutive = 0;
            try {
                bookPageMaxConsecutive = calculateMaxConsecutiveFromBookPages(userId);
                if (bookPageMaxConsecutive == null) {
                    bookPageMaxConsecutive = 0;
                }
            } catch (Exception e) {
                log.warn("BookPage 최대 연속 독서일 조회 실패, ReadingLog 값 사용: 사용자 {}", userId, e);
                bookPageMaxConsecutive = 0;
            }
            
            // 3. 더 큰 값을 사용 (데이터 누락 방지)
            Integer finalMaxConsecutive = Math.max(readingLogMaxConsecutive, bookPageMaxConsecutive);
            
            log.debug("최대 연속 독서일 검증: 사용자 {}, ReadingLog={}, BookPage={}, 최종={}", 
                    userId, readingLogMaxConsecutive, bookPageMaxConsecutive, finalMaxConsecutive);
            
            return finalMaxConsecutive;
            
        } catch (Exception e) {
            log.error("최대 연속 독서일 조회 중 오류 발생: 사용자 {}", userId, e);
            // 오류 시 기본값 반환
            return 0;
        }
    }
    
    /**
     * BookPage 데이터로부터 최대 연속 독서일 계산
     */
    private Integer calculateMaxConsecutiveFromBookPages(Long userId) {
        try {
            // BookPage에서 고유한 날짜 목록 조회
            List<LocalDate> bookPageDates = bookPageRepository.findDistinctReadingDatesByUserId(userId);
            
            if (bookPageDates.isEmpty()) {
                return 0;
            }
            
            // 날짜 정렬
            bookPageDates.sort(LocalDate::compareTo);
            
            int maxConsecutive = 1;
            int currentConsecutive = 1;
            
            for (int i = 1; i < bookPageDates.size(); i++) {
                LocalDate prevDate = bookPageDates.get(i - 1);
                LocalDate currentDate = bookPageDates.get(i);
                
                if (java.time.temporal.ChronoUnit.DAYS.between(prevDate, currentDate) == 1) {
                    currentConsecutive++;
                    maxConsecutive = Math.max(maxConsecutive, currentConsecutive);
                } else {
                    currentConsecutive = 1;
                }
            }
            
            return maxConsecutive;
            
        } catch (Exception e) {
            log.error("BookPage 기반 최대 연속 독서일 계산 중 오류: 사용자 {}", userId, e);
            return 0;
        }
    }
    
    public Integer getTotalReadingDays(Long userId) {
        return readingLogRepository.getTotalReadingDays(userId);
    }
    
    /**
     * 이중 검증을 통한 도장개수 조회
     * 20페이지 이상 읽은 날 수를 ReadingLog와 BookPage에서 모두 확인
     */
    @Transactional(readOnly = true)
    public Long getTotalStampDaysWithValidation(Long userId) {
        try {
            // 1. ReadingLog에서 조회 (메인)
            Long readingLogStampDays = readingLogRepository.countStampDays(userId);
            if (readingLogStampDays == null) {
                readingLogStampDays = 0L;
            }
            
            // 2. BookPage에서 조회 (백업) - 예외 발생 시 기본값 사용
            Long bookPageStampDays = 0L;
            try {
                bookPageStampDays = bookPageRepository.countStampDaysByUserId(userId);
                if (bookPageStampDays == null) {
                    bookPageStampDays = 0L;
                }
            } catch (Exception e) {
                log.warn("BookPage 도장개수 조회 실패, ReadingLog 값 사용: 사용자 {}", userId, e);
                bookPageStampDays = 0L;
            }
            
            // 3. 더 큰 값을 사용 (데이터 누락 방지)
            Long finalStampDays = Math.max(readingLogStampDays, bookPageStampDays);
            
            log.debug("도장개수 검증: 사용자 {}, ReadingLog={}, BookPage={}, 최종={}", 
                    userId, readingLogStampDays, bookPageStampDays, finalStampDays);
            
            return finalStampDays;
            
        } catch (Exception e) {
            log.error("도장개수 조회 중 오류 발생: 사용자 {}", userId, e);
            // 오류 시 기본값 반환
            return 0L;
        }
    }
    
    /**
     * 이중 검증을 통한 총 읽은 날 수 조회
     * ReadingLog를 메인으로, BookPage를 백업으로 사용
     */
    @Transactional(readOnly = true)
    public Long getTotalReadingDaysWithValidation(Long userId) {
        try {
            // 1. ReadingLog에서 조회 (메인)
            Long readingLogDays = readingLogRepository.countTotalReadingDays(userId);
            if (readingLogDays == null) {
                readingLogDays = 0L;
            }
            
            // 2. BookPage에서 조회 (백업) - 예외 발생 시 기본값 사용
            Long bookPageDays = 0L;
            try {
                bookPageDays = bookPageRepository.countDistinctReadingDaysByUserId(userId);
                if (bookPageDays == null) {
                    bookPageDays = 0L;
                }
            } catch (Exception e) {
                log.warn("BookPage 총 읽은 날 수 조회 실패, ReadingLog 값 사용: 사용자 {}", userId, e);
                bookPageDays = 0L;
            }
            
            // 3. 더 큰 값을 사용 (데이터 누락 방지)
            Long finalDays = Math.max(readingLogDays, bookPageDays);
            
            log.debug("총 읽은 날 수 검증: 사용자 {}, ReadingLog={}, BookPage={}, 최종={}", 
                    userId, readingLogDays, bookPageDays, finalDays);
            
            return finalDays;
            
        } catch (Exception e) {
            log.error("총 읽은 날 수 조회 중 오류 발생: 사용자 {}", userId, e);
            // 오류 시 기본값 반환
            return 0L;
        }
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
        return dto;
    }
}


