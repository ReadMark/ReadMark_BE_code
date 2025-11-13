package com.example.ReadMark.config;

import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.servlet.ServletContextInitializer;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;

@Slf4j
@Component
public class ServerStartupListener implements ApplicationListener<ContextRefreshedEvent> {

    @Value("${server.port:5000}")
    private int serverPort;

    @Value("${server.address:0.0.0.0}")
    private String serverAddress;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        try {
            String hostAddress = InetAddress.getLocalHost().getHostAddress();
            String hostName = InetAddress.getLocalHost().getHostName();
            
            log.info("==========================================");
            log.info("서버가 성공적으로 시작되었습니다!");
            log.info("서버 주소: {}", serverAddress);
            log.info("서버 포트: {}", serverPort);
            log.info("로컬 호스트 주소: {}", hostAddress);
            log.info("로컬 호스트 이름: {}", hostName);
            log.info("ESP32 업로드 엔드포인트:");
            log.info("  POST http://{}:{}/api/upload/esp32-cam/upload", hostAddress, serverPort);
            log.info("  POST http://{}:{}/api/upload/esp32-cam/upload", serverAddress.equals("0.0.0.0") ? "43.200.102.14" : serverAddress, serverPort);
            log.info("==========================================");
            
            // 포트 리스닝 확인
            log.info("포트 {}에서 리스닝 중입니다.", serverPort);
            log.info("모든 네트워크 인터페이스에서 연결을 받을 준비가 되었습니다.");
            
        } catch (UnknownHostException e) {
            log.error("호스트 정보를 가져올 수 없습니다.", e);
        }
    }
}


