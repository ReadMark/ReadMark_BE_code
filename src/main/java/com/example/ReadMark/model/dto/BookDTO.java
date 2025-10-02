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
}
