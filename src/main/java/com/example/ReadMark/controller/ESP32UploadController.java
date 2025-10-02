package com.example.ReadMark.controller;

import com.example.ReadMark.model.dto.BookPageDTO;
import com.example.ReadMark.service.BookPageService;
import com.example.ReadMark.service.GoogleVisionService;
import com.example.ReadMark.service.ReadingPeriodService;
import com.example.ReadMark.service.ReadingSessionService;
import com.example.ReadMark.service.ESP32ReadingSessionService;
import com.example.ReadMark.handler.ESP32WebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
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
    private final ReadingPeriodService readingPeriodService;
    private final Environment environment;
    private final ESP32WebSocketHandler webSocketHandler;
    private final ESP32ReadingSessionService esp32ReadingSessionService;
    
    /**
     * ESP32-CAM에서 이미지를 업로드하고 OCR 결과를 반환합니다.
     * 한 번의 요청에는 반드시 하나의 이미지만 업로드됩니다.
     */
    /**
     * ESP32-CAM 단일 이미지 업로드 API - 한 번에 하나의 이미지만 처리
     * 현재 읽고 있는 페이지 번호를 받아서 독서 세션을 업데이트
     */
    @PostMapping("/current-page")
    public ResponseEntity<?> updateCurrentPage(@RequestParam("image") MultipartFile image,
                                             @RequestParam(required = false) String deviceId) {
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

            log.info("ESP32 현재 페이지 업데이트 시작: 크기 {} bytes, deviceId={}", image.getSize(), deviceId);
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
            
            // bookId와 userId가 모두 있어야 OCR 결과를 저장
            if (bookId == null || userId == null) {
                response.put("success", false);
                response.put("message", "활성 세션이 없습니다. ESP32에서 먼저 사용자 로그인과 책 선택을 해주세요.");
                return ResponseEntity.badRequest().body(response);
            }

            // OCR 처리
            var visionResult = visionService.extractTextFromImage(imageBytes);

            if (!visionResult.isSuccess()) {
                response.put("success", false);
                response.put("message", "OCR 처리 실패: " + visionResult.getErrorMessage());
                return ResponseEntity.badRequest().body(response);
            }

            // OCR 결과를 서버 콘솔 로그에 출력
            log.info("=== ESP32-CAM 이미지 OCR 결과 ===");
            log.info("추출된 숫자들: {}", visionResult.getDetectedNumbers());
            log.info("현재 페이지 번호: {}", visionResult.getEstimatedPageNumber());
            log.info("신뢰도: {}", visionResult.getConfidence());
            log.info("숫자 개수: {}", visionResult.getNumberCount());
            log.info("=====================================");

            // WebSocket으로 ESP32에게 OCR 결과 전송
            webSocketHandler.sendOCRResultToESP32(bookId, userId, visionResult);

            // OCR 결과와 book_id 매칭하여 데이터베이스에 저장
            if (bookId != null) {
                // BookPageDTO 생성 및 저장
                BookPageDTO bookPageDTO = new BookPageDTO();
                bookPageDTO.setBookId(bookId);
                bookPageDTO.setUserId(userId);
                bookPageDTO.setPageNumber(visionResult.getEstimatedPageNumber());
                bookPageDTO.setCapturedAt(LocalDateTime.now());
                
                // 데이터베이스에 저장
                bookPageService.saveBookPage(bookPageDTO);
                log.info("OCR 결과와 book_id 매칭하여 저장 완료: bookId={}, pageNumber={}", bookId, visionResult.getEstimatedPageNumber());
            } else {
                log.warn("book_id가 없어서 OCR 결과를 저장하지 않음: deviceId={}", deviceId);
            }

            // 단순한 응답 구성
            response.put("success", true);
            response.put("currentPage", visionResult.getEstimatedPageNumber());
            response.put("confidence", visionResult.getConfidence());
            response.put("bookId", bookId);
            response.put("userId", userId);

            log.info("ESP32 현재 페이지 업데이트 완료: 페이지 번호 {}", visionResult.getEstimatedPageNumber());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("ESP32 현재 페이지 업데이트 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "현재 페이지 업데이트 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * ESP32-CAM 전용 단일 이미지 업로드 API
     * 한 번의 요청에 반드시 하나의 이미지만 업로드
     */
    @PostMapping("/esp32-cam/upload")
    public ResponseEntity<?> esp32CamUpload(@RequestParam("image") MultipartFile image) {
        Map<String, Object> response = new HashMap<>();

        try {
            // 이미지 유효성 검사
            if (image.isEmpty()) {
                response.put("success", false);
                response.put("message", "이미지 파일이 필요합니다.");
                response.put("color", "#FF0000"); // 빨간색 - 에러
                return ResponseEntity.badRequest().body(response);
            }

            // 이미지 크기 제한 (16MB)
            if (image.getSize() > 16 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "이미지 크기가 너무 큽니다. (최대 16MB)");
                response.put("color", "#FF0000"); // 빨간색 - 에러
                return ResponseEntity.badRequest().body(response);
            }

            // 이미지 타입 검증
            String contentType = image.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                response.put("success", false);
                response.put("message", "이미지 파일만 업로드 가능합니다.");
                response.put("color", "#FF0000"); // 빨간색 - 에러
                return ResponseEntity.badRequest().body(response);
            }

            log.info("ESP32-CAM 이미지 업로드: size={} bytes", image.getSize());
            byte[] imageBytes = image.getBytes();

            // 활성 세션에서 book_id와 user_id 조회
            Long bookId = null;
            Long userId = null;
            
            for (Map.Entry<String, Long> entry : esp32ReadingSessionService.getAllBookIds().entrySet()) {
                bookId = entry.getValue();
                break;
            }
            
            for (Map.Entry<String, Long> entry : esp32ReadingSessionService.getAllUserIds().entrySet()) {
                userId = entry.getValue();
                break;
            }

            log.info("활성 세션에서 조회: bookId={}, userId={}", bookId, userId);
            
            if (bookId == null || userId == null) {
                response.put("success", false);
                response.put("message", "활성 세션이 없습니다. ESP32에서 먼저 사용자 로그인과 책 선택을 해주세요.");
                response.put("color", "#FF0000"); // 빨간색 - 세션 없음 에러
                return ResponseEntity.badRequest().body(response);
            }

            // OCR 처리
            var visionResult = visionService.extractTextFromImage(imageBytes);

            if (!visionResult.isSuccess()) {
                response.put("success", false);
                response.put("message", "OCR 처리 실패: " + visionResult.getErrorMessage());
                response.put("color", "#FF0000"); // 빨간색 - OCR 실패
                return ResponseEntity.badRequest().body(response);
            }

            // OCR 결과 로그 (페이지 번호만 추출)
            log.info("=== ESP32-CAM 페이지 번호 OCR 결과 ===");
            log.info("추출된 숫자들: {}", visionResult.getDetectedNumbers());
            log.info("페이지 번호: {}", visionResult.getEstimatedPageNumber());
            log.info("신뢰도: {}", visionResult.getConfidence());

            // WebSocket으로 ESP32에게 OCR 결과 전송
            webSocketHandler.sendOCRResultToESP32(bookId, userId, visionResult);

            // OCR 결과를 데이터베이스에 저장
            BookPageDTO bookPageDTO = new BookPageDTO();
            bookPageDTO.setBookId(bookId);
            bookPageDTO.setUserId(userId);
            bookPageDTO.setPageNumber(visionResult.getEstimatedPageNumber());
            bookPageDTO.setCapturedAt(LocalDateTime.now());
            
            bookPageService.saveBookPage(bookPageDTO);
            log.info("ESP32-CAM OCR 결과 저장 완료: bookId={}, pageNumber={}", bookId, visionResult.getEstimatedPageNumber());

            // 응답 구성 (최대한 간결하게)
            response.put("success", true);
            response.put("message", "페이지 번호가 성공적으로 추출되었습니다.");
            response.put("bookId", bookId);
            response.put("userId", userId);
            response.put("pageNumber", visionResult.getEstimatedPageNumber());
            response.put("color", "#00FF00"); // 초록색 - OCR 성공

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("ESP32-CAM 이미지 업로드 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
            response.put("color", "#FF0000"); // 빨간색 - 에러
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * ESP32 마지막 페이지 전송 API (URL 2) - 드물게 호출됨
     * 책을 다 읽었을 때 마지막 페이지 번호를 받아서 독서 세션을 완료
     */
    @PostMapping("/final-page")
    public ResponseEntity<?> completeReading(@RequestParam("image") MultipartFile image) {
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

            log.info("ESP32 마지막 페이지 전송 시작: 크기 {} bytes", image.getSize());
            byte[] imageBytes = image.getBytes();

            var visionResult = visionService.extractTextFromImage(imageBytes);

            if (!visionResult.isSuccess()) {
                response.put("success", false);
                response.put("message", "OCR 처리 실패: " + visionResult.getErrorMessage());
                return ResponseEntity.badRequest().body(response);
            }

            // OCR 결과를 서버 콘솔 로그에 출력
            log.info("=== ESP32 마지막 페이지 OCR 결과 ===");
            log.info("추출된 숫자들: {}", visionResult.getDetectedNumbers());
            log.info("마지막 페이지 번호: {}", visionResult.getEstimatedPageNumber());
            log.info("신뢰도: {}", visionResult.getConfidence());
            log.info("숫자 개수: {}", visionResult.getNumberCount());
            log.info("=====================================");

            // 독서 세션 완료 처리
            String deviceId = "ESP32-DEFAULT"; // 실제로는 ESP32에서 전송하는 디바이스 ID 사용
            Map<String, Object> readingResult = esp32ReadingSessionService.completeReading(deviceId, visionResult.getEstimatedPageNumber());
            
            // WebSocket으로 ESP32에게 독서 완료 결과 전송
            webSocketHandler.sendReadingCompleteResult(visionResult.getEstimatedPageNumber(), readingResult);

            // 응답 구성
            response.put("success", true);
            response.put("finalPage", readingResult.get("finalPage"));
            response.put("confidence", visionResult.getConfidence());
            response.put("readingStartDate", readingResult.get("readingStartDate"));
            response.put("totalReadingDays", readingResult.get("totalReadingDays"));
            response.put("startPage", readingResult.get("startPage"));
            response.put("message", "독서 완료!");

            log.info("ESP32 독서 완료 처리: 마지막 페이지 {}", 
                    visionResult.getEstimatedPageNumber());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("ESP32 마지막 페이지 전송 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "마지막 페이지 전송 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

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
            
            // 이미지 크기 제한 (16MB - MEDIUMBLOB 크기)
            if (image.getSize() > 16 * 1024 * 1024) {
                response.put("success", false);
                response.put("message", "이미지 크기가 너무 큽니다. (최대 16MB)");
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
            
            // OCR 처리
            var visionResult = visionService.extractTextFromImage(imageBytes);
            
            if (!visionResult.isSuccess()) {
                response.put("success", false);
                response.put("message", "OCR 처리 실패: " + visionResult.getErrorMessage());
                return ResponseEntity.badRequest().body(response);
            }
            
            // OCR 결과를 서버 콘솔 로그에 출력
            log.info("=== OCR 결과 ===");
            log.info("추출된 숫자들: {}", visionResult.getDetectedNumbers());
            log.info("추정 페이지 번호: {}", visionResult.getEstimatedPageNumber());
            log.info("신뢰도: {}", visionResult.getConfidence());
            log.info("숫자 개수: {}", visionResult.getNumberCount());
            log.info("================");
            
            // 단순한 응답 구성
            response.put("success", true);
            response.put("pageNumber", visionResult.getEstimatedPageNumber());
            
            log.info("ESP32 OCR 처리 완료: 페이지 번호 {}", visionResult.getEstimatedPageNumber());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("ESP32 이미지 업로드 처리 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "이미지 처리 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
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
        response.put("status", "REST API 모드");
        return ResponseEntity.ok(response);
    }
    
    /**
     * ESP32 상태 확인 (REST API 모드)
     */
    @GetMapping("/status")
    public ResponseEntity<?> getStatus() {
        Map<String, Object> response = new HashMap<>();
        response.put("status", "OK");
        response.put("mode", "REST API");
        response.put("timestamp", LocalDateTime.now());
        response.put("serverPort", environment.getProperty("server.port", "5000"));
        response.put("serverAddress", environment.getProperty("server.address", "0.0.0.0"));
        return ResponseEntity.ok(response);
    }
    
    /**
     * ESP32 테스트 메시지 (REST API 모드)
     */
    @PostMapping("/test")
    public ResponseEntity<?> sendTestMessage(@RequestParam("message") String message) {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("success", true);
            response.put("message", "테스트 메시지 수신: " + message);
            response.put("timestamp", LocalDateTime.now());
            response.put("mode", "REST API");
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("success", false);
            response.put("message", "메시지 처리 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * ESP32 진단 정보 (REST API 모드)
     */
    @GetMapping("/diagnostics")
    public ResponseEntity<?> getDiagnostics() {
        Map<String, Object> response = new HashMap<>();
        try {
            response.put("serverPort", environment.getProperty("server.port", "5000"));
            response.put("serverAddress", environment.getProperty("server.address", "0.0.0.0"));
            response.put("mode", "REST API");
            response.put("corsEnabled", true);
            response.put("timestamp", LocalDateTime.now());
            response.put("endpoints", Map.of(
                "upload", "/upload/",
                "health", "/upload/health",
                "status", "/upload/status",
                "test", "/upload/test",
                "diagnostics", "/upload/diagnostics"
            ));
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            response.put("error", "진단 정보 수집 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * ESP32 독서 세션 초기화 API
     * ESP32 버튼으로 초기화할 때 호출
     */
    @PostMapping("/reset-session")
    public ResponseEntity<?> resetReadingSession() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String deviceId = "ESP32-DEFAULT"; // 실제로는 ESP32에서 전송하는 디바이스 ID 사용
            esp32ReadingSessionService.resetSession(deviceId);
            
            response.put("success", true);
            response.put("message", "독서 세션이 초기화되었습니다.");
            response.put("deviceId", deviceId);
            
            log.info("ESP32 독서 세션 초기화: 디바이스 {}", deviceId);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("ESP32 독서 세션 초기화 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "독서 세션 초기화 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
}


