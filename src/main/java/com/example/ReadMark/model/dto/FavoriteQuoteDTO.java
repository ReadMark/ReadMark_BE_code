package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FavoriteQuoteDTO {
    private Long favQuoteId;
    private BookDTO book;
    private Integer pageNumber;
    private String content;
    private LocalDateTime createdAt;
}
