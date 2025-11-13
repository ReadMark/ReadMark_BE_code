package com.example.ReadMark.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.ReadMark.service.ESP32ReadingSessionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ESP32CAMWebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ESP32ReadingSessionService sessionService;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        
        log.info("ESP32-CAM WebSocket 연결됨: {}", sessionId);
        log.info("현재 활성 ESP32-CAM 세션 수: {}", sessions.size());
        
        // 연결 확인 메시지 전송
        Map<String, Object> connectionMessage = new LinkedHashMap<>();
        connectionMessage.put("status", "connected" + "\n");
        connectionMessage.put("message", "ESP32-CAM 연결됨" + "\n");
        connectionMessage.put("color", "#00FF00" + "\n");
        sendMessage(session, connectionMessage);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload().toString();
        
        log.info("ESP32-CAM에서 메시지 수신 [{}]: {}", sessionId, payload);
        
        try {
            // JSON 메시지 파싱 시도
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            String type = (String) messageData.get("type");
            Object data = messageData.get("data");
            
            log.info("ESP32-CAM 메시지 타입: {}, 데이터: {}", type, data);
            
            // 메시지 타입에 따른 처리
            switch (type) {
                case "ocr_result":
                    handleOCRResult(session, data);
                    break;
                case "ping":
                    handlePing(session, data);
                    break;
                default:
                    log.warn("알 수 없는 ESP32-CAM 메시지 타입: {}", type);
                    Map<String, Object> errorMessage = new LinkedHashMap<>();
                    errorMessage.put("status", "error" + "\n");
                    errorMessage.put("message", "알 수 없는 메시지 타입: " + type + "\n");
                    errorMessage.put("color", "#FF0000" + "\n"); // 빨간색 - 에러
                    sendMessage(session, errorMessage);
            }
            
        } catch (Exception e) {
            log.error("ESP32-CAM 메시지 처리 중 오류 발생 [{}]: {}", sessionId, e.getMessage());
            Map<String, Object> errorMessage = new LinkedHashMap<>();
            errorMessage.put("status", "error" + "\n");
            errorMessage.put("message", "메시지 처리 중 오류 발생: " + e.getMessage() + "\n");
            errorMessage.put("color", "#FF0000" + "\n"); // 빨간색 - 에러
            sendMessage(session, errorMessage);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        log.error("ESP32-CAM WebSocket 전송 오류 [{}]: {}", sessionId, exception.getMessage());
        
        // 세션 제거
        sessions.remove(sessionId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        
        log.info("ESP32-CAM WebSocket 연결 종료 [{}]: {}", sessionId, closeStatus);
        log.info("현재 활성 ESP32-CAM 세션 수: {}", sessions.size());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    /**
     * ESP32-CAM에서 OCR 결과 처리
     */
    private void handleOCRResult(WebSocketSession session, Object data) throws Exception {
        String sessionId = session.getId();
        
        log.info("ESP32-CAM에서 OCR 결과 수신 [{}]: {}", sessionId, data);
        
        // OCR 결과에서 사용자 ID와 책 ID 추출 시도
        Long bookId = null;
        Long userId = null;
        
        try {
            // data가 Map인 경우 직접 추출
            if (data instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) data;
                if (dataMap.containsKey("userId")) {
                    userId = Long.valueOf(dataMap.get("userId").toString());
                }
                if (dataMap.containsKey("bookId")) {
                    bookId = Long.valueOf(dataMap.get("bookId").toString());
                }
            }
            
            // OCR 결과 텍스트에서 사용자 ID 추출 시도 (예: "id: 2 page: 300")
            if (userId == null && data instanceof Map) {
                Map<String, Object> dataMap = (Map<String, Object>) data;
                if (dataMap.containsKey("text")) {
                    String text = dataMap.get("text").toString();
                    if (text.contains("id:") && text.contains("page:")) {
                        try {
                            String[] parts = text.split("page:");
                            if (parts.length > 0) {
                                String idPart = parts[0].trim();
                                if (idPart.contains("id:")) {
                                    String idStr = idPart.substring(idPart.indexOf("id:") + 3).trim();
                                    userId = Long.valueOf(idStr);
                                    log.info("OCR 텍스트에서 사용자 ID 추출: {}", userId);
                                }
                            }
                        } catch (Exception e) {
                            log.warn("OCR 텍스트에서 사용자 ID 추출 실패: {}", e.getMessage());
                        }
                    }
                }
            }
        } catch (Exception e) {
            log.warn("OCR 결과에서 직접 사용자/책 ID 추출 실패: {}", e.getMessage());
        }
        
        // 직접 추출이 실패한 경우 활성 세션에서 가져오기
        if (userId == null || bookId == null) {
            Map<String, Long> allBookIds = sessionService.getAllBookIds();
            Map<String, Long> allUserIds = sessionService.getAllUserIds();
            
            log.info("ESP32-CAM 활성 세션 수: bookIds={}, userIds={}", allBookIds.size(), allUserIds.size());
            
            if (!allBookIds.isEmpty()) {
                bookId = allBookIds.values().iterator().next();
            }
            
            if (!allUserIds.isEmpty()) {
                userId = allUserIds.values().iterator().next();
            }
        }
        
        log.info("OCR 결과 처리용 사용자/책 ID: userId={}, bookId={}", userId, bookId);
        
        if (bookId != null && userId != null) {
            // OCR 결과를 데이터베이스에 저장
            saveOCRResultToDatabase(bookId, userId, data);
            
            // ESP32-CAM에 확인 메시지 전송
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("type", "OCR_RESULT_SAVED" + "\n");
            response.put("bookId", bookId + "\n");
            response.put("userId", userId + "\n");
            response.put("status", "success" + "\n");
            response.put("message", "OCR 결과가 저장되었습니다." + "\n");
            response.put("color", "#00FF00" + "\n"); // 초록색 - OCR 저장 성공
            
            sendMessage(session, response);
        } else {
            log.warn("활성 세션이 없어서 OCR 결과 저장 건너뜀");
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("type", "OCR_RESULT_ERROR" + "\n");
            response.put("status", "error" + "\n");
            response.put("message", "활성 세션이 없습니다. ESP32에서 먼저 책을 선택해주세요." + "\n");
            response.put("color", "#FF0000" + "\n"); // 빨간색 - 세션 없음 에러
            
            sendMessage(session, response);
        }
    }

    /**
     * OCR 결과를 데이터베이스에 저장
     */
    private void saveOCRResultToDatabase(Long bookId, Long userId, Object ocrData) {
        try {
            // OCR 데이터 파싱
            Map<String, Object> ocrMap = objectMapper.convertValue(ocrData, Map.class);
            
            String detectedNumbers = (String) ocrMap.get("detectedNumbers");
            Double confidence = (Double) ocrMap.get("confidence");
            Integer pageNumber = (Integer) ocrMap.get("pageNumber");
            
            log.info("OCR 결과 저장: bookId={}, userId={}, pageNumber={}, confidence={}", 
                    bookId, userId, pageNumber, confidence);
            
            // TODO: BookPageService를 사용하여 데이터베이스에 저장
            // BookPageDTO bookPageDTO = new BookPageDTO();
            // bookPageDTO.setBookId(bookId);
            // bookPageDTO.setPageNumber(pageNumber);
            // bookPageDTO.setConfidence(confidence);
            // bookPageDTO.setDetectedNumbers(detectedNumbers);
            // bookPageService.saveBookPage(bookPageDTO);
            
        } catch (Exception e) {
            log.error("OCR 결과 저장 중 오류 발생", e);
        }
    }

    private void handlePing(WebSocketSession session, Object data) throws Exception {
        log.info("ESP32-CAM Ping 수신: {}", data);
        
        Map<String, Object> pongMessage = new LinkedHashMap<>();
        pongMessage.put("type", "pong" + "\n");
        pongMessage.put("status", "alive" + "\n");
        pongMessage.put("date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n");
        pongMessage.put("color", "#00FF00" + "\n"); // 초록색 - 정상 응답
        
        sendMessage(session, pongMessage);
    }

    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            String jsonMessage = objectMapper.writeValueAsString(message);
            session.sendMessage(new TextMessage(jsonMessage));
            log.info("ESP32-CAM에 메시지 전송: {}", jsonMessage);
        } catch (IOException e) {
            log.error("ESP32-CAM에 메시지 전송 실패", e);
        }
    }
}
