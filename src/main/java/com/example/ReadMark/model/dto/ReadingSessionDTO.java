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
    private Integer totalNumbersRead;
    private String sessionNotes;
}
