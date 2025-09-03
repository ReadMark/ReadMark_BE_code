package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class ReadingLogDTO {
    private Long logId;
    private LocalDate readDate;
    private int pagesRead;
    private LocalDateTime createdAt;
}


