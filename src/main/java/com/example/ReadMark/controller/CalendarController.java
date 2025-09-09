package com.example.ReadMark.controller;

import com.example.ReadMark.model.dto.CalendarMonthDTO;
import com.example.ReadMark.service.CalendarService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/calendar")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class CalendarController {
    
    private final CalendarService calendarService;
    
    /**
     * 특정 월의 캘린더 데이터를 조회합니다.
     */
    @GetMapping("/{userId}/{year}/{month}")
    public ResponseEntity<?> getCalendarMonth(
            @PathVariable Long userId,
            @PathVariable int year,
            @PathVariable int month) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 월 유효성 검사
            if (month < 1 || month > 12) {
                response.put("success", false);
                response.put("message", "월은 1-12 사이의 값이어야 합니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 년도 유효성 검사
            if (year < 2020 || year > 2030) {
                response.put("success", false);
                response.put("message", "년도는 2020-2030 사이의 값이어야 합니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            CalendarMonthDTO calendar = calendarService.getCalendarMonth(userId, year, month);
            
            response.put("success", true);
            response.put("calendar", calendar);
            response.put("message", "캘린더 데이터 조회 성공");
            
            log.info("캘린더 조회 완료: 사용자 {}, {}-{}", userId, year, month);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("캘린더 조회 중 오류 발생: 사용자 {}, {}-{}", userId, year, month, e);
            response.put("success", false);
            response.put("message", "캘린더 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 현재 월의 캘린더 데이터를 조회합니다.
     */
    @GetMapping("/{userId}/current")
    public ResponseEntity<?> getCurrentMonth(@PathVariable Long userId) {
        LocalDate now = LocalDate.now();
        return getCalendarMonth(userId, now.getYear(), now.getMonthValue());
    }
    
    /**
     * 특정 날짜의 상세 독서 정보를 조회합니다.
     */
    @GetMapping("/{userId}/day/{year}/{month}/{day}")
    public ResponseEntity<?> getDayDetail(
            @PathVariable Long userId,
            @PathVariable int year,
            @PathVariable int month,
            @PathVariable int day) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            LocalDate date = LocalDate.of(year, month, day);
            Map<String, Object> detail = calendarService.getDayDetail(userId, date);
            
            response.put("success", true);
            response.put("detail", detail);
            response.put("message", "일별 상세 정보 조회 성공");
            
            log.info("일별 상세 조회 완료: 사용자 {}, {}", userId, date);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("일별 상세 조회 중 오류 발생: 사용자 {}, {}-{}-{}", userId, year, month, day, e);
            response.put("success", false);
            response.put("message", "일별 상세 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 오늘의 독서 정보를 조회합니다.
     */
    @GetMapping("/{userId}/today")
    public ResponseEntity<?> getTodayDetail(@PathVariable Long userId) {
        LocalDate today = LocalDate.now();
        return getDayDetail(userId, today.getYear(), today.getMonthValue(), today.getDayOfMonth());
    }
    
    /**
     * 캘린더 통계 요약을 조회합니다.
     */
    @GetMapping("/{userId}/stats")
    public ResponseEntity<?> getCalendarStats(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            LocalDate now = LocalDate.now();
            CalendarMonthDTO currentMonth = calendarService.getCalendarMonth(userId, now.getYear(), now.getMonthValue());
            
            // 이번 달 통계
            Map<String, Object> currentMonthStats = new HashMap<>();
            currentMonthStats.put("totalReadingDays", currentMonth.getTotalReadingDays());
            currentMonthStats.put("totalPages", currentMonth.getTotalPages());
            currentMonthStats.put("totalMinutes", currentMonth.getTotalMinutes());
            currentMonthStats.put("maxConsecutiveDays", currentMonth.getMaxConsecutiveDays());
            currentMonthStats.put("currentConsecutiveDays", currentMonth.getCurrentConsecutiveDays());
            currentMonthStats.put("readingRate", currentMonth.getSummary().get("readingRate"));
            
            // 지난 달 통계 (비교용)
            LocalDate lastMonth = now.minusMonths(1);
            CalendarMonthDTO lastMonthData = calendarService.getCalendarMonth(userId, lastMonth.getYear(), lastMonth.getMonthValue());
            
            Map<String, Object> lastMonthStats = new HashMap<>();
            lastMonthStats.put("totalReadingDays", lastMonthData.getTotalReadingDays());
            lastMonthStats.put("totalPages", lastMonthData.getTotalPages());
            lastMonthStats.put("totalMinutes", lastMonthData.getTotalMinutes());
            
            response.put("success", true);
            response.put("currentMonth", currentMonthStats);
            response.put("lastMonth", lastMonthStats);
            response.put("message", "캘린더 통계 조회 성공");
            
            log.info("캘린더 통계 조회 완료: 사용자 {}", userId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("캘린더 통계 조회 중 오류 발생: 사용자 {}", userId, e);
            response.put("success", false);
            response.put("message", "캘린더 통계 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
