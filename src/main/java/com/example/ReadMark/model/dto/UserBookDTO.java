package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

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
}
