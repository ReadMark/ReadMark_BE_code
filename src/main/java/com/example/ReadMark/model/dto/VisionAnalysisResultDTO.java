package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
public class VisionAnalysisResultDTO {
    
    private List<String> detectedNumbers;
    private Double confidence;
    private LocalDateTime analysisTime;
    private String language;
    private Integer estimatedPageNumber;
    private Boolean isBookPage;
    private String errorMessage;
    
    // 분석 성공 여부 (숫자가 하나라도 있으면 성공)
    public boolean isSuccess() {
        return errorMessage == null && detectedNumbers != null && !detectedNumbers.isEmpty();
    }
    
    // 숫자 개수
    public int getNumberCount() {
        if (detectedNumbers == null) return 0;
        return detectedNumbers.size();
    }
}
