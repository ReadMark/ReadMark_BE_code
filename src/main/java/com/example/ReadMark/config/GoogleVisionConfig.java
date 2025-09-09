package com.example.ReadMark.config;

import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
@Slf4j
public class GoogleVisionConfig {
    
    @Value("${google.vision.api.key:}")
    private String apiKey;
    
    @Value("${google.vision.project.id:}")
    private String projectId;
    
    /**
     * Google Vision API 클라이언트를 생성합니다.
     * 환경 변수 GOOGLE_APPLICATION_CREDENTIALS에 서비스 계정 키 파일 경로를 설정해야 합니다.
     */
    @Bean
    public ImageAnnotatorClient imageAnnotatorClient() throws IOException {
        try {
            // 환경 변수에서 인증 정보를 읽어옵니다.
            // GOOGLE_APPLICATION_CREDENTIALS 환경 변수를 설정해야 합니다.
            ImageAnnotatorClient client = ImageAnnotatorClient.create();
            log.info("Google Vision API 클라이언트가 성공적으로 생성되었습니다. Project ID: {}", projectId);
            return client;
        } catch (IOException e) {
            log.error("Google Vision API 클라이언트 생성 실패", e);
            log.error("GOOGLE_APPLICATION_CREDENTIALS 환경 변수를 설정해주세요.");
            throw e;
        }
    }
    
    /**
     * Google Vision API 설정을 확인합니다.
     */
    @Bean
    public boolean checkVisionApiConfiguration() {
        try {
            // 환경 변수 확인
            String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
            if (credentialsPath == null || credentialsPath.trim().isEmpty()) {
                log.warn("GOOGLE_APPLICATION_CREDENTIALS 환경 변수가 설정되지 않았습니다.");
                log.warn("Google Vision API를 사용하려면 서비스 계정 키 파일 경로를 설정해주세요.");
                return false;
            }
            
            log.info("Google Vision API 설정 확인 완료");
            log.info("Credentials Path: {}", credentialsPath);
            return true;
            
        } catch (Exception e) {
            log.error("Google Vision API 설정 확인 실패", e);
            return false;
        }
    }
}
