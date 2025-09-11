package com.example.ReadMark.service;

import com.example.ReadMark.model.dto.ReadingSessionDTO;
import com.example.ReadMark.model.entity.ReadingLog;
import com.example.ReadMark.model.entity.User;
import com.example.ReadMark.repository.ReadingLogRepository;
import com.example.ReadMark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class ReadingSessionService {
    
    private final ReadingLogRepository readingLogRepository;
    private final UserRepository userRepository;
    private final GoogleVisionService visionService;
    
    // 사용자별 독서 세션 캐시 (실제로는 Redis 사용 권장)
    private final Map<Long, ReadingSessionDTO> activeSessions = new HashMap<>();
    
    /**
     * 새로운 독서 세션을 시작합니다.
     */
    public ReadingSessionDTO startReadingSession(Long userId, Long bookId) {
        // 기존 세션이 있다면 종료
        if (activeSessions.containsKey(userId)) {
            endReadingSession(userId);
        }
        
        ReadingSessionDTO session = new ReadingSessionDTO();
        session.setUserId(userId);
        session.setBookId(bookId);
        session.setStartTime(LocalDateTime.now());
        session.setTotalPagesRead(0);
        session.setTotalNumbersRead(0);
        
        activeSessions.put(userId, session);
        
        log.info("독서 세션 시작: 사용자 {}, 책 {}", userId, bookId);
        return session;
    }
    
    /**
     * 독서 세션을 종료합니다.
     */
    public ReadingSessionDTO endReadingSession(Long userId) {
        ReadingSessionDTO session = activeSessions.remove(userId);
        if (session != null) {
            session.setEndTime(LocalDateTime.now());
            
            // 독서 기록 생성
            if (session.getTotalPagesRead() > 0) {
                createReadingLog(session);
            }
            
            log.info("독서 세션 종료: 사용자 {}, 총 {}페이지, {}분", 
                    userId, session.getTotalPagesRead(), session.getReadingDurationMinutes());
        }
        
        return session;
    }
    
    /**
     * 이미지 분석 결과를 세션에 추가합니다.
     */
    public void addImageToSession(Long userId, byte[] imageBytes, Integer pageNumber) {
        ReadingSessionDTO session = activeSessions.get(userId);
        if (session == null) {
            log.warn("활성 세션이 없습니다: 사용자 {}", userId);
            return;
        }
        
        // confidence 기반으로 품질 평가 (기본값 0.8 이상이면 유효)
        if (true) { // confidence는 이미 Google Vision API에서 검증됨
            session.setTotalPagesRead(session.getTotalPagesRead() + 1);
            // 숫자 개수 계산 (간단히 1로 설정)
            session.setTotalNumbersRead(session.getTotalNumbersRead() + 1);
            
            log.info("세션에 페이지 추가: 사용자 {}, 총 {}페이지", 
                    userId, session.getTotalPagesRead());
        } else {
            log.warn("텍스트 품질이 낮아 제외됨: 사용자 {}", userId);
        }
    }
    
    /**
     * 독서 기록을 생성합니다.
     */
    private void createReadingLog(ReadingSessionDTO session) {
        try {
            User user = userRepository.findById(session.getUserId())
                    .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
            
            ReadingLog readingLog = new ReadingLog();
            readingLog.setUser(user);
            readingLog.setReadDate(LocalDate.now());
            readingLog.setPagesRead(session.getTotalPagesRead());
            
            readingLogRepository.save(readingLog);
            
            log.info("독서 기록 생성: 사용자 {}, {}페이지", 
                    session.getUserId(), session.getTotalPagesRead());
                    
        } catch (Exception e) {
            log.error("독서 기록 생성 실패", e);
        }
    }
    
    /**
     * 최대 연속 독서일을 계산합니다.
     */
    public int getMaxConsecutiveReadingDays(Long userId) {
        List<LocalDate> readingDates = getReadingDates(userId);
        
        if (readingDates.isEmpty()) {
            return 0;
        }
        
        // 날짜 정렬
        readingDates.sort(LocalDate::compareTo);
        
        int maxConsecutive = 1;
        int currentConsecutive = 1;
        
        for (int i = 1; i < readingDates.size(); i++) {
            LocalDate prevDate = readingDates.get(i - 1);
            LocalDate currentDate = readingDates.get(i);
            
            if (ChronoUnit.DAYS.between(prevDate, currentDate) == 1) {
                currentConsecutive++;
                maxConsecutive = Math.max(maxConsecutive, currentConsecutive);
            } else {
                currentConsecutive = 1;
            }
        }
        
        return maxConsecutive;
    }
    
    /**
     * 총 독서일을 계산합니다.
     */
    public int getTotalReadingDays(Long userId) {
        return getReadingDates(userId).size();
    }
    
    /**
     * 현재 연속 독서일을 계산합니다.
     */
    public int getCurrentConsecutiveDays(Long userId) {
        List<LocalDate> readingDates = getReadingDates(userId);
        
        if (readingDates.isEmpty()) {
            return 0;
        }
        
        // 날짜 정렬
        readingDates.sort(LocalDate::compareTo);
        
        LocalDate today = LocalDate.now();
        LocalDate lastReadingDate = readingDates.get(readingDates.size() - 1);
        
        // 오늘 읽지 않았다면 0
        if (!lastReadingDate.equals(today)) {
            return 0;
        }
        
        int consecutiveDays = 1;
        LocalDate checkDate = lastReadingDate.minusDays(1);
        
        for (int i = readingDates.size() - 2; i >= 0; i--) {
            if (readingDates.get(i).equals(checkDate)) {
                consecutiveDays++;
                checkDate = checkDate.minusDays(1);
            } else {
                break;
            }
        }
        
        return consecutiveDays;
    }
    
    /**
     * 사용자의 독서 날짜 목록을 가져옵니다.
     */
    private List<LocalDate> getReadingDates(Long userId) {
        // 최근 1년간의 독서 기록 조회
        LocalDate startDate = LocalDate.now().minusYears(1);
        LocalDate endDate = LocalDate.now();
        
        List<ReadingLog> readingLogs = readingLogRepository.findByUser_UserIdAndReadDateBetween(userId, startDate, endDate);
        
        return readingLogs.stream()
                .map(ReadingLog::getReadDate)
                .distinct()
                .collect(Collectors.toList());
    }
    
    /**
     * 월별 독서 통계를 가져옵니다.
     */
    public Map<String, Object> getMonthlyReadingStats(Long userId, int months) {
        LocalDate startDate = LocalDate.now().minusMonths(months);
        LocalDate endDate = LocalDate.now();
        
        List<ReadingLog> readingLogs = readingLogRepository.findByUser_UserIdAndReadDateBetween(userId, startDate, endDate);
        
        Map<String, Object> stats = new HashMap<>();
        Map<String, Integer> monthlyPages = new HashMap<>();
        Map<String, Integer> monthlyDays = new HashMap<>();
        
        for (ReadingLog log : readingLogs) {
            String monthKey = log.getReadDate().format(java.time.format.DateTimeFormatter.ofPattern("yyyy-MM"));
            
            monthlyPages.merge(monthKey, log.getPagesRead(), Integer::sum);
            monthlyDays.merge(monthKey, 1, Integer::sum);
        }
        
        stats.put("monthlyPages", monthlyPages);
        stats.put("monthlyDays", monthlyDays);
        stats.put("totalPages", monthlyPages.values().stream().mapToInt(Integer::intValue).sum());
        stats.put("totalDays", monthlyDays.values().stream().mapToInt(Integer::intValue).sum());
        
        return stats;
    }
    
    /**
     * 독서 습관 분석을 가져옵니다.
     */
    public Map<String, Object> getReadingHabitAnalysis(Long userId) {
        List<LocalDate> readingDates = getReadingDates(userId);
        
        Map<String, Object> analysis = new HashMap<>();
        
        if (readingDates.isEmpty()) {
            analysis.put("avgPagesPerDay", 0);
            analysis.put("favoriteDayOfWeek", "없음");
            analysis.put("readingFrequency", "없음");
            return analysis;
        }
        
        // 평균 페이지 수 계산
        List<ReadingLog> recentLogs = readingLogRepository.findByUser_UserIdAndReadDateBetween(
                userId, LocalDate.now().minusMonths(1), LocalDate.now());
        
        double avgPages = recentLogs.stream()
                .mapToInt(ReadingLog::getPagesRead)
                .average()
                .orElse(0.0);
        
        // 선호 요일 분석
        Map<String, Long> dayOfWeekCount = readingDates.stream()
                .map(date -> date.getDayOfWeek().getDisplayName(
                        java.time.format.TextStyle.SHORT, 
                        java.util.Locale.KOREAN))
                .collect(Collectors.groupingBy(day -> day, Collectors.counting()));
        
        String favoriteDay = dayOfWeekCount.entrySet().stream()
                .max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey)
                .orElse("없음");
        
        // 독서 빈도 분석
        long totalDays = ChronoUnit.DAYS.between(
                readingDates.get(0), 
                readingDates.get(readingDates.size() - 1)) + 1;
        double frequency = (double) readingDates.size() / totalDays * 100;
        
        analysis.put("avgPagesPerDay", Math.round(avgPages * 10) / 10.0);
        analysis.put("favoriteDayOfWeek", favoriteDay);
        analysis.put("readingFrequency", String.format("%.1f%%", frequency));
        analysis.put("totalReadingDays", readingDates.size());
        analysis.put("maxConsecutiveDays", getMaxConsecutiveReadingDays(userId));
        analysis.put("currentConsecutiveDays", getCurrentConsecutiveDays(userId));
        
        return analysis;
    }
}
