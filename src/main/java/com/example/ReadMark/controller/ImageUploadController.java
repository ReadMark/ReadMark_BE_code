package com.example.ReadMark.controller;

import com.example.ReadMark.model.dto.BookPageDTO;
import com.example.ReadMark.model.dto.VisionAnalysisResultDTO;
import com.example.ReadMark.service.BookPageService;
import com.example.ReadMark.service.GoogleVisionService;
import com.example.ReadMark.service.ReadingSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/image")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = {"http://localhost:3000", "http://localhost:8080"})
public class ImageUploadController {
    
    private final GoogleVisionService visionService;
    private final ReadingSessionService readingSessionService;
    private final BookPageService bookPageService;
    
    /**
     * 임베디드 기기에서 책 페이지 이미지를 업로드하고 DB에 저장합니다.
     */
    @PostMapping("/upload")
    public ResponseEntity<?> uploadBookImage(
            @RequestParam("userId") Long userId,
            @RequestParam("bookId") Long bookId,
            @RequestParam("image") MultipartFile image,
            @RequestParam(value = "deviceInfo", required = false) String deviceInfo,
            @RequestParam(value = "captureTime", required = false) String captureTime) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 이미지 유효성 검사
            if (image.isEmpty()) {
                response.put("success", false);
                response.put("message", "이미지가 비어있습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 이미지 크기 제한 (10MB)
            if (image.getSize() > 10 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "이미지 크기가 너무 큽니다. (최대 10MB)");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 이미지 형식 검사
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "올바른 이미지 형식이 아닙니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            log.info("책 페이지 이미지 업로드 시작: 사용자 {}, 책 {}, 크기 {} bytes", 
                    userId, bookId, image.getSize());
            
            // 이미지를 바이트 배열로 변환
            byte[] imageBytes = image.getBytes();
            
            // 책 페이지인지 확인
            if (!visionService.isBookPage(imageBytes)) {
                response.put("success", false);
                response.put("message", "책 페이지로 인식되지 않습니다. 더 명확한 이미지를 촬영해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 캡처 시간 파싱
            LocalDateTime parsedCaptureTime = null;
            if (captureTime != null && !captureTime.trim().isEmpty()) {
                try {
                    DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
                    parsedCaptureTime = LocalDateTime.parse(captureTime, formatter);
                } catch (Exception e) {
                    log.warn("캡처 시간 파싱 실패: {}, 현재 시간 사용", captureTime);
                }
            }
            
            // BookPageService를 통해 페이지 생성 및 DB 저장
            BookPageDTO bookPage = bookPageService.createBookPage(
                userId, bookId, imageBytes, deviceInfo, parsedCaptureTime);
            
            // 독서 세션에 이미지 추가
            readingSessionService.addImageToSession(userId, imageBytes, bookPage.getExtractedText());
            
            // 응답 데이터 구성
            Map<String, Object> pageData = new HashMap<>();
            pageData.put("pageId", bookPage.getPageId());
            pageData.put("pageNumber", bookPage.getPageNumber());
            pageData.put("extractedText", bookPage.getExtractedText());
            pageData.put("confidence", bookPage.getConfidence());
            pageData.put("language", bookPage.getLanguage());
            pageData.put("numberCount", bookPage.getNumberCount());
            pageData.put("textQuality", bookPage.getTextQuality());
            pageData.put("capturedAt", bookPage.getCapturedAt());
            
            response.put("success", true);
            response.put("message", "책 페이지가 성공적으로 저장되었습니다.");
            response.put("page", pageData);
            response.put("deviceInfo", deviceInfo);
            
            log.info("책 페이지 저장 완료: 사용자 {}, 책 {}, 페이지 {}", 
                    userId, bookId, bookPage.getPageNumber());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("책 페이지 업로드 처리 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "책 페이지 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 독서 세션을 시작합니다.
     */
    @PostMapping("/session/start")
    public ResponseEntity<?> startReadingSession(
            @RequestParam("userId") Long userId,
            @RequestParam("bookId") Long bookId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            var session = readingSessionService.startReadingSession(userId, bookId);
            
            response.put("success", true);
            response.put("message", "독서 세션이 시작되었습니다.");
            response.put("sessionId", session.getSessionId());
            response.put("startTime", session.getStartTime());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("독서 세션 시작 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "독서 세션 시작 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 독서 세션을 종료합니다.
     */
    @PostMapping("/session/end")
    public ResponseEntity<?> endReadingSession(@RequestParam("userId") Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            var session = readingSessionService.endReadingSession(userId);
            
            if (session != null) {
                response.put("success", true);
                response.put("message", "독서 세션이 종료되었습니다.");
                response.put("totalPagesRead", session.getTotalPagesRead());
                response.put("totalNumbersRead", session.getTotalNumbersRead());
                response.put("readingDurationMinutes", session.getReadingDurationMinutes());
                response.put("endTime", session.getEndTime());
            } else {
                response.put("success", false);
                response.put("message", "활성 독서 세션이 없습니다.");
            }
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("독서 세션 종료 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "독서 세션 종료 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 독서 통계를 조회합니다.
     */
    @GetMapping("/stats/{userId}")
    public ResponseEntity<?> getReadingStats(@PathVariable Long userId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("maxConsecutiveDays", readingSessionService.getMaxConsecutiveReadingDays(userId));
            stats.put("totalReadingDays", readingSessionService.getTotalReadingDays(userId));
            stats.put("currentConsecutiveDays", readingSessionService.getCurrentConsecutiveDays(userId));
            
            // 독서 습관 분석
            Map<String, Object> habitAnalysis = readingSessionService.getReadingHabitAnalysis(userId);
            stats.put("habitAnalysis", habitAnalysis);
            
            response.put("success", true);
            response.put("stats", stats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("독서 통계 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "독서 통계 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 월별 독서 통계를 조회합니다.
     */
    @GetMapping("/stats/{userId}/monthly")
    public ResponseEntity<?> getMonthlyReadingStats(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "6") int months) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> monthlyStats = readingSessionService.getMonthlyReadingStats(userId, months);
            
            response.put("success", true);
            response.put("monthlyStats", monthlyStats);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("월별 독서 통계 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "월별 독서 통계 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    // ========== BookPage 관련 API ==========
    
    /**
     * 특정 책의 페이지 목록을 조회합니다.
     */
    @GetMapping("/pages/{userId}/{bookId}")
    public ResponseEntity<?> getBookPages(
            @PathVariable Long userId,
            @PathVariable Long bookId) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            var pages = bookPageService.getBookPages(userId, bookId);
            Long totalPages = bookPageService.getTotalPageCount(userId, bookId);
            
            response.put("success", true);
            response.put("pages", pages);
            response.put("totalPages", totalPages);
            response.put("count", pages.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("책 페이지 목록 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "책 페이지 목록 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 특정 페이지를 조회합니다.
     */
    @GetMapping("/pages/{userId}/{bookId}/{pageNumber}")
    public ResponseEntity<?> getBookPage(
            @PathVariable Long userId,
            @PathVariable Long bookId,
            @PathVariable Integer pageNumber) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            var page = bookPageService.getBookPage(userId, bookId, pageNumber);
            
            response.put("success", true);
            response.put("page", page);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("책 페이지 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "책 페이지 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 페이지 범위로 조회합니다.
     */
    @GetMapping("/pages/{userId}/{bookId}/range")
    public ResponseEntity<?> getBookPagesByRange(
            @PathVariable Long userId,
            @PathVariable Long bookId,
            @RequestParam Integer startPage,
            @RequestParam Integer endPage) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            var pages = bookPageService.getBookPagesByRange(userId, bookId, startPage, endPage);
            
            response.put("success", true);
            response.put("pages", pages);
            response.put("count", pages.size());
            response.put("range", startPage + " ~ " + endPage);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("책 페이지 범위 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "책 페이지 범위 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 최근 페이지들을 조회합니다.
     */
    @GetMapping("/pages/{userId}/{bookId}/recent")
    public ResponseEntity<?> getRecentPages(
            @PathVariable Long userId,
            @PathVariable Long bookId,
            @RequestParam(defaultValue = "10") int limit) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            var pages = bookPageService.getRecentPages(userId, bookId, limit);
            
            response.put("success", true);
            response.put("pages", pages);
            response.put("count", pages.size());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("최근 책 페이지 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "최근 책 페이지 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 특정 페이지를 삭제합니다.
     */
    @DeleteMapping("/pages/{userId}/{bookId}/{pageNumber}")
    public ResponseEntity<?> deleteBookPage(
            @PathVariable Long userId,
            @PathVariable Long bookId,
            @PathVariable Integer pageNumber) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            bookPageService.deleteBookPage(userId, bookId, pageNumber);
            
            response.put("success", true);
            response.put("message", "페이지가 삭제되었습니다.");
            response.put("deletedPage", pageNumber);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("책 페이지 삭제 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "책 페이지 삭제 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
