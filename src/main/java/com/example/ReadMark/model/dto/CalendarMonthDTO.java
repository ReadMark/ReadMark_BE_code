package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Getter
@Setter
public class CalendarMonthDTO {
    
    private int year;
    private int month;
    private LocalDate startDate; // 달력 시작일 (월요일)
    private LocalDate endDate; // 달력 종료일 (일요일)
    
    // 월별 통계
    private Integer totalReadingDays; // 총 독서한 날
    private Integer totalPages; // 총 읽은 페이지
    private Integer totalMinutes; // 총 독서 시간(분)
    private Integer maxConsecutiveDays; // 최대 연속 독서일
    private Integer currentConsecutiveDays; // 현재 연속 독서일
    
    // 일별 데이터
    private List<CalendarDayDTO> days; // 42개 (6주 x 7일)
    
    // 요약 정보
    private Map<String, Object> summary;
    
    public CalendarMonthDTO(int year, int month) {
        this.year = year;
        this.month = month;
        this.totalReadingDays = 0;
        this.totalPages = 0;
        this.totalMinutes = 0;
        this.maxConsecutiveDays = 0;
        this.currentConsecutiveDays = 0;
    }
}
