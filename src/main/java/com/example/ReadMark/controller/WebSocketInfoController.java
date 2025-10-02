package com.example.ReadMark.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/ws")
public class WebSocketInfoController {

    @GetMapping
    public ResponseEntity<Map<String, Object>> getWebSocketInfo() {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "WebSocket 엔드포인트 정보");
        response.put("endpoints", Map.of(
            "esp32", "ws://43.200.102.14:5000/ws/esp32",
            "esp32-cam", "ws://43.200.102.14:5000/ws/esp32-cam",
            "default", "ws://43.200.102.14:5000/ws"
        ));
        response.put("note", "WebSocket 프로토콜을 사용하여 연결하세요");
        
        return ResponseEntity.ok(response);
    }
}
