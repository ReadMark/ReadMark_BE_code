package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class MissionDTO {
    private Long missionId;
    private String title;
    private String description;
    private String type; // "READING_TIME", "PAGES_READ", "BOOKS_READ", "CONSECUTIVE_DAYS"
    private Integer targetValue;
    private String unit; // "분", "페이지", "권", "일"
    private String reward;
    private LocalDateTime createdAt;
    
    // 추가 필드 (미션 완료 여부)
    private boolean completed;
    private LocalDateTime completedAt;
    
    // 미션 실패 관련 필드
    private boolean failed;
    private LocalDateTime failedAt;
}
