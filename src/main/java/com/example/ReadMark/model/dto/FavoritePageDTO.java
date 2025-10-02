package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Getter
@Setter
public class FavoritePageDTO {
    private Long favPageId;
    private String bookTitle;      // 책 제목
    private String author;         // 작가
    private int pageNumber;        // 페이지 수
    private String coverImageUrl;  // 표지 이미지
    private String createdAt;      // 날짜 (yyyy-MM-dd)
    
    // LocalDateTime을 받아서 포맷된 문자열로 설정하는 메서드
    public void setCreatedAt(LocalDateTime dateTime) {
        if (dateTime != null) {
            this.createdAt = dateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));
        }
    }
}
