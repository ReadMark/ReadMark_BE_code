package com.example.ReadMark.config;

import com.example.ReadMark.handler.ESP32WebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;
import org.springframework.web.socket.server.RequestUpgradeStrategy;
import org.springframework.web.socket.server.standard.UndertowRequestUpgradeStrategy;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

@Configuration
@EnableWebSocket
@RequiredArgsConstructor
@Slf4j
public class WebSocketConfig implements WebSocketConfigurer {

    private final ESP32WebSocketHandler esp32WebSocketHandler;

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        log.info("WebSocket 핸들러 등록 시작: /ws");
        registry.addHandler(esp32WebSocketHandler, "/ws")
                .setHandshakeHandler(handshakeHandler())
                .setAllowedOrigins("*"); // ESP32에서 접근 가능하도록 모든 origin 허용
        log.info("WebSocket 핸들러 등록 완료: /ws");
    }
    @Bean
    public DefaultHandshakeHandler handshakeHandler() {
        return new DefaultHandshakeHandler(upgradeStrategy());
    }

    @Bean
    public RequestUpgradeStrategy upgradeStrategy() {
        return new UndertowRequestUpgradeStrategy();
}}
