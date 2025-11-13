package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class UserBookDTO {
    private Long userBookId;
    private BookDTO book;
    private String status;
    private int currentPage;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private String readingTime; // 읽은 시간 (예: "1h30m")
    private LocalDate completionDate; // 완독 날짜 (READ_DONE 상태일 때)
}
