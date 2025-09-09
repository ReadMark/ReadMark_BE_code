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
    
    /**
     * 특정 월의 캘린더 데이터를 조회합니다.
     */
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
        int totalMinutes = readingSessions.stream()
                .mapToInt(session -> session.getReadingDurationMinutes() != null ? session.getReadingDurationMinutes().intValue() : 0)
                .sum();
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
        
        CalendarDayDTO dayData = new CalendarDayDTO(date, hasReading, totalPages, totalMinutes, sessionCount);
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
        int consecutiveDays = 0;
        
        // 끝 날짜부터 역순으로 확인
        for (int i = days.size() - 1; i >= 0; i--) {
            CalendarDayDTO day = days.get(i);
            if (day.isHasReading()) {
                consecutiveDays++;
            } else {
                break;
            }
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
        detail.put("totalMinutes", readingSessions.stream()
                .mapToInt(session -> session.getReadingDurationMinutes() != null ? session.getReadingDurationMinutes().intValue() : 0)
                .sum());
        
        return detail;
    }
}
