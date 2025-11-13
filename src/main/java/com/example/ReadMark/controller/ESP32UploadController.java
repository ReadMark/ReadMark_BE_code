package com.example.ReadMark.controller;

import com.example.ReadMark.model.dto.BookPageDTO;
import com.example.ReadMark.model.dto.UserBookDTO;
import com.example.ReadMark.model.entity.UserBook;
import com.example.ReadMark.service.BookPageService;
import com.example.ReadMark.service.BookService;
import com.example.ReadMark.service.GoogleVisionService;
import com.example.ReadMark.service.ReadingPeriodService;
import com.example.ReadMark.service.ReadingSessionService;
import com.example.ReadMark.service.ReadingLogService;
import com.example.ReadMark.service.ESP32ReadingSessionService;
import com.example.ReadMark.service.UserBookService;
import com.example.ReadMark.handler.ESP32WebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.env.Environment;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.util.UUID;
import java.util.Comparator;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH}, allowedHeaders = "*", allowCredentials = "false")
public class ESP32UploadController {
    
    private final GoogleVisionService visionService;
    private final BookPageService bookPageService;
    private final BookService bookService;
    private final ReadingSessionService readingSessionService;
    private final ReadingPeriodService readingPeriodService;
    private final ReadingLogService readingLogService;
    private final Environment environment;
    private final ESP32WebSocketHandler webSocketHandler;
    private final ESP32ReadingSessionService esp32ReadingSessionService;
    private final UserBookService userBookService;
    
    /**
     * ESP32-CAM에서 이미지를 업로드하고 OCR 결과를 반환합니다.
     * 한 번의 요청에는 반드시 하나의 이미지만 업로드됩니다.
     */
    /**
     * ESP32-CAM 단일 이미지 업로드 API - 한 번에 하나의 이미지만 처리
     * 현재 읽고 있는 페이지 번호를 받아서 독서 세션을 업데이트
     */
    @PostMapping(value = "/current-page", consumes = "multipart/form-data")
    public ResponseEntity<?> updateCurrentPage(@ModelAttribute("image") MultipartFile image,
                                             @ModelAttribute("deviceId") String deviceId) {
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

            // 가장 최근에 업데이트된 활성 세션에서 book_id와 user_id 조회
            Map<String, Long> latestSession = esp32ReadingSessionService.getLatestSessionInfo();
            Long bookId = latestSession.get("bookId");
            Long userId = latestSession.get("userId");
            
            log.info("활성 세션: total={}, fullyBound={}",
                    esp32ReadingSessionService.getActiveSessionCount(),
                    esp32ReadingSessionService.getFullyBoundSessionCount());
            
            log.info("가장 최근 활성 세션에서 조회: bookId={}, userId={}", bookId, userId);
            
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
                
                // UserBook의 currentPage 업데이트 (최신 페이지 번호로)
                userBookService.updateCurrentPageByUserIdAndBookId(userId, bookId, visionResult.getEstimatedPageNumber());
                
                // 일별 페이지 수 자동 업데이트 (도장 생성 포함)
                readingLogService.updateDailyPagesRead(userId, LocalDate.now());
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
    @PostMapping(value = "/esp32-cam/upload", consumes = "multipart/form-data")
    public ResponseEntity<?> esp32CamUpload(@RequestParam("image") MultipartFile image) {
        Map<String, Object> response = new HashMap<>();

        log.info(">>> ESP32-CAM 업로드 요청 수신 시작 <<<");
        log.info("요청 시간: {}", LocalDateTime.now());
        log.info("이미지 크기: {} bytes", image != null ? image.getSize() : 0);
        log.info("이미지 이름: {}", image != null ? image.getOriginalFilename() : "null");
        log.info("Content-Type: {}", image != null ? image.getContentType() : "null");

        try {
            // 이미지 유효성 검사
            if (image == null || image.isEmpty()) {
                log.warn("ESP32-CAM 업로드 실패: 이미지가 비어있음");
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

            // 가장 최근에 업데이트된 활성 세션에서 book_id와 user_id 조회
            Map<String, Long> latestSession = esp32ReadingSessionService.getLatestSessionInfo();
            Long bookId = latestSession.get("bookId");
            Long userId = latestSession.get("userId");
            
            log.info("활성 세션: total={}, fullyBound={}",
                    esp32ReadingSessionService.getActiveSessionCount(),
                    esp32ReadingSessionService.getFullyBoundSessionCount());
            
            log.info("가장 최근 활성 세션에서 조회: bookId={}, userId={}", bookId, userId);
            
            // 임시 이미지 저장 (디버깅용, 프로젝트 파일에 영향 없음)
            String tempImageUrl = null;
            try {
                tempImageUrl = saveTemporaryImage(image, userId, bookId);
                log.info("임시 이미지 저장 완료: {}", tempImageUrl);
            } catch (Exception e) {
                log.warn("임시 이미지 저장 실패: {}", e.getMessage());
            }
            
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

            // 페이지 인식 실패 검증
            if (visionResult.getEstimatedPageNumber() == null || visionResult.getEstimatedPageNumber() == -1) {
                response.put("success", false);
                response.put("message", "페이지 번호를 인식할 수 없습니다. 더 명확한 이미지로 재촬영해주세요.");
                response.put("color", "#FF0000"); // 빨간색 - 재촬영 요청
                return ResponseEntity.badRequest().body(response);
            }
            
            // 이전 페이지와 비교하여 역행 검증
            try {
                // OCR 결과를 데이터베이스에 저장 (역행 검증 포함)
                BookPageDTO bookPageDTO = bookPageService.createBookPage(
                    userId, bookId, imageBytes, "ESP32-CAM", LocalDateTime.now());
                
                log.info("ESP32-CAM OCR 결과 저장 완료: bookId={}, pageNumber={}", bookId, visionResult.getEstimatedPageNumber());
                
                // UserBook의 currentPage 업데이트 (최신 페이지 번호로)
                userBookService.updateCurrentPageByUserIdAndBookId(userId, bookId, visionResult.getEstimatedPageNumber());
                
                // 일별 페이지 수 자동 업데이트 (도장 생성 포함)
                readingLogService.updateDailyPagesRead(userId, LocalDate.now());

                // 응답 구성 (최대한 간결하게)
                response.put("success", true);
                response.put("message", "페이지 번호가 성공적으로 추출되었습니다.");
                response.put("bookId", bookId);
                response.put("userId", userId);
                response.put("pageNumber", visionResult.getEstimatedPageNumber());
                response.put("color", "#00FF00"); // 초록색 - OCR 성공
                
            } catch (RuntimeException e) {
                // 역행 또는 기타 예외 상황 처리
                response.put("success", false);
                response.put("message", e.getMessage());
                response.put("color", "#FF0000"); // 빨간색 - 재촬영 요청
                return ResponseEntity.badRequest().body(response);
            }

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
    @PostMapping(value = "/final-page", consumes = "multipart/form-data")
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

    @PostMapping(value = "/", consumes = "multipart/form-data")
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
    
    /**
     * ESP32 세션 상태 확인 API
     */
    @GetMapping("/session-status")
    public ResponseEntity<?> getSessionStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Map<String, Object> sessionStatus = esp32ReadingSessionService.getSessionStatus();
            
            response.put("success", true);
            response.put("sessionStatus", sessionStatus);
            
            log.info("ESP32 세션 상태 조회: 활성 세션 {}개", sessionStatus.get("activeSessions"));
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("ESP32 세션 상태 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "세션 상태 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * ESP32에서 bookId와 userId로 책 정보와 현재 페이지 정보 조회 API
     */
    @GetMapping("/book-info/{bookId}/{userId}")
    public ResponseEntity<?> getBookInfoForESP32(@PathVariable Long bookId, @PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 책 정보 조회
            var book = bookPageService.getUserBookId(bookId);
            if (book == null) {
                response.put("success", false);
                response.put("message", "책을 찾을 수 없습니다.");
                return ResponseEntity.notFound().build();
            }

            // 현재 읽고 있는 페이지 조회 (최근 페이지)
            Integer currentPage = 0;
            try {
                var recentPages = bookPageService.getRecentPages(userId, bookId, 1);
                if (recentPages != null && !recentPages.isEmpty() && recentPages.get(0).getPageNumber() != null) {
                    currentPage = recentPages.get(0).getPageNumber();
                }
            } catch (Exception e) {
                log.warn("최근 페이지 조회 실패: userId={}, bookId={}", userId, bookId, e);
            }
            
            // 책의 전체 페이지 수 조회 (books 테이블의 total_book)
            Integer totalBook = null;
            try {
                var bookOpt = bookService.findById(bookId);
                if (bookOpt.isPresent() && bookOpt.get().getTotalBook() != null) {
                    totalBook = bookOpt.get().getTotalBook();
                }
            } catch (Exception e) {
                log.warn("전체 페이지 수 조회 실패: userId={}, bookId={}", userId, bookId, e);
            }
            
            // 응답 구성
            response.put("success", true);
            response.put("bookId", bookId);
            response.put("userId", userId);
            response.put("currentPage", currentPage);
            response.put("totalBook", totalBook);
            response.put("readingProgress", totalBook != null && totalBook > 0 ? Math.round((double) currentPage / totalBook * 100) : 0);
            response.put("remainingPages", totalBook != null ? Math.max(0, totalBook - currentPage) : 0);
            
            log.info("ESP32 책 정보 조회: bookId={}, userId={}, currentPage={}, totalBook={}", 
                    bookId, userId, currentPage, totalBook);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("ESP32 책 정보 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "책 정보 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 프론트엔드용 통합 책 정보 API
     * 데이터베이스 최신 정보를 반영하여 통일성 확보
     */
    @GetMapping("/frontend/book-info/{userId}")
    public ResponseEntity<?> getBookInfoForFrontend(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            // 데이터베이스에서 사용자의 최신 읽고 있는 책 정보 조회
            List<UserBookDTO> readingBooks = userBookService.getUserBooksByStatus(userId, UserBook.Status.NOW_READ);
            
            if (readingBooks.isEmpty()) {
                response.put("success", false);
                response.put("message", "읽고 있는 책이 없습니다.");
                return ResponseEntity.badRequest().body(response);
            }
            
            // 가장 최근에 추가된 읽고 있는 책 선택
            UserBookDTO latestReadingBook = readingBooks.get(0);
            Long bookId = latestReadingBook.getBook().getBookId();
            Integer currentPage = latestReadingBook.getCurrentPage();
            
            // ESP32-CAM에서 OCR로 추출한 최근 페이지 번호도 확인
            Integer ocrPage = 0;
            try {
                var recentPages = bookPageService.getRecentPages(userId, bookId, 1);
                if (recentPages != null && !recentPages.isEmpty() && recentPages.get(0).getPageNumber() != null) {
                    ocrPage = recentPages.get(0).getPageNumber();
                }
            } catch (Exception e) {
                log.warn("OCR 페이지 조회 실패: userId={}, bookId={}", userId, bookId, e);
            }
            
            // OCR 페이지가 더 최신이면 그것을 사용
            Integer finalCurrentPage = Math.max(currentPage, ocrPage);
            
            // 책의 전체 페이지 수 조회 (books 테이블의 total_book)
            Integer totalBook = null;
            try {
                var bookOpt = bookService.findById(bookId);
                if (bookOpt.isPresent() && bookOpt.get().getTotalBook() != null) {
                    totalBook = bookOpt.get().getTotalBook();
                }
            } catch (Exception e) {
                log.warn("전체 페이지 수 조회 실패: userId={}, bookId={}", userId, bookId, e);
            }
            
            // 데이터베이스 최신 정보로 구성
            response.put("success", true);
            response.put("userId", userId);                    // 사용자 ID
            response.put("bookId", bookId);                     // 데이터베이스 최신 책 ID
            response.put("currentPage", finalCurrentPage);     // 데이터베이스와 OCR 중 최신 페이지
            response.put("totalBook", totalBook);              // 책의 전체 페이지 수
            response.put("date", LocalDate.now().toString());   // 현재 날짜
            response.put("timestamp", LocalDateTime.now().toString()); // 현재 시간
            
            log.info("프론트엔드용 책 정보 조회 (데이터베이스 최신 정보): userId={}, bookId={}, currentPage={}, ocrPage={}, date={}", 
                    userId, bookId, finalCurrentPage, ocrPage, LocalDate.now());
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("프론트엔드용 책 정보 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "책 정보 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * ESP32-CAM에서 업로드된 가장 최근 이미지를 조회합니다.
     * 임시 저장된 이미지 중 가장 최근 것을 반환합니다.
     */
    @GetMapping("/esp32-cam/latest-image")
    public ResponseEntity<?> getLatestESP32CAMImage() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String tempDir = "uploads/temp/esp32-cam";
            Path tempPath = Paths.get(tempDir);
            
            if (!Files.exists(tempPath)) {
                response.put("success", false);
                response.put("message", "임시 이미지 디렉토리가 없습니다.");
                return ResponseEntity.notFound().build();
            }
            
            // 가장 최근 파일 찾기
            try (Stream<Path> paths = Files.list(tempPath)) {
                Path latestFile = paths
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".jpg") || p.toString().endsWith(".jpeg") || p.toString().endsWith(".png"))
                        .max(Comparator.comparingLong(p -> {
                            try {
                                return Files.getLastModifiedTime(p).toMillis();
                            } catch (IOException e) {
                                return 0L;
                            }
                        }))
                        .orElse(null);
                
                if (latestFile == null) {
                    response.put("success", false);
                    response.put("message", "저장된 이미지가 없습니다.");
                    return ResponseEntity.notFound().build();
                }
                
                String imageUrl = "/uploads/temp/esp32-cam/" + latestFile.getFileName().toString();
                String absolutePath = latestFile.toAbsolutePath().toString();
                
                response.put("success", true);
                response.put("imageUrl", imageUrl);
                response.put("absolutePath", absolutePath);
                response.put("filename", latestFile.getFileName().toString());
                response.put("size", Files.size(latestFile));
                response.put("lastModified", Files.getLastModifiedTime(latestFile).toString());
                
                log.info("가장 최근 ESP32-CAM 이미지 조회: {}", imageUrl);
                
                return ResponseEntity.ok(response);
            }
            
        } catch (Exception e) {
            log.error("가장 최근 ESP32-CAM 이미지 조회 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "이미지 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * ESP32-CAM에서 업로드된 이미지를 임시로 저장합니다.
     * 디버깅 및 확인용으로 사용됩니다. (프로젝트 파일에 영향 없음)
     */
    private String saveTemporaryImage(MultipartFile image, Long userId, Long bookId) throws IOException {
        // 임시 디렉토리 생성
        String uploadDir = "uploads/temp/esp32-cam";
        Path uploadPath = Paths.get(uploadDir);
        
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        
        // 파일명 생성: userId_bookId_timestamp_uuid.jpg
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String uuid = UUID.randomUUID().toString().substring(0, 8);
        String filename = String.format("user%d_book%d_%s_%s.jpg", userId, bookId, timestamp, uuid);
        
        // 파일 저장
        Path filePath = uploadPath.resolve(filename);
        Files.copy(image.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // URL 반환
        String imageUrl = "/uploads/temp/esp32-cam/" + filename;
        
        log.info("임시 이미지 저장: {}", imageUrl);
        
        return imageUrl;
    }
}


