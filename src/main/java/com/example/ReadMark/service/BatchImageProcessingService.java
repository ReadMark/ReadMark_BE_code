package com.example.ReadMark.service;

import com.example.ReadMark.model.dto.BookPageDTO;
import com.example.ReadMark.service.ImageMatchingService.ImageUploadRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Slf4j
public class BatchImageProcessingService {
    
    private final ImageMatchingService imageMatchingService;
    private final GoogleVisionService visionService;
    private final BookPageService bookPageService;
    
    // 배치별 이미지 그룹핑 (타임스탬프 기반)
    private final Map<String, List<ImageUploadRequest>> batchGroups = new ConcurrentHashMap<>();
    
    /**
     * 배치 처리 시작 (비동기)
     */
    @Async
    public void startBatchProcessing() {
        log.info("배치 이미지 처리 시작");
        
        while (true) {
            try {
                // 5초마다 배치 처리
                Thread.sleep(5000);
                processBatchImages();
            } catch (InterruptedException e) {
                log.error("배치 처리 중 오류 발생", e);
                break;
            }
        }
    }
    
    /**
     * 배치 이미지 처리
     */
    private void processBatchImages() {
        // 큐에서 모든 이미지 가져오기
        List<ImageUploadRequest> batchImages = new ArrayList<>();
        
        while (imageMatchingService.getQueueSize() > 0) {
            try {
                ImageUploadRequest request = imageMatchingService.getNextImageFromQueue();
                batchImages.add(request);
            } catch (InterruptedException e) {
                log.error("이미지 큐에서 가져오기 실패", e);
                break;
            }
        }
        
        if (batchImages.isEmpty()) {
            return;
        }
        
        log.info("배치 처리 시작: {} 개 이미지", batchImages.size());
        
        // 타임스탬프별로 그룹핑
        Map<String, List<ImageUploadRequest>> timeGroups = groupByTimeWindow(batchImages);
        
        // 각 그룹별로 처리
        for (Map.Entry<String, List<ImageUploadRequest>> entry : timeGroups.entrySet()) {
            String timeWindow = entry.getKey();
            List<ImageUploadRequest> images = entry.getValue();
            
            log.info("시간 윈도우 {} 처리: {} 개 이미지", timeWindow, images.size());
            processTimeGroup(timeWindow, images);
        }
    }
    
    /**
     * 시간 윈도우별로 이미지 그룹핑 (5초 단위)
     */
    private Map<String, List<ImageUploadRequest>> groupByTimeWindow(List<ImageUploadRequest> images) {
        Map<String, List<ImageUploadRequest>> groups = new ConcurrentHashMap<>();
        
        for (ImageUploadRequest image : images) {
            // 5초 단위로 그룹핑
            long timeWindow = image.getUploadTime().getSecond() / 5;
            String groupKey = image.getDeviceId() + "_" + timeWindow;
            
            groups.computeIfAbsent(groupKey, k -> new ArrayList<>()).add(image);
        }
        
        return groups;
    }
    
    /**
     * 시간 그룹별 이미지 처리
     */
    private void processTimeGroup(String timeWindow, List<ImageUploadRequest> images) {
        // deviceId 추출
        String deviceId = timeWindow.split("_")[0];
        Long bookId = imageMatchingService.getBookIdForSession(deviceId);
        
        if (bookId == null) {
            log.warn("book_id가 없어서 이미지 처리 건너뜀: deviceId={}", deviceId);
            return;
        }
        
        // 이미지들을 업로드 시간순으로 정렬
        images.sort((a, b) -> a.getUploadTime().compareTo(b.getUploadTime()));
        
        // 순차적으로 OCR 처리
        for (int i = 0; i < images.size(); i++) {
            ImageUploadRequest image = images.get(i);
            
            try {
                // OCR 처리
                var visionResult = visionService.extractTextFromImage(image.getImageData());
                
                if (visionResult.isSuccess()) {
                    // BookPageDTO 생성 및 저장
                    BookPageDTO bookPageDTO = new BookPageDTO();
                    bookPageDTO.setBookId(bookId);
                    bookPageDTO.setPageNumber(visionResult.getEstimatedPageNumber());
                    bookPageDTO.setCapturedAt(LocalDateTime.now());
                    
                    // 데이터베이스에 저장
                    bookPageService.saveBookPage(bookPageDTO);
                    
                    log.info("배치 처리 완료: bookId={}, pageNumber={}, fileName={}, 순서={}", 
                            bookId, visionResult.getEstimatedPageNumber(), image.getFileName(), i + 1);
                } else {
                    log.warn("OCR 처리 실패: fileName={}, error={}", 
                            image.getFileName(), visionResult.getErrorMessage());
                }
                
            } catch (Exception e) {
                log.error("이미지 처리 중 오류 발생: fileName={}", image.getFileName(), e);
            }
        }
    }
}
