package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class ReadingSessionDTO {
    
    private Long sessionId;
    private Long userId;
    private Long bookId;
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private Integer totalPagesRead;
    private Integer totalWordsRead;
    private Long readingDurationMinutes;
    private String sessionNotes;
    
    // 독서 시간 계산 (분 단위)
    public Long getReadingDurationMinutes() {
        if (startTime == null || endTime == null) return 0L;
        return java.time.Duration.between(startTime, endTime).toMinutes();
    }
}
