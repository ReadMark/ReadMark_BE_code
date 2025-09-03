package com.example.ReadMark.service;

import com.example.ReadMark.model.dto.UserBookDTO;
import com.example.ReadMark.model.entity.Book;
import com.example.ReadMark.model.entity.User;
import com.example.ReadMark.model.entity.UserBook;
import com.example.ReadMark.model.entity.UserBook.Status;
import com.example.ReadMark.repository.BookRepository;
import com.example.ReadMark.repository.UserBookRepository;
import com.example.ReadMark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserBookService {
    
    private final UserBookRepository userBookRepository;
    private final UserRepository userRepository;
    private final BookRepository bookRepository;
    
    public UserBook addBookToUser(Long userId, Long bookId, Status status) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));
        
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다."));
        
        UserBook userBook = new UserBook();
        userBook.setUser(user);
        userBook.setBook(book);
        userBook.setStatus(status);
        userBook.setCurrentPage(0);
        
        return userBookRepository.save(userBook);
    }
    
    public List<UserBookDTO> getUserBooksByStatus(Long userId, Status status) {
        List<UserBook> userBooks = userBookRepository.findUserBooksWithBookInfo(userId, status);
        return userBooks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public List<UserBookDTO> getAllUserBooks(Long userId) {
        List<UserBook> userBooks = userBookRepository.findUserBooksWithBookInfo(userId);
        return userBooks.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }
    
    public UserBook updateBookStatus(Long userBookId, Status status) {
        UserBook userBook = userBookRepository.findById(userBookId)
                .orElseThrow(() -> new RuntimeException("사용자 책을 찾을 수 없습니다."));
        
        userBook.setStatus(status);
        return userBookRepository.save(userBook);
    }
    
    public UserBook updateCurrentPage(Long userBookId, int currentPage) {
        UserBook userBook = userBookRepository.findById(userBookId)
                .orElseThrow(() -> new RuntimeException("사용자 책을 찾을 수 없습니다."));
        
        userBook.setCurrentPage(currentPage);
        return userBookRepository.save(userBook);
    }
    
    public UserBookDTO convertToDTO(UserBook userBook) {
        UserBookDTO dto = new UserBookDTO();
        dto.setUserBookId(userBook.getUserBookId());
        dto.setStatus(userBook.getStatus().name());
        dto.setCurrentPage(userBook.getCurrentPage());
        dto.setCreatedAt(userBook.getCreatedAt());
        dto.setUpdatedAt(userBook.getUpdatedAt());
        
        // Book 정보도 포함
        BookDTO bookDTO = new BookDTO();
        bookDTO.setBookId(userBook.getBook().getBookId());
        bookDTO.setTitle(userBook.getBook().getTitle());
        bookDTO.setAuthor(userBook.getBook().getAuthor());
        bookDTO.setPublisher(userBook.getBook().getPublisher());
        bookDTO.setCoverImageUrl(userBook.getBook().getCoverImageUrl());
        bookDTO.setPublishedAt(userBook.getBook().getPublishedAt());
        
        dto.setBook(bookDTO);
        return dto;
    }
}
