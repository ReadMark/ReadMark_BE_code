package com.example.ReadMark.service;

import com.example.ReadMark.model.dto.BookDTO;
import com.example.ReadMark.model.entity.Book;
import com.example.ReadMark.repository.BookRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {
    
    private final BookRepository bookRepository;
    
    public Book createBook(BookDTO bookDTO) {
        Book book = new Book();
        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setPublisher(bookDTO.getPublisher());
        book.setCoverImageUrl(bookDTO.getCoverImageUrl());
        book.setPublishedAt(bookDTO.getPublishedAt());
        
        return bookRepository.save(book);
    }
    
    public List<BookDTO> searchBooks(String keyword) {
        List<Book> books = bookRepository.findBooksByKeyword(keyword);
        return books.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public Optional<Book> findById(Long bookId) {
        return bookRepository.findById(bookId);
    }
    
    public Optional<Book> findByTitleAndAuthor(String title, String author) {
        return bookRepository.findByTitleAndAuthor(title, author);
    }
    
    public BookDTO convertToDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setBookId(book.getBookId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setPublisher(book.getPublisher());
        dto.setCoverImageUrl(book.getCoverImageUrl());
        dto.setPublishedAt(book.getPublishedAt());
        return dto;
    }
}
