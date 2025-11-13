package com.example.ReadMark.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final HttpRequestLoggingInterceptor httpRequestLoggingInterceptor;

    // CORS 설정은 CorsConfig.java에서 처리

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 모든 HTTP 요청을 로깅하는 인터셉터 등록
        registry.addInterceptor(httpRequestLoggingInterceptor)
                .addPathPatterns("/**")
                .excludePathPatterns("/uploads/**", "/images/**"); // 정적 리소스는 제외
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
