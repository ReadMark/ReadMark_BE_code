package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.Book;

import java.util.List;

public interface BookRepositoryCustom {
    List<Book> findBooksByKeyword(String keyword);
}
