package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {
    
    Optional<Book> findByTitleAndAuthor(String title, String author);
}
