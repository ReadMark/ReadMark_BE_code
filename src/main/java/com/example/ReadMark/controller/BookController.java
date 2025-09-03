package com.example.ReadMark.controller;

import com.example.ReadMark.model.dto.BookDTO;
import com.example.ReadMark.model.entity.Book;
import com.example.ReadMark.service.BookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/books")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // CORS 설정
public class BookController {
    
    private final BookService bookService;
    
    @PostMapping
    public ResponseEntity<?> createBook(@RequestBody BookDTO bookDTO) {
        try {
            Book book = bookService.createBook(bookDTO);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "책이 등록되었습니다.");
            response.put("bookId", book.getBookId());
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
    
    @GetMapping("/{bookId}")
    public ResponseEntity<?> getBook(@PathVariable Long bookId) {
        // 실제 구현에서는 BookService에 findById 메서드 추가 필요
        return ResponseEntity.ok().build();
    }
}
