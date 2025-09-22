package com.example.ReadMark.config;

import com.example.ReadMark.handler.ESP32WebSocketHandler;
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

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("WebSocket 핸들러 등록 시작: /ws");
        
        // 기본 웹소켓 핸들러 등록
        registry.addHandler(esp32WebSocketHandler, "/ws")
                .setAllowedOrigins("*");
        
        // SockJS 폴백 지원 (브라우저 호환성)
        registry.addHandler(esp32WebSocketHandler, "/ws")
                .setAllowedOrigins("*")
                .withSockJS();
        
        log.info("WebSocket 핸들러 등록 완료: /ws (SockJS 지원 포함)");
    }
}
