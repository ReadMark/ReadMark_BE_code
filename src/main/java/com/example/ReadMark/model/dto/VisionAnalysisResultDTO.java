package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class VisionAnalysisResultDTO {
    
    private String extractedText;
    private List<String> detectedWords;
    private Double confidence;
    private LocalDateTime analysisTime;
    private String language;
    private Integer estimatedPageNumber;
    private Boolean isBookPage;
    private String errorMessage;
    
    // 분석 성공 여부
    public boolean isSuccess() {
        return errorMessage == null && extractedText != null && !extractedText.trim().isEmpty();
    }
    
    // 텍스트 길이 (단어 수 추정)
    public int getWordCount() {
        if (extractedText == null) return 0;
        return extractedText.split("\\s+").length;
    }
}
