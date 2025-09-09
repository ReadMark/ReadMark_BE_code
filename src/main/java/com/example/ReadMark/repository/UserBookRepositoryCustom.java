package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.UserBook;
import com.example.ReadMark.model.entity.UserBook.Status;

import java.util.List;

public interface UserBookRepositoryCustom {
    List<UserBook> findByUser_UserIdAndStatus(Long userId, Status status);
    List<UserBook> findByUserId(Long userId);
    long countByUserIdAndStatus(Long userId, Status status);
    List<UserBook> findUserBooksWithBookInfo(Long userId);
    List<UserBook> findUserBooksWithBookInfo(Long userId, Status status);
    List<UserBook> findUserBooksByCurrentPageRange(Long userId, int minPage, int maxPage);
    List<UserBook> findUserBooksByStatusAndPageRange(Long userId, Status status, int minPage, int maxPage);
    List<UserBook> findUserBooksWithPagination(Long userId, int offset, int limit);
}
