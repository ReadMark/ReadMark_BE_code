package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BookDTO {
    private Long bookId;
    private String title;
    private String author;
    private String coverImageUrl;
    private Integer totalBook;  // 책의 총 페이지 수
}
