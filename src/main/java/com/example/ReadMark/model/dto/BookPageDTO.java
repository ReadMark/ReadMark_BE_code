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
    private String extractedText;
    private String imageUrl;
    private LocalDateTime capturedAt;
    private LocalDateTime createdAt;
    private Double confidence;
    private String deviceInfo;
    private String language;
    private Integer wordCount;
    private Double textQuality;
    
    // 페이지 품질 평가 결과
    public String getQualityLevel() {
        if (textQuality == null) return "알 수 없음";
        if (textQuality >= 80) return "우수";
        if (textQuality >= 60) return "양호";
        if (textQuality >= 40) return "보통";
        return "낮음";
    }
    
    // 신뢰도 수준
    public String getConfidenceLevel() {
        if (confidence == null) return "알 수 없음";
        if (confidence >= 0.9) return "매우 높음";
        if (confidence >= 0.7) return "높음";
        if (confidence >= 0.5) return "보통";
        return "낮음";
    }
}
