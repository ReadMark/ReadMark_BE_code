package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class DailyReadingDTO {
    private LocalDate date;
    private int pagesRead;
    private int readingTime; // 분 단위
}
