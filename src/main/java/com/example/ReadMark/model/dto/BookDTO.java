package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class BookDTO {
    private Long bookId;
    private String title;
    private String author;
    private String publisher;
    private String coverImageUrl;
    private LocalDate publishedAt;
}
