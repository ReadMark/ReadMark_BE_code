package com.example.ReadMark.controller;

import com.example.ReadMark.handler.ESP32WebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/esp32/ws")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH}, allowedHeaders = "*", allowCredentials = "false")
public class ESP32WebSocketController {

    private final ESP32WebSocketHandler webSocketHandler;

    /**
     * 웹소켓 연결 상태 확인
     */
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> getWebSocketStatus() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int activeSessions = webSocketHandler.getActiveSessionCount();
            
            response.put("success", true);
            response.put("activeSessions", activeSessions);
            response.put("websocketEnabled", true);
            response.put("endpoint", "ws://43.200.102.14:5000/ws");
            response.put("sockjsEndpoint", "http://43.200.102.14:5000/ws/info");
            response.put("timestamp", System.currentTimeMillis());
            
            log.info("웹소켓 상태 조회: 활성 세션 {}개", activeSessions);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("웹소켓 상태 조회 중 오류 발생: {}", e.getMessage());
            
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 웹소켓 진단 정보
     */
    @GetMapping("/diagnostics")
    public ResponseEntity<Map<String, Object>> getWebSocketDiagnostics() {
        Map<String, Object> response = new HashMap<>();
        
        try {
            int activeSessions = webSocketHandler.getActiveSessionCount();
            
            response.put("success", true);
            response.put("activeSessions", activeSessions);
            response.put("websocketEnabled", true);
            response.put("serverInfo", Map.of(
                "serverAddress", "43.200.102.14",
                "serverPort", 5000,
                "protocol", "WebSocket",
                "supportedProtocols", new String[]{"ws", "wss", "sockjs"}
            ));
            response.put("endpoints", Map.of(
                "websocket", "ws://43.200.102.14:5000/ws",
                "sockjs", "http://43.200.102.14:5000/ws/info",
                "status", "http://43.200.102.14:5000/api/esp32/ws/status"
            ));
            response.put("timestamp", System.currentTimeMillis());
            
            log.info("웹소켓 진단 정보 조회: 활성 세션 {}개", activeSessions);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("웹소켓 진단 정보 조회 중 오류 발생: {}", e.getMessage());
            
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 웹소켓 테스트 메시지 전송
     */
    @PostMapping("/test")
    public ResponseEntity<Map<String, Object>> sendTestMessage(@RequestBody(required = false) Map<String, Object> requestData) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String message = "테스트 메시지";
            Object data = null;
            
            if (requestData != null) {
                message = (String) requestData.getOrDefault("message", "테스트 메시지");
                data = requestData.get("data");
            }
            
            // 모든 활성 세션에 브로드캐스트
            Map<String, Object> testMessage = Map.of(
                "pageNumber", 0 + "\n",
                "date", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd")) + "\n"
            );
            webSocketHandler.broadcastSimpleMessage(testMessage);
            
            int activeSessions = webSocketHandler.getActiveSessionCount();
            
            response.put("success", true);
            response.put("message", "테스트 메시지가 " + activeSessions + "개의 활성 세션에 전송되었습니다.");
            response.put("activeSessions", activeSessions);
            response.put("timestamp", System.currentTimeMillis());
            
            log.info("웹소켓 테스트 메시지 전송: {} (활성 세션 {}개)", message, activeSessions);
            
            return ResponseEntity.ok(response);
            
        } catch (Exception e) {
            log.error("웹소켓 테스트 메시지 전송 중 오류 발생: {}", e.getMessage());
            
            response.put("success", false);
            response.put("error", e.getMessage());
            response.put("timestamp", System.currentTimeMillis());
            
            return ResponseEntity.internalServerError().body(response);
        }
    }
}
