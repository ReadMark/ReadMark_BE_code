package com.example.ReadMark.controller;

import com.example.ReadMark.model.dto.BookDTO;
import com.example.ReadMark.model.entity.Book;
import com.example.ReadMark.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH}, allowedHeaders = "*", allowCredentials = "false")
public class BookController {
    
    private final BookService bookService;
    
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<?> createBook(@RequestParam("title") String title,
                                       @RequestParam("author") String author,
                                       @RequestParam("coverImage") MultipartFile coverImage,
                                       @RequestParam(value = "totalBook", required = false) Integer totalBook,
                                       @RequestParam(value = "total_book", required = false) Integer totalBookSnake) {
        try {
            // 필수 필드 검증
            if (title == null || title.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "책 제목은 필수입니다.");
                return ResponseEntity.badRequest().body(response);
            }
            if (author == null || author.trim().isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "작가명은 필수입니다.");
                return ResponseEntity.badRequest().body(response);
            }
            if (coverImage == null || coverImage.isEmpty()) {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "책 표지는 필수입니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // totalBook 처리 (camelCase와 snake_case 둘 다 지원)
            Integer finalTotalBook = totalBook != null ? totalBook : totalBookSnake;
            
            Book book = bookService.createBookWithCover(title, author, coverImage, finalTotalBook);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "책이 등록되었습니다.");
            response.put("bookId", book.getBookId());
            response.put("coverImageUrl", book.getCoverImageUrl());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "책 등록에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<?> searchBooks(@RequestParam String keyword) {
        try {
            List<BookDTO> books = bookService.searchBooks(keyword);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("books", books);
            response.put("count", books.size());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "책 검색에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping
    public ResponseEntity<?> getAllBooks() {
        try {
            List<BookDTO> books = bookService.getAllBooks();
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("books", books);
            response.put("totalBooks", books.size());  // 목록 조회 시 totalBooks 사용
            response.put("count", books.size());
            response.put("message", "책 목록 조회 성공");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "책 목록 조회에 실패했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @GetMapping("/{bookId}")
    public ResponseEntity<?> getBook(@PathVariable Long bookId) {
        try {
            Optional<Book> bookOpt = bookService.findById(bookId);
            if (bookOpt.isPresent()) {
                BookDTO bookDTO = bookService.convertToDTO(bookOpt.get());
                Map<String, Object> response = new HashMap<>();
                response.put("success", true);
                response.put("book", bookDTO);
                response.put("message", "책 정보 조회 성공");
                return ResponseEntity.ok(response);
            } else {
                Map<String, Object> response = new HashMap<>();
                response.put("success", false);
                response.put("message", "책을 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "책 정보 조회에 실패했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    @PutMapping("/{bookId}")
    public ResponseEntity<?> updateBook(@PathVariable Long bookId, @RequestBody BookDTO bookDTO) {
        try {
            BookDTO updatedBook = bookService.updateBook(bookId, bookDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "책 정보가 수정되었습니다.");
            response.put("book", updatedBook);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "책 정보 수정에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/{bookId}")
    public ResponseEntity<?> deleteBook(@PathVariable Long bookId) {
        try {
            bookService.deleteBook(bookId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "책이 삭제되었습니다.");
            response.put("deletedBookId", bookId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "책 삭제에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 책 표지를 업로드합니다.
     */
    @PostMapping(value = "/{bookId}/cover", consumes = "multipart/form-data")
    public ResponseEntity<?> uploadBookCover(@PathVariable Long bookId, 
                                           @RequestParam("coverImage") MultipartFile coverImage) {
        Map<String, Object> response = new HashMap<>();
        try {
            if (coverImage.isEmpty()) {
                response.put("success", false);
                response.put("message", "표지 이미지가 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 파일 크기 제한 (20MB)
            if (coverImage.getSize() > 20 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "표지 이미지 크기는 20MB를 초과할 수 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 이미지 타입 검증
            String contentType = coverImage.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "이미지 파일만 업로드 가능합니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            String coverImageUrl = bookService.uploadBookCover(bookId, coverImage);
            
            response.put("success", true);
            response.put("message", "책 표지가 업로드되었습니다.");
            response.put("coverImageUrl", coverImageUrl);
            response.put("bookId", bookId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "책 표지 업로드 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 책 표지를 삭제합니다.
     */
    @DeleteMapping("/{bookId}/cover")
    public ResponseEntity<?> deleteBookCover(@PathVariable Long bookId) {
        Map<String, Object> response = new HashMap<>();
        try {
            bookService.deleteBookCover(bookId);
            
            response.put("success", true);
            response.put("message", "책 표지가 삭제되었습니다.");
            response.put("bookId", bookId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "책 표지 삭제 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
}
