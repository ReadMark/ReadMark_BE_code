package com.example.ReadMark.service;

import com.example.ReadMark.entity.Book;
import com.example.ReadMark.model.dto.BookDTO;
import com.example.ReadMark.repository.BookRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class BookService {

    @Autowired
    private BookRepository bookRepository;

    // 책 생성
    public Book createBook(BookDTO dto) {
        Book book = new Book();
        book.setTitle(dto.getTitle());
        book.setAuthor(dto.getAuthor());
        book.setPublisher(dto.getPublisher());
        book.setCoverImageUrl(dto.getCoverImageUrl());
        book.setPublishedAt(dto.getPublishedAt());
        return bookRepository.save(book);
    }

    // 전체 책 조회
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    // 책 업데이트
    public Book updateBook(Long id, BookDTO dto) {
        Optional<Book> optionalBook = bookRepository.findById(id);
        if (optionalBook.isPresent()) {
            Book book = optionalBook.get();
            book.setTitle(dto.getTitle());
            book.setAuthor(dto.getAuthor());
            book.setPublisher(dto.getPublisher());
            book.setCoverImageUrl(dto.getCoverImageUrl());
            book.setPublishedAt(dto.getPublishedAt());
            return bookRepository.save(book);
        }
        return null;
    }

    // 책 삭제
    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }
}
