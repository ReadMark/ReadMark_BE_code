package com.example.ReadMark.controller;

import com.example.ReadMark.service.ImageMatchingService;
import com.example.ReadMark.service.BatchImageProcessingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/upload")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*")
public class ImprovedESP32UploadController {
    
    private final ImageMatchingService imageMatchingService;
    private final BatchImageProcessingService batchProcessingService;
    
    /**
     * 개선된 이미지 업로드 - 큐에 추가만 하고 즉시 응답
     */
    @PostMapping("/current-page")
    public ResponseEntity<?> uploadCurrentPage(@RequestParam("image") MultipartFile image,
                                             @RequestParam(required = false) String deviceId,
                                             @RequestParam(required = false) String fileName) {
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

            String actualDeviceId = deviceId != null ? deviceId : "ESP32-DEFAULT";
            String actualFileName = fileName != null ? fileName : image.getOriginalFilename();
            
            log.info("이미지 업로드 요청: deviceId={}, fileName={}, size={} bytes", 
                    actualDeviceId, actualFileName, image.getSize());

            // 이미지를 큐에 추가 (비동기 처리)
            imageMatchingService.addImageToQueue(actualDeviceId, image.getBytes(), actualFileName);

            // 즉시 응답 (처리는 백그라운드에서)
            response.put("success", true);
            response.put("message", "이미지가 처리 큐에 추가되었습니다.");
            response.put("deviceId", actualDeviceId);
            response.put("fileName", actualFileName);
            response.put("queueSize", imageMatchingService.getQueueSize());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("이미지 업로드 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * ESP32-CAM용 단일 이미지 업로드 (한 번에 하나의 이미지만)
     */
    @PostMapping("/single-image")
    public ResponseEntity<?> uploadSingleImage(@RequestParam("image") MultipartFile image,
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

            String actualDeviceId = deviceId != null ? deviceId : "ESP32-CAM-DEFAULT";
            String fileName = image.getOriginalFilename() != null ? image.getOriginalFilename() : "image.jpg";
            
            log.info("ESP32-CAM 단일 이미지 업로드: deviceId={}, fileName={}, size={} bytes", 
                    actualDeviceId, fileName, image.getSize());

            // 이미지를 큐에 추가 (비동기 처리)
            imageMatchingService.addImageToQueue(actualDeviceId, image.getBytes(), fileName);

            // 즉시 응답 (처리는 백그라운드에서)
            response.put("success", true);
            response.put("message", "이미지가 성공적으로 업로드되었습니다.");
            response.put("deviceId", actualDeviceId);
            response.put("fileName", fileName);
            response.put("fileSize", image.getSize());
            response.put("contentType", contentType);
            response.put("queueSize", imageMatchingService.getQueueSize());

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            log.error("ESP32-CAM 이미지 업로드 중 오류 발생", e);
            response.put("success", false);
            response.put("message", "이미지 업로드 중 오류가 발생했습니다: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }
    
    /**
     * 큐 상태 조회
     */
    @GetMapping("/queue-status")
    public ResponseEntity<?> getQueueStatus() {
        Map<String, Object> response = new HashMap<>();
        
        response.put("success", true);
        response.put("queueSize", imageMatchingService.getQueueSize());
        response.put("message", "큐 상태 조회 완료");
        
        return ResponseEntity.ok(response);
    }
}
