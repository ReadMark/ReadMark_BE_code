package com.example.ReadMark.config;

import com.example.ReadMark.handler.ESP32WebSocketHandler;
import com.example.ReadMark.handler.ESP32CAMWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketConfigurer {

    private final ESP32WebSocketHandler esp32WebSocketHandler;
    private final ESP32CAMWebSocketHandler esp32CAMWebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("WebSocket 핸들러 등록 시작");
        
        // ESP32 WebSocket 핸들러 등록
        registry.addHandler(esp32WebSocketHandler, "/ws/esp32")
                .setAllowedOrigins("*");
        
        // ESP32-CAM WebSocket 핸들러 등록
        registry.addHandler(esp32CAMWebSocketHandler, "/ws/esp32-cam")
                .setAllowedOrigins("*");
        
        // 기본 /ws 경로도 ESP32 핸들러로 매핑 (호환성)
        registry.addHandler(esp32WebSocketHandler, "/ws")
                .setAllowedOrigins("*");
        
        // SockJS 폴백 지원 (브라우저 호환성)
        registry.addHandler(esp32WebSocketHandler, "/ws/esp32")
                .setAllowedOrigins("*")
                .withSockJS();
        
        registry.addHandler(esp32CAMWebSocketHandler, "/ws/esp32-cam")
                .setAllowedOrigins("*")
                .withSockJS();
        
        registry.addHandler(esp32WebSocketHandler, "/ws")
                .setAllowedOrigins("*")
                .withSockJS();
        
        log.info("WebSocket 핸들러 등록 완료: /ws, /ws/esp32, /ws/esp32-cam (SockJS 지원 포함)");
    }
}
