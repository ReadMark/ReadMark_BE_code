package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class FavoriteQuoteDTO {
    private Long favQuoteId;
    private Integer pageNumber;    // 즐겨찾기 페이지
    private String bookTitle;      // 제목
    private String content;        // 내용
    private String coverImageUrl;  // 표지
    private String createdAt;      // 저장날짜 (yyyy-MM-dd)
    
    // LocalDateTime을 받아서 포맷된 문자열로 설정하는 메서드
    public void setCreatedAt(LocalDateTime dateTime) {
        if (dateTime != null) {
            this.createdAt = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }
}
