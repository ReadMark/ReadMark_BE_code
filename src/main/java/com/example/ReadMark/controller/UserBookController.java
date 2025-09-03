package com.example.ReadMark.controller;

import com.example.ReadMark.model.dto.UserBookDTO;
import com.example.ReadMark.model.entity.UserBook;
import com.example.ReadMark.model.entity.UserBook.Status;
import com.example.ReadMark.service.UserBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/userbooks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // CORS 설정
public class UserBookController {
    
    private final UserBookService userBookService;
    
    @PostMapping
    public ResponseEntity<?> addBookToUser(@RequestParam Long userId, 
                                         @RequestParam Long bookId, 
                                         @RequestParam String status) {
        try {
            Status bookStatus = Status.valueOf(status.toUpperCase());
            UserBook userBook = userBookService.addBookToUser(userId, bookId, bookStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "책이 추가되었습니다.");
            response.put("userBookId", userBook.getUserBookId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "책 추가에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<?> getUserBooks(@PathVariable Long userId) {
        try {
            List<UserBookDTO> userBooks = userBookService.getAllUserBooks(userId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userBooks", userBooks);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "사용자 책 목록 조회에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<?> getUserBooksByStatus(@PathVariable Long userId, 
                                               @PathVariable String status) {
        try {
            Status bookStatus = Status.valueOf(status.toUpperCase());
            List<UserBookDTO> userBooks = userBookService.getUserBooksByStatus(userId, bookStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("userBooks", userBooks);
            response.put("status", status);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "상태별 책 목록 조회에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{userBookId}/status")
    public ResponseEntity<?> updateBookStatus(@PathVariable Long userBookId, 
                                           @RequestParam String status) {
        try {
            Status bookStatus = Status.valueOf(status.toUpperCase());
            UserBook userBook = userBookService.updateBookStatus(userBookId, bookStatus);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "책 상태가 업데이트되었습니다.");
            response.put("userBookId", userBook.getUserBookId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "책 상태 업데이트에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping("/{userBookId}/page")
    public ResponseEntity<?> updateCurrentPage(@PathVariable Long userBookId, 
                                            @RequestParam int currentPage) {
        try {
            UserBook userBook = userBookService.updateCurrentPage(userBookId, currentPage);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "현재 페이지가 업데이트되었습니다.");
            response.put("userBookId", userBook.getUserBookId());
            response.put("currentPage", userBook.getCurrentPage());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "현재 페이지 업데이트에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
