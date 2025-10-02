package com.example.ReadMark.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class ESP32ReadingSessionService {
    
    // ESP32 디바이스별 독서 세션 정보를 메모리에 저장
    // 실제 운영환경에서는 Redis나 DB를 사용하는 것이 좋음
    private final Map<String, ReadingSession> activeSessions = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionBookIds = new ConcurrentHashMap<>(); // 세션별 book_id 저장
    private final Map<String, Long> sessionUserIds = new ConcurrentHashMap<>(); // 세션별 user_id 저장
    
    /**
     * 현재 페이지 업데이트
     */
    public void updateCurrentPage(String deviceId, int pageNumber) {
        ReadingSession session = activeSessions.get(deviceId);
        
        if (session == null) {
            // 새로운 독서 세션 시작
            session = new ReadingSession();
            session.setDeviceId(deviceId);
            session.setStartDate(LocalDate.now());
            session.setStartTime(LocalDateTime.now());
            session.setCurrentPage(pageNumber);
            session.setLastUpdateTime(LocalDateTime.now());
            
            activeSessions.put(deviceId, session);
            
            log.info("새로운 독서 세션 시작: 디바이스 {}, 시작 페이지 {}", deviceId, pageNumber);
        } else {
            // 기존 세션 업데이트
            session.setCurrentPage(pageNumber);
            session.setLastUpdateTime(LocalDateTime.now());
            
            log.info("독서 세션 업데이트: 디바이스 {}, 현재 페이지 {}", deviceId, pageNumber);
        }
    }
    
    /**
     * 독서 완료 처리
     */
    public Map<String, Object> completeReading(String deviceId, int finalPage) {
        ReadingSession session = activeSessions.get(deviceId);
        
        if (session == null) {
            throw new RuntimeException("활성 독서 세션이 없습니다.");
        }
        
        // 독서 시작일로부터 경과 일수 계산
        long totalDays = java.time.temporal.ChronoUnit.DAYS.between(session.getStartDate(), LocalDate.now());
        
        // 결과 생성
        Map<String, Object> result = new HashMap<>();
        result.put("finalPage", finalPage);
        result.put("readingStartDate", session.getStartDate().format(DateTimeFormatter.ofPattern("yyyy/MM/dd")));
        result.put("totalReadingDays", (int) totalDays + 1); // +1은 시작일 포함
        result.put("startPage", session.getCurrentPage());
        
        // 세션 완료 처리
        session.setCompleted(true);
        session.setEndTime(LocalDateTime.now());
        session.setFinalPage(finalPage);
        
        log.info("독서 완료: 디바이스 {}, 시작 페이지 {}, 마지막 페이지 {}", 
                deviceId, session.getCurrentPage(), finalPage);
        
        return result;
    }
    
    /**
     * 독서 세션 초기화 (ESP32 버튼으로 초기화)
     */
    public void resetSession(String deviceId) {
        activeSessions.remove(deviceId);
        sessionBookIds.remove(deviceId);
        sessionUserIds.remove(deviceId);
        log.info("독서 세션 초기화: 디바이스 {}", deviceId);
    }

    /**
     * 세션에 book_id 설정
     */
    public void setBookIdForSession(String sessionId, Long bookId) {
        sessionBookIds.put(sessionId, bookId);
        log.info("세션에 book_id 설정: sessionId={}, bookId={}", sessionId, bookId);
    }
    
    /**
     * 세션에 user_id 설정
     */
    public void setUserIdForSession(String sessionId, Long userId) {
        sessionUserIds.put(sessionId, userId);
        log.info("세션에 user_id 설정: sessionId={}, userId={}", sessionId, userId);
    }

    /**
     * 세션의 book_id 조회
     */
    public Long getBookIdForSession(String sessionId) {
        return sessionBookIds.get(sessionId);
    }
    
    /**
     * 세션의 user_id 조회
     */
    public Long getUserIdForSession(String sessionId) {
        return sessionUserIds.get(sessionId);
    }
    
    /**
     * 모든 활성 세션의 book_id 조회 (ESP32 1개인 경우용)
     */
    public Map<String, Long> getAllBookIds() {
        return new HashMap<>(sessionBookIds);
    }
    
    /**
     * 모든 활성 세션의 user_id 조회 (ESP32 1개인 경우용)
     */
    public Map<String, Long> getAllUserIds() {
        return new HashMap<>(sessionUserIds);
    }

    /**
     * 세션 시작 (book_id와 user_id 포함)
     */
    public void startSession(String sessionId, Long bookId, Long userId) {
        ReadingSession session = new ReadingSession();
        session.setDeviceId(sessionId);
        session.setBookId(bookId);
        session.setUserId(userId);
        session.setStartDate(LocalDate.now());
        session.setStartTime(LocalDateTime.now());
        session.setLastUpdateTime(LocalDateTime.now());
        
        activeSessions.put(sessionId, session);
        sessionBookIds.put(sessionId, bookId);
        
        log.info("새로운 독서 세션 시작: sessionId={}, bookId={}, userId={}", sessionId, bookId, userId);
    }
    
    
    /**
     * 독서 세션 정보 클래스
     */
    private static class ReadingSession {
        private String deviceId;
        private Long bookId;
        private Long userId;
        private LocalDate startDate;
        private LocalDateTime startTime;
        private LocalDateTime endTime;
        private LocalDateTime lastUpdateTime;
        private int currentPage;
        private int finalPage;
        private boolean completed = false;
        
        // Getters and Setters
        public String getDeviceId() { return deviceId; }
        public void setDeviceId(String deviceId) { this.deviceId = deviceId; }
        
        public Long getBookId() { return bookId; }
        public void setBookId(Long bookId) { this.bookId = bookId; }
        
        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }
        
        public LocalDate getStartDate() { return startDate; }
        public void setStartDate(LocalDate startDate) { this.startDate = startDate; }
        
        public LocalDateTime getStartTime() { return startTime; }
        public void setStartTime(LocalDateTime startTime) { this.startTime = startTime; }
        
        public LocalDateTime getEndTime() { return endTime; }
        public void setEndTime(LocalDateTime endTime) { this.endTime = endTime; }
        
        public LocalDateTime getLastUpdateTime() { return lastUpdateTime; }
        public void setLastUpdateTime(LocalDateTime lastUpdateTime) { this.lastUpdateTime = lastUpdateTime; }
        
        public int getCurrentPage() { return currentPage; }
        public void setCurrentPage(int currentPage) { this.currentPage = currentPage; }
        
        public int getFinalPage() { return finalPage; }
        public void setFinalPage(int finalPage) { this.finalPage = finalPage; }
        
        public boolean isCompleted() { return completed; }
        public void setCompleted(boolean completed) { this.completed = completed; }
    }
}
