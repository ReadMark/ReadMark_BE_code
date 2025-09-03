package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.UserBook;
import com.example.ReadMark.model.entity.UserBook.Status;

import java.util.List;

public interface UserBookRepositoryCustom {
    List<UserBook> findUserBooksWithBookInfo(Long userId, Status status);
    List<UserBook> findUserBooksWithBookInfo(Long userId);
}
