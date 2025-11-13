package com.example.ReadMark.service;

import com.example.ReadMark.model.dto.CalendarDayDTO;
import com.example.ReadMark.model.dto.CalendarMonthDTO;
import com.example.ReadMark.model.entity.BookPage;
import com.example.ReadMark.model.entity.ReadingLog;
import com.example.ReadMark.model.entity.ReadingSession;
import com.example.ReadMark.repository.BookPageRepository;
import com.example.ReadMark.repository.ReadingLogRepository;
import com.example.ReadMark.repository.ReadingSessionRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.time.temporal.WeekFields;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class CalendarService {
    
    private final BookPageRepository bookPageRepository;
    private final ReadingLogRepository readingLogRepository;
    private final ReadingSessionRepository readingSessionRepository;
    private final ReadingLogService readingLogService;
    
    /**
     * 특정 월의 캘린더 데이터를 조회합니다.
     */
    @Transactional(readOnly = true)
    public CalendarMonthDTO getCalendarMonth(Long userId, int year, int month) {
        CalendarMonthDTO calendar = new CalendarMonthDTO(year, month);
        
        // 달력 범위 계산 (월요일 시작, 일요일 종료)
        LocalDate monthStart = LocalDate.of(year, month, 1);
        LocalDate monthEnd = monthStart.withDayOfMonth(monthStart.lengthOfMonth());
        
        // 달력 시작일 (월요일)
        LocalDate calendarStart = monthStart.with(WeekFields.ISO.dayOfWeek(), 1);
        // 달력 종료일 (일요일)
        LocalDate calendarEnd = monthEnd.with(WeekFields.ISO.dayOfWeek(), 7);
        
        calendar.setStartDate(calendarStart);
        calendar.setEndDate(calendarEnd);
        
        // 해당 기간의 독서 데이터 조회
        List<CalendarDayDTO> days = getCalendarDays(userId, calendarStart, calendarEnd);
        calendar.setDays(days);
        
        // 월별 통계 계산
        calculateMonthStatistics(calendar, days, monthStart, monthEnd);
        
        log.info("캘린더 데이터 조회 완료: 사용자 {}, {}-{}", userId, year, month);
        return calendar;
    }
    
    /**
     * 캘린더 기간의 일별 데이터를 생성합니다.
     */
    private List<CalendarDayDTO> getCalendarDays(Long userId, LocalDate startDate, LocalDate endDate) {
        List<CalendarDayDTO> days = new ArrayList<>();
        
        // 각 날짜별로 독서 데이터 조회
        LocalDate currentDate = startDate;
        while (!currentDate.isAfter(endDate)) {
            CalendarDayDTO dayData = getDayReadingData(userId, currentDate);
            days.add(dayData);
            currentDate = currentDate.plusDays(1);
        }
        
        return days;
    }
    
    /**
     * 특정 날짜의 독서 데이터를 조회합니다.
     */
    private CalendarDayDTO getDayReadingData(Long userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        // 해당 날짜의 독서 세션 조회
        List<ReadingSession> readingSessions = readingSessionRepository
                .findByUser_UserIdAndStartTimeBetweenOrderByStartTimeAsc(userId, startOfDay, endOfDay);
        
        // 해당 날짜의 책 페이지 조회
        List<BookPage> bookPages = bookPageRepository
                .findByUser_UserIdAndCapturedAtBetween(userId, startOfDay, endOfDay);
        
        boolean hasReading = !readingSessions.isEmpty() || !bookPages.isEmpty();
        int totalPages = bookPages.size();
        int sessionCount = readingSessions.size();
        
        LocalDateTime firstReadingTime = null;
        LocalDateTime lastReadingTime = null;
        
        if (hasReading) {
            // 첫 독서 시간
            Optional<LocalDateTime> firstTime = readingSessions.stream()
                    .map(ReadingSession::getStartTime)
                    .min(LocalDateTime::compareTo);
            if (firstTime.isPresent()) {
                firstReadingTime = firstTime.get();
            }
            
            // 마지막 독서 시간
            Optional<LocalDateTime> lastTime = readingSessions.stream()
                    .map(ReadingSession::getStartTime)
                    .max(LocalDateTime::compareTo);
            if (lastTime.isPresent()) {
                lastReadingTime = lastTime.get();
            }
        }
        
        CalendarDayDTO dayData = new CalendarDayDTO(date, hasReading, totalPages, 0, sessionCount);
        dayData.setFirstReadingTime(firstReadingTime);
        dayData.setLastReadingTime(lastReadingTime);
        
        return dayData;
    }
    
    /**
     * 월별 통계를 계산합니다.
     */
    private void calculateMonthStatistics(CalendarMonthDTO calendar, List<CalendarDayDTO> days, 
                                        LocalDate monthStart, LocalDate monthEnd) {
        // 해당 월의 데이터만 필터링
        List<CalendarDayDTO> monthDays = days.stream()
                .filter(day -> !day.getDate().isBefore(monthStart) && !day.getDate().isAfter(monthEnd))
                .collect(Collectors.toList());
        
        // 총 독서한 날
        int totalReadingDays = (int) monthDays.stream()
                .filter(CalendarDayDTO::isHasReading)
                .count();
        
        // 총 읽은 페이지
        int totalPages = monthDays.stream()
                .mapToInt(CalendarDayDTO::getTotalPages)
                .sum();
        
        // 총 독서 시간
        int totalMinutes = monthDays.stream()
                .mapToInt(CalendarDayDTO::getTotalMinutes)
                .sum();
        
        // 연속 독서일 계산
        int maxConsecutiveDays = calculateMaxConsecutiveDays(monthDays);
        int currentConsecutiveDays = calculateCurrentConsecutiveDays(monthDays, monthEnd);
        
        calendar.setTotalReadingDays(totalReadingDays);
        calendar.setTotalPages(totalPages);
        calendar.setTotalMinutes(totalMinutes);
        calendar.setMaxConsecutiveDays(maxConsecutiveDays);
        calendar.setCurrentConsecutiveDays(currentConsecutiveDays);
        
        // 요약 정보 생성
        Map<String, Object> summary = new HashMap<>();
        summary.put("averagePagesPerDay", totalReadingDays > 0 ? totalPages / totalReadingDays : 0);
        summary.put("averageMinutesPerDay", totalReadingDays > 0 ? totalMinutes / totalReadingDays : 0);
        summary.put("readingRate", monthDays.size() > 0 ? (double) totalReadingDays / monthDays.size() * 100 : 0);
        calendar.setSummary(summary);
    }
    
    /**
     * 최대 연속 독서일을 계산합니다.
     */
    private int calculateMaxConsecutiveDays(List<CalendarDayDTO> days) {
        int maxConsecutive = 0;
        int currentConsecutive = 0;
        
        for (CalendarDayDTO day : days) {
            if (day.isHasReading()) {
                currentConsecutive++;
                maxConsecutive = Math.max(maxConsecutive, currentConsecutive);
            } else {
                currentConsecutive = 0;
            }
        }
        
        return maxConsecutive;
    }
    
    /**
     * 현재 연속 독서일을 계산합니다.
     */
    private int calculateCurrentConsecutiveDays(List<CalendarDayDTO> days, LocalDate endDate) {
        if (days == null || days.isEmpty()) {
            return 0;
        }

        LocalDate today = LocalDate.now();

        // 통계를 조회하는 월이 현재 월보다 뒤라면 현재 날짜 대신 해당 월의 마지막 날을 사용
        if (today.isAfter(endDate)) {
            today = endDate;
        }

        // 오늘 날짜에 해당하는 인덱스 탐색
        int todayIndex = -1;
        for (int i = 0; i < days.size(); i++) {
            if (days.get(i).getDate().equals(today)) {
                todayIndex = i;
                break;
            }
        }

        if (todayIndex == -1) {
            return 0;
        }

        // 오늘 읽지 않았다면 연속 기록은 종료된 것으로 간주
        if (!days.get(todayIndex).isHasReading()) {
            return 0;
        }

        int consecutiveDays = 1;
        LocalDate expectedDate = today.minusDays(1);

        for (int i = todayIndex - 1; i >= 0; i--) {
            CalendarDayDTO day = days.get(i);

            // 날짜가 끊기면 더 이상 연속이 아님
            if (!day.getDate().equals(expectedDate)) {
                break;
            }

            if (!day.isHasReading()) {
                break;
            }

            consecutiveDays++;
            expectedDate = expectedDate.minusDays(1);
        }

        return consecutiveDays;
    }
    
    /**
     * 특정 날짜의 상세 독서 정보를 조회합니다.
     */
    public Map<String, Object> getDayDetail(Long userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        // 독서 세션
        List<ReadingSession> readingSessions = readingSessionRepository
                .findByUser_UserIdAndStartTimeBetweenOrderByStartTimeAsc(userId, startOfDay, endOfDay);
        
        // 책 페이지
        List<BookPage> bookPages = bookPageRepository
                .findByUser_UserIdAndCapturedAtBetween(userId, startOfDay, endOfDay);
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("date", date);
        detail.put("readingSessions", readingSessions);
        detail.put("bookPages", bookPages);
        detail.put("totalSessions", readingSessions.size());
        detail.put("totalPages", bookPages.size());
        
        return detail;
    }
    
    /**
     * 오늘의 독서 통계를 조회합니다.
     * - 오늘 읽은 페이지 수
     * - 읽은 날 수
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTodayReadingStats(Long userId, LocalDate today) {
        LocalDateTime startOfDay = today.atStartOfDay();
        LocalDateTime endOfDay = today.atTime(23, 59, 59);
        
        // 오늘의 독서 세션
        List<ReadingSession> todaySessions = readingSessionRepository
                .findByUser_UserIdAndStartTimeBetweenOrderByStartTimeAsc(userId, startOfDay, endOfDay);
        
        // 오늘의 책 페이지
        List<BookPage> todayPages = bookPageRepository
                .findByUser_UserIdAndCapturedAtBetween(userId, startOfDay, endOfDay);
        
        
        // 오늘 읽은 페이지 수 (BookPage 기반)
        int todayPagesRead = todayPages.size();
        
        // ReadingLog에서도 확인 (백업용)
        Integer readingLogPages = readingLogService.getTodayPagesRead(userId);
        if (readingLogPages != null && readingLogPages > todayPagesRead) {
            todayPagesRead = readingLogPages; // ReadingLog가 더 많으면 그것을 사용
        }
        
        // 총 읽은 날 수 (이중 검증)
        long totalReadingDays = readingLogService.getTotalReadingDaysWithValidation(userId);
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("date", today);
        stats.put("todayPagesRead", todayPagesRead);
        stats.put("totalReadingDays", totalReadingDays);
        stats.put("todaySessions", todaySessions.size());
        stats.put("hasReadingToday", !todaySessions.isEmpty() || !todayPages.isEmpty());
        
        log.info("오늘의 독서 통계 조회 완료: 사용자 {}, 날짜 {}, 페이지 {}개", 
                userId, today, todayPagesRead);
        
        return stats;
    }
    
    /**
     * 전체 독서 통계를 조회합니다.
     * - 총 독서 시간
     * - 총 읽은 페이지 수
     * - 총 읽은 날 수
     * - 연속 독서 일수
     */
    @Transactional(readOnly = true)
    public Map<String, Object> getTotalReadingStats(Long userId) {
        // 전체 독서 세션
        List<ReadingSession> allSessions = readingSessionRepository.findByUser_UserId(userId);
        
        // 전체 책 페이지
        List<BookPage> allPages = bookPageRepository.findByUser_UserId(userId);
        
        // 총 읽은 페이지 수
        int totalPagesRead = allPages.size();
        
        // 총 읽은 날 수 (이중 검증)
        long totalReadingDays = readingLogService.getTotalReadingDaysWithValidation(userId);
        
        // 최대 연속 독서 일수 계산 (이중 검증)
        int maxConsecutiveDays = readingLogService.getMaxConsecutiveReadingDaysWithValidation(userId);
        
        // 도장개수 계산 (20페이지 이상 읽은 날 수)
        long totalStampDays = readingLogService.getTotalStampDaysWithValidation(userId);
        
        // 평균 페이지 수 (읽은 날 기준)
        double averagePagesPerDay = totalReadingDays > 0 ? (double) totalPagesRead / totalReadingDays : 0;
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalPagesRead", totalPagesRead);
        stats.put("totalReadingDays", totalReadingDays);
        stats.put("maxConsecutiveDays", maxConsecutiveDays);
        stats.put("totalStampDays", totalStampDays);  // 도장개수 추가
        stats.put("averagePagesPerDay", Math.round(averagePagesPerDay * 100.0) / 100.0);
        stats.put("totalSessions", allSessions.size());
        
        log.info("전체 독서 통계 조회 완료: 사용자 {}, 총 페이지 {}개, 총 읽은 날 {}일", 
                userId, totalPagesRead, totalReadingDays);
        
        return stats;
    }

    /**
     * 사용자의 총 독서 시간을 분 단위로 반환합니다.
     */
    @Transactional(readOnly = true)
    public Long getTotalReadingMinutes(Long userId) {
        try {
            Long minutes = readingSessionRepository.getTotalReadingMinutesByUserId(userId);
            return minutes != null ? minutes : 0L;
        } catch (Exception e) {
            log.error("총 독서 시간 조회 실패: userId={}", userId, e);
            return 0L;
        }
    }

    /**
     * 특정 책에 대해 사용자의 총 독서 시간을 분 단위로 반환합니다.
     */
    @Transactional(readOnly = true)
    public Long getTotalReadingMinutesByBook(Long userId, Long bookId) {
        try {
            Long minutes = readingSessionRepository.getTotalReadingMinutesByUserIdAndBookId(userId, bookId);
            return minutes != null ? minutes : 0L;
        } catch (Exception e) {
            log.error("총 독서 시간(책별) 조회 실패: userId={}, bookId={}", userId, bookId, e);
            return 0L;
        }
    }

    /**
     * 오늘의 총 독서 시간을 분 단위로 반환합니다.
     */
    @Transactional(readOnly = true)
    public Long getTodayReadingMinutes(Long userId, LocalDate today) {
        try {
            var startOfDay = today.atStartOfDay();
            var endOfDay = today.atTime(23, 59, 59);
            var sessions = readingSessionRepository
                    .findByUser_UserIdAndStartTimeBetweenOrderByStartTimeAsc(userId, startOfDay, endOfDay);

            long total = 0L;
            for (var s : sessions) {
                if (s.getStartTime() == null) continue;
                var end = s.getEndTime() != null ? s.getEndTime() : java.time.LocalDateTime.now();
                long minutes = java.time.Duration.between(s.getStartTime(), end).toMinutes();
                if (minutes > 0) total += minutes;
            }
            return total;
        } catch (Exception e) {
            log.error("오늘 총 독서 시간 조회 실패: userId={}, date={}", userId, today, e);
            return 0L;
        }
    }
    
    /**
     * 연속 독서 일수를 계산합니다.
     */
    private int calculateConsecutiveReadingDays(Long userId) {
        // 최근 30일간의 독서 데이터를 조회하여 연속일 계산
        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(30);
        
        List<CalendarDayDTO> recentDays = getCalendarDays(userId, startDate, endDate);
        
        int consecutiveDays = 0;
        
        // 오늘부터 역순으로 확인
        for (int i = recentDays.size() - 1; i >= 0; i--) {
            CalendarDayDTO day = recentDays.get(i);
            if (day.isHasReading()) {
                consecutiveDays++;
            } else {
                break;
            }
        }
        
        return consecutiveDays;
    }
    
    /**
     * 특정 날짜에 독서 기록이 있는지 확인합니다.
     */
    @Transactional(readOnly = true)
    public boolean hasReadingOnDate(Long userId, LocalDate date) {
        try {
            LocalDateTime startOfDay = date.atStartOfDay();
            LocalDateTime endOfDay = date.atTime(23, 59, 59);
            
            // 해당 날짜의 독서 세션 조회
            List<ReadingSession> readingSessions = readingSessionRepository
                    .findByUser_UserIdAndStartTimeBetweenOrderByStartTimeAsc(userId, startOfDay, endOfDay);
            
            // 해당 날짜의 책 페이지 조회
            List<BookPage> bookPages = bookPageRepository
                    .findByUser_UserIdAndCapturedAtBetween(userId, startOfDay, endOfDay);
            
            // 독서 기록이 있으면 true
            return !readingSessions.isEmpty() || !bookPages.isEmpty();
            
        } catch (Exception e) {
            log.error("독서 기록 확인 중 오류 발생: 사용자 {}, 날짜 {}", userId, date, e);
            return false;
        }
    }
}
