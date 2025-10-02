package com.example.ReadMark.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // 모든 엔드포인트 허용
                .allowedOrigins("*", "http://localhost:5173", "http://127.0.0.1:5173") // localhost:5173 포함 모든 출처 허용
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS") // 메서드 허용
                .allowedHeaders("*") // 헤더 허용
                .allowCredentials(false)
                .maxAge(3600);
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 프로필 이미지 정적 리소스 매핑
        registry.addResourceHandler("/uploads/profile-images/**")
                .addResourceLocations("file:uploads/profile-images/");
        
        // 책 표지 이미지 정적 리소스 매핑
        registry.addResourceHandler("/uploads/book-covers/**")
                .addResourceLocations("file:uploads/book-covers/");
        
        // 즐겨찾기 문장 책 표지 이미지 정적 리소스 매핑
        registry.addResourceHandler("/uploads/favorite-quote-covers/**")
                .addResourceLocations("file:uploads/favorite-quote-covers/");
        
        // 일반 uploads 경로 매핑
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:uploads/");
    }
}
