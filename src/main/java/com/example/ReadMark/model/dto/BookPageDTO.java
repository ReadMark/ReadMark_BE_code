package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class BookPageDTO {
    
    private Long pageId;
    private Long bookId;
    private Long userId;
    private Integer pageNumber;
    private LocalDateTime capturedAt;
    private LocalDateTime createdAt;
}
