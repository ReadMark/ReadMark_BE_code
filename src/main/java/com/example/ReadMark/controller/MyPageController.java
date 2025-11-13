package com.example.ReadMark.controller;

import com.example.ReadMark.model.dto.FavoritePageDTO;
import com.example.ReadMark.model.dto.FavoriteQuoteDTO;
import com.example.ReadMark.model.dto.UserStatsDTO;
import com.example.ReadMark.service.MyPageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/mypage")
@RequiredArgsConstructor
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH}, allowedHeaders = "*", allowCredentials = "false")
public class MyPageController {
    
    private final MyPageService myPageService;
    
    @GetMapping("/user/{userId}/stats")
    public ResponseEntity<?> getUserStats(@PathVariable Long userId) {
        try {
            UserStatsDTO stats = myPageService.getUserStats(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("data", stats);  // stats를 data로 변경하여 통일
            response.put("message", "사용자 통계 조회 성공");
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
            response.put("favoritePages", favoritePages);  // 프론트엔드 호환성을 위해 원래 필드명 유지
            response.put("data", favoritePages);  // 통일된 구조를 위해 data 필드도 추가
            response.put("message", "즐겨찾기한 페이지 조회 성공");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "즐겨찾기한 페이지 조회에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    
    @PostMapping("/user/{userId}/favorite-page")
    public ResponseEntity<?> createFavoritePage(@PathVariable Long userId,
                                             @RequestParam Long bookId,
                                             @RequestParam int pageNumber) {
        try {
            var favoritePage = myPageService.createFavoritePage(userId, bookId, pageNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "즐겨찾기한 페이지가 저장되었습니다.");
            response.put("favoritePageId", favoritePage.getFavPageId());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "즐겨찾기한 페이지 저장에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    
    @PutMapping("/favorite-page/{favPageId}")
    public ResponseEntity<?> updateFavoritePage(@PathVariable Long favPageId,
                                             @RequestParam(required = false) Integer pageNumber) {
        try {
            var favoritePage = myPageService.updateFavoritePage(favPageId, pageNumber);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "즐겨찾기한 페이지가 수정되었습니다.");
            response.put("favoritePage", favoritePage);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "즐겨찾기한 페이지 수정에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    
    @DeleteMapping("/favorite-page/{favPageId}")
    public ResponseEntity<?> deleteFavoritePage(@PathVariable Long favPageId) {
        try {
            myPageService.deleteFavoritePage(favPageId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "즐겨찾기한 페이지가 삭제되었습니다.");
            response.put("deletedFavPageId", favPageId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "즐겨찾기한 페이지 삭제에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    // ========== 즐겨찾기 문장 관련 API ==========
    
    @GetMapping("/user/{userId}/favorite-quotes")
    public ResponseEntity<?> getFavoriteQuotes(@PathVariable Long userId) {
        try {
            List<FavoriteQuoteDTO> favoriteQuotes = myPageService.getFavoriteQuotes(userId);
            
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("favoriteQuotes", favoriteQuotes);  // 프론트엔드 호환성을 위해 원래 필드명 유지
            response.put("data", favoriteQuotes);  // 통일된 구조를 위해 data 필드도 추가
            response.put("message", "즐겨찾기한 문장 조회 성공");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "즐겨찾기한 문장 조회에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping(value = "/user/{userId}/favorite-quote", consumes = "multipart/form-data")
    public ResponseEntity<?> createFavoriteQuote(@PathVariable Long userId,
                                               @RequestParam Integer pageNumber,
                                               @RequestParam String content,
                                               @RequestParam String bookTitle,
                                               @RequestParam(required = false) MultipartFile coverImage,
                                               @RequestParam(required = false) String coverImageUrl) {
        try {
            String finalCoverImageUrl = null;
            
            // 1. 파일 업로드가 있으면 파일을 저장
            if (coverImage != null && !coverImage.isEmpty()) {
                finalCoverImageUrl = saveCoverImage(coverImage, userId);
            }
            // 2. coverImageUrl 파라미터가 있으면 그것을 사용
            else if (coverImageUrl != null && !coverImageUrl.trim().isEmpty()) {
                finalCoverImageUrl = coverImageUrl.trim();
            }
            
            var favoriteQuote = myPageService.createFavoriteQuote(userId, pageNumber, content, bookTitle, finalCoverImageUrl);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "즐겨찾기한 문장이 저장되었습니다.");
            response.put("favoriteQuoteId", favoriteQuote.getFavQuoteId());
            response.put("coverImageUrl", finalCoverImageUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "즐겨찾기한 문장 저장에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PutMapping(value = "/favorite-quote/{favQuoteId}", consumes = "multipart/form-data")
    public ResponseEntity<?> updateFavoriteQuote(@PathVariable Long favQuoteId,
                                               @RequestParam(required = false) Integer pageNumber,
                                               @RequestParam(required = false) String content,
                                               @RequestParam(required = false) String bookTitle,
                                               @RequestParam(required = false) MultipartFile coverImage,
                                               @RequestParam(required = false) String coverImageUrl) {
        try {
            String finalCoverImageUrl = null;
            
            // 1. 파일 업로드가 있으면 파일을 저장
            if (coverImage != null && !coverImage.isEmpty()) {
                // 기존 즐겨찾기 문장 정보 조회 (userId 필요)
                var existingQuote = myPageService.getFavoriteQuoteById(favQuoteId);
                if (existingQuote != null) {
                    finalCoverImageUrl = saveCoverImage(coverImage, existingQuote.getUser().getUserId());
                }
            }
            // 2. coverImageUrl 파라미터가 있으면 그것을 사용
            else if (coverImageUrl != null && !coverImageUrl.trim().isEmpty()) {
                finalCoverImageUrl = coverImageUrl.trim();
            }
            
            var favoriteQuote = myPageService.updateFavoriteQuote(favQuoteId, pageNumber, content, bookTitle, finalCoverImageUrl);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "즐겨찾기한 문장이 수정되었습니다.");
            response.put("favoriteQuote", favoriteQuote);
            response.put("coverImageUrl", finalCoverImageUrl);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "즐겨찾기한 문장 수정에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @DeleteMapping("/favorite-quote/{favQuoteId}")
    public ResponseEntity<?> deleteFavoriteQuote(@PathVariable Long favQuoteId) {
        try {
            myPageService.deleteFavoriteQuote(favQuoteId);
            Map<String, Object> response = new HashMap<>();
            response.put("success", true);
            response.put("message", "즐겨찾기한 문장이 삭제되었습니다.");
            response.put("deletedFavQuoteId", favQuoteId);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "즐겨찾기한 문장 삭제에 실패했습니다: " + e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 책 표지 이미지 저장
     */
    private String saveCoverImage(MultipartFile coverImage, Long userId) throws IOException {
        // 업로드 디렉토리 생성
        String uploadDir = "uploads/favorite-quote-covers/";
        Path uploadPath = Paths.get(uploadDir);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 파일명 생성 (UUID + 원본 확장자)
        String originalFilename = coverImage.getOriginalFilename();
        String fileExtension = originalFilename != null ? 
            originalFilename.substring(originalFilename.lastIndexOf(".")) : ".jpg";
        String filename = "user_" + userId + "_" + UUID.randomUUID().toString() + fileExtension;
        
        // 파일 저장
        Path filePath = uploadPath.resolve(filename);
        Files.copy(coverImage.getInputStream(), filePath);
        
        // URL 반환
        return "/uploads/favorite-quote-covers/" + filename;
    }
}
