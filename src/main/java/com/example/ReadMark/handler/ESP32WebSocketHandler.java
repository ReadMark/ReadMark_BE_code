package com.example.ReadMark.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Slf4j
public class ESP32WebSocketHandler implements WebSocketHandler {

    private final Set<WebSocketSession> sessions = Collections.newSetFromMap(new ConcurrentHashMap<>());
    private final ObjectMapper objectMapper = new ObjectMapper();

    public ESP32WebSocketHandler() {
        log.info("ESP32WebSocketHandler Bean 생성됨");
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        sessions.add(session);
        log.info("ESP32 WebSocket 연결됨: {}", session.getId());
        
        // 연결 확인 메시지 전송
        sendMessage(session, createMessage("connected", "WebSocket 연결이 성공적으로 설정되었습니다."));
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        log.info("ESP32로부터 메시지 수신: {}", message.getPayload());
        
        // ESP32로부터의 메시지 처리 (필요시)
        if (message instanceof TextMessage) {
            String payload = ((TextMessage) message).getPayload();
            log.info("ESP32 메시지: {}", payload);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        log.error("WebSocket 전송 오류: {}", exception.getMessage(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        sessions.remove(session);
        log.info("ESP32 WebSocket 연결 종료: {}, 상태: {}", session.getId(), closeStatus);
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * 모든 연결된 ESP32 클라이언트에게 메시지 브로드캐스트
     */
    public void broadcast(String type, String message) {
        String jsonMessage = createMessage(type, message);
        broadcast(jsonMessage);
    }

    /**
     * 모든 연결된 ESP32 클라이언트에게 JSON 메시지 브로드캐스트
     */
    public void broadcast(String message) {
        sessions.forEach(session -> {
            try {
                if (session.isOpen()) {
                    session.sendMessage(new TextMessage(message));
                    log.debug("ESP32에게 메시지 전송 완료: {}", session.getId());
                }
            } catch (Exception e) {
                log.error("ESP32 메시지 전송 실패: {}", e.getMessage(), e);
                sessions.remove(session);
            }
        });
    }

    /**
     * 특정 ESP32 클라이언트에게 메시지 전송
     */
    public void sendMessage(WebSocketSession session, String message) {
        try {
            if (session.isOpen()) {
                session.sendMessage(new TextMessage(message));
                log.debug("특정 ESP32에게 메시지 전송: {}", session.getId());
            }
        } catch (Exception e) {
            log.error("특정 ESP32 메시지 전송 실패: {}", e.getMessage(), e);
        }
    }

    /**
     * 연결된 ESP32 클라이언트 수 반환
     */
    public int getConnectedClientCount() {
        return sessions.size();
    }

    /**
     * JSON 메시지 생성
     */
    private String createMessage(String type, String message) {
        try {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("type", type);
            messageMap.put("message", message);
            messageMap.put("timestamp", System.currentTimeMillis());
            
            return objectMapper.writeValueAsString(messageMap);
        } catch (Exception e) {
            log.error("JSON 메시지 생성 실패: {}", e.getMessage(), e);
            return String.format("{\"type\":\"%s\",\"message\":\"%s\",\"timestamp\":%d}", 
                type, message, System.currentTimeMillis());
        }
    }

    /**
     * 이미지 업로드 알림 메시지 생성
     */
    public void notifyImageUpload(String fileName, long fileSize, boolean isBookPage) {
        try {
            Map<String, Object> messageMap = new HashMap<>();
            messageMap.put("type", "image_uploaded");
            messageMap.put("fileName", fileName);
            messageMap.put("fileSize", fileSize);
            messageMap.put("isBookPage", isBookPage);
            messageMap.put("timestamp", System.currentTimeMillis());
            
            String message = objectMapper.writeValueAsString(messageMap);
            broadcast(message);
            log.info("이미지 업로드 알림 전송: {}", fileName);
        } catch (Exception e) {
            log.error("이미지 업로드 알림 생성 실패: {}", e.getMessage(), e);
        }
    }
}
