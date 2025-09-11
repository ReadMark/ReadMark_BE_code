package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BookPageDTO {
    
    private Long pageId;
    private Long bookId;
    private Long userId;
    private Integer pageNumber;
    private String imageUrl;
    private LocalDateTime capturedAt;
    private LocalDateTime createdAt;
    private Double confidence;
    private String deviceInfo;
    private String language;
    private Integer numberCount;
}
