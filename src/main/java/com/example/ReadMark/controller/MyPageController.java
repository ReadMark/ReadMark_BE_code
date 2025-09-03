package com.example.ReadMark.controller;

import com.example.ReadMark.model.dto.FavoritePageDTO;
import com.example.ReadMark.model.dto.FavoriteQuoteDTO;
import com.example.ReadMark.model.dto.UserStatsDTO;
import com.example.ReadMark.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
@CrossOrigin(origins = "*") // CORS 설정
public class MyPageController {
    
    private final MyPageService myPageService;
    
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<?> getUserStats(@PathVariable Long userId) {
        try {
            UserStatsDTO stats = myPageService.getUserStats(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("stats", stats);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "사용자 통계 조회에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/user/{userId}/favorite-pages")
    public ResponseEntity<?> getFavoritePages(@PathVariable Long userId) {
        try {
            List<FavoritePageDTO> favoritePages = myPageService.getFavoritePages(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("favoritePages", favoritePages);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "즐겨찾기한 페이지 조회에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @GetMapping("/user/{userId}/favorite-quotes")
    public ResponseEntity<?> getFavoriteQuotes(@PathVariable Long userId) {
        try {
            List<FavoriteQuoteDTO> favoriteQuotes = myPageService.getFavoriteQuotes(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("favoriteQuotes", favoriteQuotes);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "즐겨찾기한 문장 조회에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/user/{userId}/favorite-page")
    public ResponseEntity<?> createFavoritePage(@PathVariable Long userId,
                                             @RequestParam Long bookId,
                                             @RequestParam int pageNumber) {
        try {
            // 실제 구현에서는 MyPageService의 createFavoritePage 메서드 완성 필요
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "즐겨찾기한 페이지가 저장되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "즐겨찾기한 페이지 저장에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/user/{userId}/favorite-quote")
    public ResponseEntity<?> createFavoriteQuote(@PathVariable Long userId,
                                              @RequestParam Long bookId,
                                              @RequestParam(required = false) Integer pageNumber,
                                              @RequestParam String content) {
        try {
            // 실제 구현에서는 MyPageService의 createFavoriteQuote 메서드 완성 필요
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "즐겨찾기한 문장이 저장되었습니다.");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "즐겨찾기한 문장 저장에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
