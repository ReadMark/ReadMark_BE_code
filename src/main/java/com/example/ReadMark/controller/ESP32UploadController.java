package com.example.ReadMark.controller;

import com.example.ReadMark.model.dto.BookPageDTO;
import com.example.ReadMark.service.BookPageService;
import com.example.ReadMark.service.GoogleVisionService;
import com.example.ReadMark.service.ReadingSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/upload")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ESP32UploadController {
    
    private final GoogleVisionService visionService;
    private final BookPageService bookPageService;
    private final ReadingSessionService readingSessionService;
    
    /**
     * ESP32-CAM에서 이미지를 업로드합니다.
     * ESP32는 기본적으로 userId=1, bookId=1을 사용합니다.
     */
    @PostMapping("/")
    public ResponseEntity<?> uploadImage(@RequestParam("image") MultipartFile image) {
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
            
            log.info("ESP32 이미지 업로드 시작: 크기 {} bytes", image.getSize());
            
            // 이미지를 바이트 배열로 변환
            byte[] imageBytes = image.getBytes();
            
            // 책 페이지인지 확인
            if (!visionService.isBookPage(imageBytes)) {
                response.put("success", false);
                response.put("message", "책 페이지로 인식되지 않습니다. 더 명확한 이미지를 촬영해주세요.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // ESP32용 기본값 설정 (userId=1, bookId=1)
            Long userId = 1L;
            Long bookId = 1L;
            String deviceInfo = "ESP32-CAM";
            LocalDateTime captureTime = LocalDateTime.now();
            
            // BookPageService를 통해 페이지 생성 및 DB 저장
            BookPageDTO bookPage = bookPageService.createBookPage(
                userId, bookId, imageBytes, deviceInfo, captureTime);
            
            // 독서 세션에 페이지 추가
            readingSessionService.addImageToSession(userId, imageBytes, bookPage.getExtractedText());
            
            // 독서 기간 계산 (시작일과 종료일)
            Map<String, Object> readingPeriod = calculateReadingPeriod(userId);
            
            // ESP32 응답 형식에 맞춰 응답 구성
            response.put("success", true);
            response.put("message", "이미지 업로드 성공");
            response.put("pageId", bookPage.getPageId());
            response.put("pageNumber", bookPage.getPageNumber());
            response.put("extractedText", bookPage.getExtractedText());
            response.put("confidence", bookPage.getConfidence());
            response.put("textQuality", bookPage.getTextQuality());
            response.put("deviceInfo", deviceInfo);
            response.put("capturedAt", bookPage.getCapturedAt());
            
            // 계산된 날짜 정보 추가
            response.put("date", readingPeriod.get("date")); // ESP32에서 사용할 간단한 값
            response.put("readingPeriod", readingPeriod.get("period")); // 전체 기간 정보
            response.put("currentConsecutiveDays", readingPeriod.get("consecutiveDays"));
            
            log.info("ESP32 이미지 업로드 완료: 페이지 {}", 
                    bookPage.getPageNumber());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("ESP32 이미지 업로드 처리 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "이미지 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 독서 기간을 계산합니다.
     * ESP32에서 사용할 간단한 값과 전체 기간 정보를 반환합니다.
     */
    private Map<String, Object> calculateReadingPeriod(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 현재 연속 독서일 계산
            int consecutiveDays = readingSessionService.getCurrentConsecutiveDays(userId);
            
            // 독서 통계 가져오기
            Map<String, Object> readingStats = readingSessionService.getReadingHabitAnalysis(userId);
            
            // 현재 날짜
            LocalDate today = LocalDate.now();
            
            // 독서 시작일 계산 (연속 독서일 기준)
            LocalDate readingStartDate = today.minusDays(consecutiveDays - 1);
            
            // 날짜 형식 포맷터
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            
            // 기간 문자열 생성 (예: "2023.01.02 - 2023.01.05")
            String periodString = readingStartDate.format(formatter) + " - " + today.format(formatter);
            
            // ESP32에서 사용할 간단한 값 (연속 독서일 수)
            result.put("date", consecutiveDays);
            result.put("period", periodString);
            result.put("consecutiveDays", consecutiveDays);
            result.put("startDate", readingStartDate.format(formatter));
            result.put("endDate", today.format(formatter));
            result.put("totalReadingDays", readingStats.get("totalReadingDays"));
            
            log.info("독서 기간 계산 완료: 사용자 {}, 연속 {}일, 기간: {}", 
                    userId, consecutiveDays, periodString);
            
        } catch (Exception e) {
            log.error("독서 기간 계산 중 오류 발생", e);
            // 오류 시 기본값 설정
            result.put("date", 1);
            result.put("period", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
            result.put("consecutiveDays", 1);
            result.put("startDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
            result.put("endDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
            result.put("totalReadingDays", 1);
        }
        
        return result;
    }
    
    /**
     * ESP32 상태 확인용 헬스체크
     */
    @GetMapping("/health")
    public ResponseEntity<?> healthCheck() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("message", "ESP32 Upload Service is running");
        response.put("timestamp", LocalDateTime.now());
        return ResponseEntity.ok(response);
    }
}


