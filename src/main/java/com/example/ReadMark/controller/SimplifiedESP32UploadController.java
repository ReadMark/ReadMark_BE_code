package com.example.ReadMark.controller;

import com.example.ReadMark.model.dto.BookPageDTO;
import com.example.ReadMark.service.ESP32ReadingSessionService;
import com.example.ReadMark.service.GoogleVisionService;
import com.example.ReadMark.service.BookPageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

// @RestController
@RequestMapping("/api/upload-simplified")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class SimplifiedESP32UploadController {
    
    private final ESP32ReadingSessionService esp32ReadingSessionService;
    private final GoogleVisionService visionService;
    private final BookPageService bookPageService;
    
    /**
     * 단순화된 이미지 업로드 - ESP32 1개인 경우
     */
    @PostMapping("/current-page")
    public ResponseEntity<?> updateCurrentPage(@RequestParam("image") MultipartFile image) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 이미지 유효성 검사
            if (image.isEmpty()) {
                response.put("success", false);
                response.put("message", "이미지 파일이 필요합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            // 이미지 크기 제한 (16MB)
            if (image.getSize() > 16 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "이미지 크기가 너무 큽니다. (최대 16MB)");
                return ResponseEntity.badRequest().body(response);
            }

            // 이미지 타입 검증
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "이미지 파일만 업로드 가능합니다.");
                return ResponseEntity.badRequest().body(response);
            }

            log.info("ESP32 이미지 업로드 시작: 크기 {} bytes", image.getSize());
            byte[] imageBytes = image.getBytes();

            // ESP32가 1개이므로 활성 세션에서 book_id와 user_id 조회
            Long bookId = null;
            Long userId = null;
            
            // 활성 세션에서 첫 번째 세션의 정보 사용 (ESP32 1개이므로)
            for (Map.Entry<String, Long> entry : esp32ReadingSessionService.getAllBookIds().entrySet()) {
                bookId = entry.getValue();
                break; // 첫 번째 세션만 사용
            }
            
            for (Map.Entry<String, Long> entry : esp32ReadingSessionService.getAllUserIds().entrySet()) {
                userId = entry.getValue();
                break; // 첫 번째 세션만 사용
            }

            log.info("활성 세션에서 조회: bookId={}, userId={}", bookId, userId);

            // OCR 처리
            var visionResult = visionService.extractTextFromImage(imageBytes);

            if (!visionResult.isSuccess()) {
                response.put("success", false);
                response.put("message", "OCR 처리 실패: " + visionResult.getErrorMessage());
                return ResponseEntity.badRequest().body(response);
            }

            // OCR 결과 로그
            log.info("=== ESP32 이미지 OCR 결과 ===");
            log.info("추출된 숫자들: {}", visionResult.getDetectedNumbers());
            log.info("현재 페이지 번호: {}", visionResult.getEstimatedPageNumber());
            log.info("신뢰도: {}", visionResult.getConfidence());

            // book_id가 있으면 데이터베이스에 저장
            if (bookId != null) {
                BookPageDTO bookPageDTO = new BookPageDTO();
                bookPageDTO.setBookId(bookId);
                bookPageDTO.setUserId(userId);
                bookPageDTO.setPageNumber(visionResult.getEstimatedPageNumber());
                bookPageDTO.setCapturedAt(LocalDateTime.now());
                
                bookPageService.saveBookPage(bookPageDTO);
                
                log.info("BookPage 저장 완료: bookId={}, userId={}, pageNumber={}", bookId, userId, visionResult.getEstimatedPageNumber());
            } else {
                log.warn("book_id가 없어서 BookPage 저장 건너뜀");
            }

            // 응답 데이터
            response.put("success", true);
            response.put("message", "이미지 처리 완료");
            response.put("bookId", bookId);
            response.put("userId", userId);
            response.put("pageNumber", visionResult.getEstimatedPageNumber());
            response.put("confidence", visionResult.getConfidence());
            response.put("detectedNumbers", visionResult.getDetectedNumbers());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("이미지 업로드 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 활성 세션 상태 조회
     */
    @GetMapping("/session-status")
    public ResponseEntity<?> getSessionStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Long> bookIds = esp32ReadingSessionService.getAllBookIds();
            Map<String, Long> userIds = esp32ReadingSessionService.getAllUserIds();
            
            response.put("success", true);
            response.put("activeSessions", bookIds.size());
            response.put("bookIds", bookIds);
            response.put("userIds", userIds);
            response.put("message", "세션 상태 조회 완료");
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("세션 상태 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "세션 상태 조회 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
