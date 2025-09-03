package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.Book;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface BookRepository extends JpaRepository<Book, Long>, BookRepositoryCustom {
    
    Optional<Book> findByTitleAndAuthor(String title, String author);
    
    @Query("SELECT b FROM Book b WHERE b.title LIKE %:keyword% OR b.author LIKE %:keyword%")
    List<Book> findByTitleOrAuthorContaining(@Param("keyword") String keyword);
}
