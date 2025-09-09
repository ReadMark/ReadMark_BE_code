package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.Book;

import java.time.LocalDate;
import java.util.List;

public interface BookRepositoryCustom {
    List<Book> findBooksByKeyword(String keyword);
    List<Book> findBooksByAuthor(String author);
    List<Book> findBooksByPublisher(String publisher);
    List<Book> findBooksByPublishedDateRange(LocalDate startDate, LocalDate endDate);
    List<Book> findBooksByTitleContaining(String title);
    List<Book> findBooksWithPagination(int offset, int limit);
}
