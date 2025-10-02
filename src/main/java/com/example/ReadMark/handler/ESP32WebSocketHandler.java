package com.example.ReadMark.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.ReadMark.service.ESP32ReadingSessionService;
import com.example.ReadMark.service.UserBookService;
import com.example.ReadMark.service.BookService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ESP32WebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ESP32ReadingSessionService sessionService;
    private final UserBookService userBookService;
    private final BookService bookService;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionUserIds = new ConcurrentHashMap<>(); // 세션별 user_id 저장
    private final Map<String, Long> sessionBookIds = new ConcurrentHashMap<>(); // 세션별 book_id 저장
    
    // 세션별 책 목록과 현재 인덱스 저장
    private final Map<String, List<Map<String, Object>>> sessionBookLists = new ConcurrentHashMap<>();
    private final Map<String, Integer> sessionCurrentBookIndex = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        
        log.info("ESP32 WebSocket 연결됨: {}", sessionId);
        log.info("현재 활성 세션 수: {}", sessions.size());
        
        // 연결 확인 메시지 전송
        Map<String, Object> connectionMessage = new LinkedHashMap<>();
        connectionMessage.put("pageNumber", 0 + "\n");
        connectionMessage.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n");
        connectionMessage.put("color", "#00FF00" + "\n"); // 초록색 - 연결 성공
        sendMessage(session, connectionMessage);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload().toString();
        
        log.info("ESP32에서 메시지 수신 [{}]: {}", sessionId, payload);
        
        // 빈 메시지나 공백만 있는 메시지 처리
        if (payload == null || payload.trim().isEmpty()) {
            log.warn("빈 메시지 수신 [{}]", sessionId);
            Map<String, Object> errorMessage = new LinkedHashMap<>();
            errorMessage.put("type", "ERROR" + "\n");
            errorMessage.put("message", "빈 메시지입니다." + "\n");
            errorMessage.put("color", "#FF0000" + "\n"); // 빨간색 - 에러
            sendMessage(session, errorMessage);
            return;
        }
        
        try {
            // JSON 메시지 파싱 시도
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            String type = (String) messageData.get("type");
            Object data = messageData.get("data");
            
            log.info("메시지 타입: {}, 데이터: {}", type, data);
            
            // 메시지 타입에 따른 처리
            switch (type) {
                case "ping":
                    handlePing(session, data);
                    break;
                case "status":
                    handleStatus(session, data);
                    break;
                case "image_upload":
                    handleImageUpload(session, data);
                    break;
                case "user_login":
                    handleUserLogin(session, messageData);
                    break;
                case "book_selection":
                    handleBookSelection(session, messageData);
                    break;
                case "session_start":
                    handleSessionStart(session, messageData);
                    break;
                case "next_book":
                    handleNextBook(session, messageData);
                    break;
                default:
                    log.warn("알 수 없는 메시지 타입: {}", type);
                    Map<String, Object> errorMessage = new LinkedHashMap<>();
                    errorMessage.put("type", "ERROR" + "\n");
                    errorMessage.put("message", "알 수 없는 메시지 타입: " + type + "\n");
                    errorMessage.put("color", "#FF0000" + "\n"); // 빨간색 - 에러
                    sendMessage(session, errorMessage);
            }
            
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            log.error("JSON 파싱 오류 [{}]: {}", sessionId, e.getMessage());
            log.error("수신된 페이로드: '{}'", payload);
            Map<String, Object> errorMessage = new LinkedHashMap<>();
            errorMessage.put("type", "ERROR" + "\n");
            errorMessage.put("message", "잘못된 JSON 형식입니다." + "\n");
            errorMessage.put("color", "#FF0000" + "\n"); // 빨간색 - 에러
            sendMessage(session, errorMessage);
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생 [{}]: {}", sessionId, e.getMessage());
            Map<String, Object> errorMessage = new LinkedHashMap<>();
            errorMessage.put("pageNumber", 0 + "\n");
            errorMessage.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n");
            errorMessage.put("color", "#FF0000" + "\n"); // 빨간색 - 에러
            sendMessage(session, errorMessage);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        log.error("WebSocket 전송 오류 [{}]: {}", sessionId, exception.getMessage());
        
        // 세션 제거 및 데이터 정리
        sessions.remove(sessionId);
        sessionUserIds.remove(sessionId);
        sessionBookIds.remove(sessionId);
        sessionBookLists.remove(sessionId);
        sessionCurrentBookIndex.remove(sessionId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        
        // 세션별 데이터 정리
        sessionUserIds.remove(sessionId);
        sessionBookIds.remove(sessionId);
        sessionBookLists.remove(sessionId);
        sessionCurrentBookIndex.remove(sessionId);
        
        log.info("ESP32 WebSocket 연결 종료 [{}]: {}", sessionId, closeStatus);
        log.info("현재 활성 세션 수: {}", sessions.size());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    // 메시지 처리 메서드들
    private void handlePing(WebSocketSession session, Object data) throws Exception {
        log.info("Ping 메시지 수신: {}", data);
        Map<String, Object> pongMessage = new LinkedHashMap<>();
        pongMessage.put("pageNumber", 0 + "\n");
        pongMessage.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n");
        pongMessage.put("color", "#00FF00" + "\n"); // 초록색 - 정상 응답
        sendMessage(session, pongMessage);
    }

    private void handleStatus(WebSocketSession session, Object data) throws Exception {
        log.info("상태 요청 수신: {}", data);
        
        Map<String, Object> statusMessage = new LinkedHashMap<>();
        statusMessage.put("pageNumber", sessions.size() + "\n");
        statusMessage.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n");
        statusMessage.put("color", "#00FF00" + "\n"); // 초록색 - 정상 상태
        
        sendMessage(session, statusMessage);
    }

    private void handleImageUpload(WebSocketSession session, Object data) throws Exception {
        log.info("이미지 업로드 요청 수신: {}", data);
        
        // 이미지 업로드는 REST API를 통해 처리하도록 안내
        Map<String, Object> infoMessage = new LinkedHashMap<>();
        infoMessage.put("pageNumber", 0 + "\n");
        infoMessage.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n");
        infoMessage.put("color", "#FFFF00" + "\n"); // 노란색 - 안내 메시지
        sendMessage(session, infoMessage);
    }

    /**
     * ESP32에서 사용자 로그인 메시지 처리
     */
    private void handleUserLogin(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        String sessionId = session.getId();
        Long userId = Long.valueOf(messageData.get("userId").toString());
        
        log.info("ESP32에서 사용자 로그인 수신 [{}]: userId={}", sessionId, userId);
        
        // 세션별 user_id 저장
        sessionUserIds.put(sessionId, userId);
        
        // 사용자의 등록된 책 목록 조회
        List<Map<String, Object>> userBooks = getUserBooks(userId);
        
        // 세션별 책 목록과 인덱스 저장
        sessionBookLists.put(sessionId, userBooks);
        sessionCurrentBookIndex.put(sessionId, 0);
        
        // 첫 번째 책만 전송
        if (!userBooks.isEmpty()) {
            Map<String, Object> firstBook = userBooks.get(0);
            String bookText = formatSingleBookAsText(userId, firstBook, 0, userBooks.size());
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("text", bookText);
            
            sendMessage(session, response);
        } else {
            // 책이 없는 경우
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("text", "유저: " + userId + "\\n\n등록된 책이 없습니다.\n\n색깔: #FF0000");
            
            sendMessage(session, response);
        }
    }
    
    /**
     * ESP32에서 책 선택 메시지 처리
     */
    private void handleBookSelection(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        String sessionId = session.getId();
        Long userId = Long.valueOf(messageData.get("userId").toString());
        Long bookId = Long.valueOf(messageData.get("bookId").toString());
        
        log.info("ESP32에서 책 선택 수신 [{}]: userId={}, bookId={}", sessionId, userId, bookId);
        
        // 세션별 user_id와 book_id 저장
        sessionUserIds.put(sessionId, userId);
        sessionBookIds.put(sessionId, bookId);
        
        // 세션 서비스에 정보 저장
        sessionService.setUserIdForSession(sessionId, userId);
        sessionService.setBookIdForSession(sessionId, bookId);
        
        // 선택된 책 정보 조회
        Map<String, Object> selectedBook = getBookInfo(bookId);
        
        // ESP32에 확인 메시지 전송 (JSON 형식)
        StringBuilder textBuilder = new StringBuilder();
        textBuilder.append("BOOK_SELECTED\n");
        textBuilder.append("user : ").append(userId).append("\n");
        textBuilder.append("book : ").append(bookId).append("\n");
        textBuilder.append("책 제목 : ").append(selectedBook.get("title")).append("\n");
        textBuilder.append("상태 : success\n");
        textBuilder.append("색깔:#00FF00\n");
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("type", "book_selection_response" + "\n");
        response.put("text", textBuilder.toString());
        response.put("color", "#00FF00" + "\n");
        
        sendMessage(session, response);
    }

    /**
     * ESP32에서 다음 책 요청 처리
     */
    private void handleNextBook(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        String sessionId = session.getId();
        Long userId = sessionUserIds.get(sessionId);
        
        if (userId == null) {
            log.warn("세션에 사용자 ID가 없습니다: {}", sessionId);
            return;
        }
        
        List<Map<String, Object>> userBooks = sessionBookLists.get(sessionId);
        Integer currentIndex = sessionCurrentBookIndex.get(sessionId);
        
        if (userBooks == null || userBooks.isEmpty() || currentIndex == null) {
            log.warn("책 목록이 없습니다: sessionId={}", sessionId);
            return;
        }
        
        // 다음 인덱스로 이동 (순환)
        int nextIndex = (currentIndex + 1) % userBooks.size();
        sessionCurrentBookIndex.put(sessionId, nextIndex);
        
        Map<String, Object> nextBook = userBooks.get(nextIndex);
        String bookText = formatSingleBookAsText(userId, nextBook, nextIndex, userBooks.size());
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("text", bookText);
        
        sendMessage(session, response);
        
        log.info("다음 책으로 이동 [{}]: userId={}, bookId={}, index={}/{}", 
                sessionId, userId, nextBook.get("bookId"), nextIndex + 1, userBooks.size());
    }
    

    /**
     * ESP32에서 세션 시작 메시지 처리
     */
    private void handleSessionStart(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        String sessionId = session.getId();
        Long bookId = Long.valueOf(messageData.get("bookId").toString());
        Long userId = Long.valueOf(messageData.get("userId").toString());
        
        log.info("ESP32에서 세션 시작 수신 [{}]: bookId={}, userId={}", sessionId, bookId, userId);
        
        // 세션별 book_id 저장
        sessionBookIds.put(sessionId, bookId);
        
        // 세션 서비스에 세션 시작
        sessionService.startSession(sessionId, bookId, userId);
        
        // ESP32에 확인 메시지 전송
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("type", "SESSION_STARTED" + "\n");
        response.put("bookId", bookId + "\n");
        response.put("userId", userId + "\n");
        response.put("status", "success" + "\n");
        response.put("message", "읽기 세션이 시작되었습니다." + "\n");
        response.put("color", "#00FF00" + "\n"); // 초록색 - 세션 시작 성공
        
        sendMessage(session, response);
    }

    /**
     * OCR 결과를 웹소켓으로 전송
     */
    public void sendOCRResult(int pageNumber) {
        log.info("OCR 결과 웹소켓 전송: 페이지 {}", pageNumber);

        Map<String, Object> ocrData = new LinkedHashMap<>();
        ocrData.put("pageNumber", pageNumber + "\n");
        ocrData.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n");
        ocrData.put("color", "#00FF00" + "\n"); // 초록색 - OCR 성공

        broadcastSimpleMessage(ocrData);
    }
    
    /**
     * 독서 완료 결과를 ESP32에게 전송
     */
    public void sendReadingCompleteResult(int finalPage, Map<String, Object> readingResult) {
        log.info("독서 완료 결과 웹소켓 전송: 마지막 페이지 {}", finalPage);

        Map<String, Object> completeData = new LinkedHashMap<>();
        completeData.put("type", "READING_COMPLETE");
        completeData.put("finalPage", finalPage + "\n");
        completeData.put("readingStartDate", readingResult.get("readingStartDate") + "\n");
        completeData.put("totalReadingDays", readingResult.get("totalReadingDays") + "\n");
        completeData.put("startPage", readingResult.get("startPage") + "\n");
        completeData.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n");
        completeData.put("color", "#00FF00" + "\n"); // 초록색 - 독서 완료

        broadcastSimpleMessage(completeData);
    }
    
    /**
     * 현재 페이지 업데이트 결과를 ESP32에게 전송
     */
    public void sendCurrentPageUpdate(int currentPage) {
        log.info("현재 페이지 업데이트 웹소켓 전송: 페이지 {}", currentPage);

        Map<String, Object> updateData = new LinkedHashMap<>();
        updateData.put("type", "PAGE_UPDATE");
        updateData.put("currentPage", currentPage + "\n");
        updateData.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n");
        updateData.put("color", "#00FF00" + "\n"); // 초록색 - 페이지 업데이트 성공

        broadcastSimpleMessage(updateData);
    }
    
    /**
     * OCR 결과를 ESP32에게 전송
     */
    public void sendOCRResultToESP32(Long bookId, Long userId, com.example.ReadMark.model.dto.VisionAnalysisResultDTO visionResult) {
        log.info("OCR 결과를 ESP32에게 전송: bookId={}, userId={}, pageNumber={}", 
                bookId, userId, visionResult.getEstimatedPageNumber());

        Map<String, Object> ocrData = new LinkedHashMap<>();
        ocrData.put("type", "OCR_RESULT" + "\n");
        ocrData.put("bookId", bookId + "\n");
        ocrData.put("userId", userId + "\n");
        ocrData.put("pageNumber", visionResult.getEstimatedPageNumber() + "\n");
        ocrData.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n");
        ocrData.put("color", "#00FF00" + "\n"); // 초록색 - OCR 성공

        broadcastSimpleMessage(ocrData);
    }

    /**
     * 이미지 업로드 완료 알림을 웹소켓으로 전송
     */
    public void sendImageUploadComplete(int pageNumber) {
        log.info("이미지 업로드 완료 웹소켓 전송: 페이지 번호 {}", pageNumber);
        
        Map<String, Object> uploadData = new LinkedHashMap<>();
        uploadData.put("pageNumber", pageNumber + "\n");
        uploadData.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n");
        uploadData.put("color", "#00FF00" + "\n"); // 초록색 - 업로드 성공
        
        broadcastSimpleMessage(uploadData);
    }

    /**
     * 독서 세션 정보를 웹소켓으로 전송
     */
    public void sendReadingSessionInfo(int totalPages) {
        log.info("독서 세션 정보 웹소켓 전송: 총 {}페이지", totalPages);
        
        Map<String, Object> sessionData = new LinkedHashMap<>();
        sessionData.put("pageNumber", totalPages + "\n");
        sessionData.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n");
        sessionData.put("color", "#00FF00" + "\n"); // 초록색 - 세션 정보
        
        broadcastSimpleMessage(sessionData);
    }
    
    /**
     * 사용자의 등록된 책 목록 조회
     */
    private List<Map<String, Object>> getUserBooks(Long userId) {
        try {
            return userBookService.getAllUserBooks(userId).stream()
                    .map(userBook -> {
                        Map<String, Object> bookInfo = new LinkedHashMap<>();
                        bookInfo.put("bookId", userBook.getBook().getBookId());
                        bookInfo.put("title", userBook.getBook().getTitle());
                        bookInfo.put("author", userBook.getBook().getAuthor());
                        bookInfo.put("coverImageUrl", userBook.getBook().getCoverImageUrl());
                        return bookInfo;
                    })
                    .toList();
        } catch (Exception e) {
            log.error("사용자 책 목록 조회 실패: userId={}", userId, e);
            return List.of();
        }
    }
    
    /**
     * 책 목록을 ESP32 OLED용 텍스트 형식으로 변환
     */
    private String formatBookListAsText(Long userId, List<Map<String, Object>> userBooks) {
        StringBuilder textBuilder = new StringBuilder();
        
        // 유저 정보 먼저 추가
        textBuilder.append("유저: ").append(userId).append("\\n");
        textBuilder.append("\n");
        
        // 각 책에 대해 텍스트 형식으로 변환
        for (Map<String, Object> book : userBooks) {
            textBuilder.append("책: ").append(book.get("bookId")).append("\\n");
            textBuilder.append("\n");
            textBuilder.append("제목: ").append(book.get("title")).append("\\n");
            textBuilder.append("\n");
            textBuilder.append("\n");
        }
        
        // 마지막에 색깔 정보 추가
        textBuilder.append("색깔: #00FF00");
        
        return textBuilder.toString();
    }
    
    /**
     * 단일 책을 ESP32 OLED용 텍스트 형식으로 변환
     */
    private String formatSingleBookAsText(Long userId, Map<String, Object> book, int currentIndex, int totalBooks) {
        StringBuilder textBuilder = new StringBuilder();
        
        // 유저 정보
        textBuilder.append("유저: ").append(userId).append("\\n");
        textBuilder.append("\n");
        
        // 책 정보
        textBuilder.append("책: ").append(book.get("bookId")).append("\\n");
        textBuilder.append("\n");
        textBuilder.append("제목: ").append(book.get("title")).append("\\n");
        textBuilder.append("\n");
        
        // 페이지 정보
        textBuilder.append("(").append(currentIndex + 1).append("/").append(totalBooks).append(")\\n");
        textBuilder.append("\n");
        
        // 색깔 정보
        textBuilder.append("색깔: #00FF00");
        
        return textBuilder.toString();
    }
    
    /**
     * 책 정보 조회
     */
    private Map<String, Object> getBookInfo(Long bookId) {
        try {
            var book = bookService.getBookById(bookId);
            Map<String, Object> bookInfo = new LinkedHashMap<>();
            bookInfo.put("bookId", book.getBookId());
            bookInfo.put("title", book.getTitle());
            bookInfo.put("author", book.getAuthor());
            bookInfo.put("coverImageUrl", book.getCoverImageUrl());
            return bookInfo;
        } catch (Exception e) {
            log.error("책 정보 조회 실패: bookId={}", bookId, e);
            Map<String, Object> defaultInfo = new LinkedHashMap<>();
            defaultInfo.put("bookId", bookId);
            defaultInfo.put("title", "알 수 없는 책");
            defaultInfo.put("author", "");
            defaultInfo.put("coverImageUrl", "");
            return defaultInfo;
        }
    }

    // 유틸리티 메서드들
    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            if (session.isOpen()) {
                // 항상 JSON으로 전송
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
                log.debug("JSON 메시지 전송 완료: {}", jsonMessage);
            } else {
                log.warn("세션이 닫혀있어 메시지를 전송할 수 없습니다: {}", session.getId());
            }
        } catch (IOException e) {
            log.error("메시지 전송 중 오류 발생: {}", e.getMessage());
        }
    }

    // 공개 메서드들

    public void broadcastSimpleMessage(Map<String, Object> data) {
        log.info("간단한 브로드캐스트 메시지 전송: {}", data);
        
        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                sendMessage(session, data);
            }
        });
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }

    public boolean isSessionActive(String sessionId) {
        return sessions.containsKey(sessionId);
    }
}
