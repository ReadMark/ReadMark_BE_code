package com.example.ReadMark.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ESP32WebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

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
        sendMessage(session, connectionMessage);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload().toString();
        
        log.info("ESP32에서 메시지 수신 [{}]: {}", sessionId, payload);
        
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
                       default:
                           log.warn("알 수 없는 메시지 타입: {}", type);
                           Map<String, Object> errorMessage = new LinkedHashMap<>();
                           errorMessage.put("pageNumber", 0 + "\n");
                           errorMessage.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n");
                           sendMessage(session, errorMessage);
            }
            
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생 [{}]: {}", sessionId, e.getMessage());
            Map<String, Object> errorMessage = new LinkedHashMap<>();
            errorMessage.put("pageNumber", 0 + "\n");
            errorMessage.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n");
            sendMessage(session, errorMessage);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        log.error("WebSocket 전송 오류 [{}]: {}", sessionId, exception.getMessage());
        
        // 세션 제거
        sessions.remove(sessionId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        
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
        sendMessage(session, pongMessage);
    }

    private void handleStatus(WebSocketSession session, Object data) throws Exception {
        log.info("상태 요청 수신: {}", data);
        
        Map<String, Object> statusMessage = Map.of(
            "pageNumber", sessions.size(),
            "date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n"
        );
        
        sendMessage(session, statusMessage);
    }

    private void handleImageUpload(WebSocketSession session, Object data) throws Exception {
        log.info("이미지 업로드 요청 수신: {}", data);
        
        // 이미지 업로드는 REST API를 통해 처리하도록 안내
        Map<String, Object> infoMessage = Map.of(
            "pageNumber", 0 + "\n",
            "date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n"
        );
        sendMessage(session, infoMessage);
    }

    /**
     * OCR 결과를 웹소켓으로 전송
     */
    public void sendOCRResult(int pageNumber) {
        log.info("OCR 결과 웹소켓 전송: 페이지 {}", pageNumber);
        
        Map<String, Object> ocrData = Map.of(
            "pageNumber", pageNumber + "\n",
            "date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n"
        );
        
        broadcastSimpleMessage(ocrData);
    }

    /**
     * 이미지 업로드 완료 알림을 웹소켓으로 전송
     */
    public void sendImageUploadComplete(int pageNumber) {
        log.info("이미지 업로드 완료 웹소켓 전송: 페이지 번호 {}", pageNumber);
        
        Map<String, Object> uploadData = Map.of(
            "pageNumber", pageNumber + "\n",
            "date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n"
        );
        
        broadcastSimpleMessage(uploadData);
    }

    /**
     * 독서 세션 정보를 웹소켓으로 전송
     */
    public void sendReadingSessionInfo(int totalPages) {
        log.info("독서 세션 정보 웹소켓 전송: 총 {}페이지", totalPages);
        
        Map<String, Object> sessionData = Map.of(
            "pageNumber", totalPages + "\n",
            "date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n"
        );
        
        broadcastSimpleMessage(sessionData);
    }

    // 유틸리티 메서드들
    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            if (session.isOpen()) {
                // 직접 JSON 문자열 생성 (개행문자 이스케이프 처리)
                String pageNumber = message.get("pageNumber").toString().replace("\n", "\\n");
                String date = message.get("date").toString().replace("\n", "\\n");
                String jsonMessage = String.format("{\"pageNumber\":\"%s\",\"date\":\"%s\"}", 
                    pageNumber, date);
                session.sendMessage(new TextMessage(jsonMessage));
                log.debug("메시지 전송 완료: {}", jsonMessage);
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
