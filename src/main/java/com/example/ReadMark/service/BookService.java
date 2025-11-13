package com.example.ReadMark.service;

import com.example.ReadMark.model.dto.BookDTO;
import com.example.ReadMark.model.entity.Book;
import com.example.ReadMark.model.entity.User;
import com.example.ReadMark.model.entity.UserBook;
import com.example.ReadMark.repository.BookRepository;
import com.example.ReadMark.repository.UserBookRepository;
import com.example.ReadMark.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class BookService {

    private final BookRepository bookRepository;
    private final UserBookRepository userBookRepository;
    private final UserRepository userRepository;

    public Book createBook(BookDTO bookDTO) {
        Book book = new Book();
        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setCoverImageUrl(bookDTO.getCoverImageUrl());

        return bookRepository.save(book);
    }

    /**
     * 책과 표지를 한 번에 등록합니다.
     */
    public Book createBookWithCover(String title, String author, MultipartFile coverImage, Integer totalBook) throws IOException {
        // 책 먼저 생성
        Book book = new Book();
        book.setTitle(title);
        book.setAuthor(author);
        if (totalBook != null && totalBook > 0) {
            book.setTotalBook(totalBook);
        }
        Book savedBook = bookRepository.save(book);

        // 표지 업로드
        String coverImageUrl = uploadBookCover(savedBook.getBookId(), coverImage);
        savedBook.setCoverImageUrl(coverImageUrl);

        return bookRepository.save(savedBook);
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

    public List<BookDTO> getAllBooks() {
        List<Book> books = bookRepository.findAll();
        return books.stream()
                .map(this::convertToDTO)
                .collect(Collectors.toList());
    }

    public BookDTO updateBook(Long bookId, BookDTO bookDTO) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다."));

        book.setTitle(bookDTO.getTitle());
        book.setAuthor(bookDTO.getAuthor());
        book.setCoverImageUrl(bookDTO.getCoverImageUrl());
        if (bookDTO.getTotalBook() != null) {
            book.setTotalBook(bookDTO.getTotalBook());
        }

        Book updatedBook = bookRepository.save(book);
        return convertToDTO(updatedBook);
    }

    public void deleteBook(Long bookId) {
        if (!bookRepository.existsById(bookId)) {
            throw new RuntimeException("책을 찾을 수 없습니다.");
        }
        bookRepository.deleteById(bookId);
    }

    public BookDTO convertToDTO(Book book) {
        BookDTO dto = new BookDTO();
        dto.setBookId(book.getBookId());
        dto.setTitle(book.getTitle());
        dto.setAuthor(book.getAuthor());
        dto.setCoverImageUrl(book.getCoverImageUrl());
        dto.setTotalBook(book.getTotalBook());
        return dto;
    }

    /**
     * 책 표지를 업로드합니다.
     */
    public String uploadBookCover(Long bookId, MultipartFile coverImage) throws IOException {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다."));

        // 업로드 디렉토리 생성
        String uploadDir = "uploads/book-covers";
        Path uploadPath = Paths.get(uploadDir);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }

        // 파일명 생성
        String originalFilename = coverImage.getOriginalFilename();
        String fileExtension = originalFilename != null ?
                originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = "book_" + bookId + "_" + UUID.randomUUID().toString() + fileExtension;

        // 파일 저장
        Path filePath = uploadPath.resolve(filename);
        Files.copy(coverImage.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // URL 생성
        String coverImageUrl = "/uploads/book-covers/" + filename;

        // 데이터베이스 업데이트
        book.setCoverImageUrl(coverImageUrl);
        bookRepository.save(book);

        return coverImageUrl;
    }

    /**
     * 책 표지를 삭제합니다.
     */
    public void deleteBookCover(Long bookId) throws IOException {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다."));

        String coverImageUrl = book.getCoverImageUrl();
        if (coverImageUrl != null && !coverImageUrl.isEmpty()) {
            // 파일 시스템에서 삭제
            String filename = coverImageUrl.substring(coverImageUrl.lastIndexOf("/") + 1);
            Path filePath = Paths.get("uploads/book-covers/" + filename);
            if (Files.exists(filePath)) {
                Files.delete(filePath);
            }

            // 데이터베이스에서 URL 제거
            book.setCoverImageUrl(null);
            bookRepository.save(book);
        }
    }

    /**
     * 책을 즐겨찾기에 추가합니다.
     */
    public void addToFavorites(Long bookId, Long userId) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다."));

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("사용자를 찾을 수 없습니다."));

        // UserBook 관계가 있는지 확인
        Optional<UserBook> userBookOpt = userBookRepository.findByUser_UserIdAndBook_BookId(userId, bookId);

        if (userBookOpt.isPresent()) {
            // 이미 관계가 있으면 즐겨찾기만 설정
            UserBook userBook = userBookOpt.get();
            userBook.setFavorite(true);
            userBookRepository.save(userBook);
        } else {
            // 관계가 없으면 새로 생성
            UserBook userBook = new UserBook();
            userBook.setUser(user);
            userBook.setBook(book);
            userBook.setStatus(UserBook.Status.WANNA_READ);
            userBook.setFavorite(true);
            userBookRepository.save(userBook);
        }
    }

    /**
     * 책을 즐겨찾기에서 제거합니다.
     */
    public void removeFromFavorites(Long bookId, Long userId) {
        Optional<UserBook> userBookOpt = userBookRepository.findByUser_UserIdAndBook_BookId(userId, bookId);

        if (userBookOpt.isPresent()) {
            UserBook userBook = userBookOpt.get();
            userBook.setFavorite(false);
            userBookRepository.save(userBook);
        } else {
            throw new RuntimeException("즐겨찾기에 등록되지 않은 책입니다.");
        }
    }

    /**
     * 사용자의 즐겨찾기 책 목록을 조회합니다.
     */
    public List<BookDTO> getFavoriteBooks(Long userId) {
        List<UserBook> favoriteUserBooks = userBookRepository.findByUser_UserIdAndFavoriteTrue(userId);

        return favoriteUserBooks.stream()
                .map(userBook -> convertToDTO(userBook.getBook()))
                .collect(Collectors.toList());
    }

    /**
     * 책이 즐겨찾기에 있는지 확인합니다.
     */
    public boolean isFavorite(Long bookId, Long userId) {
        Optional<UserBook> userBookOpt = userBookRepository.findByUser_UserIdAndBook_BookId(userId, bookId);
        return userBookOpt.isPresent() && userBookOpt.get().isFavorite();
    }

    /**
     * 책 ID로 책 정보 조회
     */
    public Book getBookById(Long bookId) {
        return bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다: " + bookId));
    }

    /**
     * 책의 총 페이지 수를 업데이트합니다.
     */
    public Book updateBookTotalBook(Long bookId, Integer totalBook) {
        Book book = bookRepository.findById(bookId)
                .orElseThrow(() -> new RuntimeException("책을 찾을 수 없습니다: " + bookId));
        
        if (totalBook != null && totalBook > 0) {
            book.setTotalBook(totalBook);
            return bookRepository.save(book);
        }
        
        return book;
    }


}
