package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class CalendarDayDTO {
    
    private LocalDate date;
    private boolean hasReading; // 독서한 날인지
    private Integer totalPages; // 그날 읽은 총 페이지 수
    private Integer totalMinutes; // 그날 독서한 총 시간(분)
    private Integer sessionCount; // 그날 독서 세션 수
    private LocalDateTime firstReadingTime; // 첫 독서 시간
    private LocalDateTime lastReadingTime; // 마지막 독서 시간
    private String qualityLevel; // 그날 독서 품질 (우수/양호/보통/낮음)
    
    public CalendarDayDTO(LocalDate date) {
        this.date = date;
        this.hasReading = false;
        this.totalPages = 0;
        this.totalMinutes = 0;
        this.sessionCount = 0;
        this.qualityLevel = "없음";
    }
    
    public CalendarDayDTO(LocalDate date, boolean hasReading, Integer totalPages, 
                         Integer totalMinutes, Integer sessionCount) {
        this.date = date;
        this.hasReading = hasReading;
        this.totalPages = totalPages != null ? totalPages : 0;
        this.totalMinutes = totalMinutes != null ? totalMinutes : 0;
        this.sessionCount = sessionCount != null ? sessionCount : 0;
        this.qualityLevel = hasReading ? "보통" : "없음";
    }
}
