package com.example.ReadMark.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageMatchingService {
    
    // 이미지 업로드 대기열 (타임스탬프와 함께 저장)
    private final BlockingQueue<ImageUploadRequest> imageQueue = new LinkedBlockingQueue<>();
    
    // 세션별 book_id 매핑
    private final Map<String, Long> sessionBookIds = new ConcurrentHashMap<>();
    
    // 이미지 업로드 요청 클래스
    public static class ImageUploadRequest {
        private String deviceId;
        private byte[] imageData;
        private LocalDateTime uploadTime;
        private String fileName;
        
        public ImageUploadRequest(String deviceId, byte[] imageData, String fileName) {
            this.deviceId = deviceId;
            this.imageData = imageData;
            this.fileName = fileName;
            this.uploadTime = LocalDateTime.now();
        }
        
        // Getters
        public String getDeviceId() { return deviceId; }
        public byte[] getImageData() { return imageData; }
        public LocalDateTime getUploadTime() { return uploadTime; }
        public String getFileName() { return fileName; }
    }
    
    /**
     * 이미지 업로드 요청을 큐에 추가
     */
    public void addImageToQueue(String deviceId, byte[] imageData, String fileName) {
        ImageUploadRequest request = new ImageUploadRequest(deviceId, imageData, fileName);
        imageQueue.offer(request);
        log.info("이미지 업로드 요청 큐에 추가: deviceId={}, fileName={}, uploadTime={}", 
                deviceId, fileName, request.getUploadTime());
    }
    
    /**
     * 세션에 book_id 설정
     */
    public void setBookIdForSession(String deviceId, Long bookId) {
        sessionBookIds.put(deviceId, bookId);
        log.info("세션에 book_id 설정: deviceId={}, bookId={}", deviceId, bookId);
    }
    
    /**
     * 세션의 book_id 조회
     */
    public Long getBookIdForSession(String deviceId) {
        return sessionBookIds.get(deviceId);
    }
    
    /**
     * 큐에서 이미지 처리 (순차 처리)
     */
    public ImageUploadRequest getNextImageFromQueue() throws InterruptedException {
        return imageQueue.take(); // 블로킹 방식으로 순차 처리
    }
    
    /**
     * 큐 크기 조회
     */
    public int getQueueSize() {
        return imageQueue.size();
    }
}
