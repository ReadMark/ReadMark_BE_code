package com.example.ReadMark.controller;

import com.example.ReadMark.constant.ResponseMessage;
import com.example.ReadMark.model.dto.ApiResponse;
import com.example.ReadMark.model.dto.UserBookDTO;
import com.example.ReadMark.model.entity.UserBook;
import com.example.ReadMark.model.entity.UserBook.Status;
import com.example.ReadMark.service.UserBookService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/userbooks")
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
public class UserBookController {
    
    private final UserBookService userBookService;
    
    @PostMapping
    public ResponseEntity<ApiResponse<UserBook>> addBookToUser(@RequestBody Map<String, Object> request) {
        try {
            Long userId = Long.valueOf(request.get("userId").toString());
            Long bookId = Long.valueOf(request.get("bookId").toString());
            String statusStr = request.get("status").toString();
            Integer currentPage = request.get("currentPage") != null ? 
                Integer.valueOf(request.get("currentPage").toString()) : 0;
            
            Status bookStatus = Status.valueOf(statusStr.toUpperCase());
            UserBook userBook = userBookService.addBookToUser(userId, bookId, bookStatus, currentPage);
            
            return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USERBOOK_ADD_SUCCESS, userBook));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ResponseMessage.USERBOOK_ADD_FAIL + e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<List<UserBookDTO>>> getUserBooks(@PathVariable Long userId) {
        try {
            List<UserBookDTO> userBooks = userBookService.getAllUserBooks(userId);
            return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USERBOOK_LIST_SUCCESS, userBooks));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ResponseMessage.USERBOOK_LIST_FAIL + e.getMessage()));
        }
    }
    
    @GetMapping("/user/{userId}/status/{status}")
    public ResponseEntity<ApiResponse<List<UserBookDTO>>> getUserBooksByStatus(@PathVariable Long userId, 
                                               @PathVariable String status) {
        try {
            Status bookStatus = Status.valueOf(status.toUpperCase());
            List<UserBookDTO> userBooks = userBookService.getUserBooksByStatus(userId, bookStatus);
            
            return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USERBOOK_LIST_SUCCESS, userBooks));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ResponseMessage.USERBOOK_LIST_FAIL + e.getMessage()));
        }
    }
    
    @PutMapping("/{userBookId}/status")
    public ResponseEntity<ApiResponse<UserBook>> updateBookStatus(@PathVariable Long userBookId, 
                                           @RequestBody Map<String, Object> request) {
        try {
            String statusStr = request.get("status").toString();
            Status bookStatus = Status.valueOf(statusStr.toUpperCase());
            UserBook userBook = userBookService.updateBookStatus(userBookId, bookStatus);
            
            return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USERBOOK_STATUS_UPDATE_SUCCESS, userBook));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ResponseMessage.USERBOOK_STATUS_UPDATE_FAIL + e.getMessage()));
        }
    }
    
    @PutMapping("/{userBookId}/page")
    public ResponseEntity<ApiResponse<UserBook>> updateCurrentPage(@PathVariable Long userBookId, 
                                            @RequestBody Map<String, Object> request) {
        try {
            Integer currentPage = Integer.valueOf(request.get("currentPage").toString());
            UserBook userBook = userBookService.updateCurrentPage(userBookId, currentPage);
            
            return ResponseEntity.ok(ApiResponse.success(ResponseMessage.USERBOOK_PAGE_UPDATE_SUCCESS, userBook));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(ResponseMessage.USERBOOK_PAGE_UPDATE_FAIL + e.getMessage()));
        }
    }
}
