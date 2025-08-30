package com.example.ReadMark.model.dto;

import com.example.ReadMark.entity.UserBook.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserBookDTO {
    private Long userId;
    private Long bookId;
    private Status status;
    private int currentPage;
}
