package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
public class StampDTO {
    
    private Long stampId;
    private Long userId;
    private LocalDate earnedDate;      // 도장을 받은 날짜
    private Integer pagesRead;         // 해당 날에 읽은 페이지 수
    private LocalDateTime createdAt;   // 도장 획득 시간
    private String description;        // 도장 획득 설명
    
    // 추가 정보
    private String formattedDate;      // 포맷된 날짜 (예: "2025-01-15")
    private String achievement;        // 달성 내용 (예: "25페이지 읽기")
}
