package com.example.ReadMark.config;

import com.google.api.gax.core.FixedCredentialsProvider;
import com.google.auth.oauth2.GoogleCredentials;
import com.google.cloud.vision.v1.ImageAnnotatorClient;
import com.google.cloud.vision.v1.ImageAnnotatorSettings;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Configuration
@Slf4j
public class GoogleVisionConfig {

    @Value("${google.vision.enabled:true}")
    private boolean visionEnabled;

    @Bean
    public ImageAnnotatorClient imageAnnotatorClient() throws IOException {
        if (!visionEnabled) {
            throw new IllegalStateException("Google Vision API가 비활성화되어 있습니다.");
        }

        String credentialsPath = System.getenv("GOOGLE_APPLICATION_CREDENTIALS");
        if (credentialsPath == null || credentialsPath.trim().isEmpty()) {
            throw new IllegalStateException("GOOGLE_APPLICATION_CREDENTIALS 환경 변수가 설정되지 않았습니다.");
        }

        File credentialFile = new File(credentialsPath);
        if (!credentialFile.exists()) {
            throw new IllegalStateException("서비스 계정 키 파일을 찾을 수 없습니다: " + credentialsPath);
        }

        GoogleCredentials credentials = GoogleCredentials.fromStream(new FileInputStream(credentialFile));
        ImageAnnotatorClient client = ImageAnnotatorClient.create(
                ImageAnnotatorSettings.newBuilder()
                        .setCredentialsProvider(FixedCredentialsProvider.create(credentials))
                        .build()
        );

        log.info("Google Vision API 클라이언트 생성 완료. Credentials Path: {}", credentialsPath);
        return client;
    }
}