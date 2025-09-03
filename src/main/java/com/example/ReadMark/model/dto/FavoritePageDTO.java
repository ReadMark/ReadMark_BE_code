package com.example.ReadMark.model.dto;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class FavoritePageDTO {
    private Long favPageId;
    private BookDTO book;
    private int pageNumber;
    private LocalDateTime createdAt;
}
