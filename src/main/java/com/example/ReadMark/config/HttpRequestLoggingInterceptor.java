package com.example.ReadMark.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.util.Enumeration;

@Slf4j
@Component
public class HttpRequestLoggingInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        // 모든 요청을 로깅 (ESP32 요청 추적용)
        String method = request.getMethod();
        String uri = request.getRequestURI();
        String queryString = request.getQueryString();
        String remoteAddr = getClientIpAddress(request);
        String userAgent = request.getHeader("User-Agent");
        
        log.info("=== HTTP 요청 수신 ===");
        log.info("Method: {}", method);
        log.info("URI: {}", uri);
        if (queryString != null) {
            log.info("Query: {}", queryString);
        }
        log.info("Remote Address: {}", remoteAddr);
        log.info("User-Agent: {}", userAgent);
        log.info("Content-Type: {}", request.getContentType());
        log.info("Content-Length: {}", request.getContentLength());
        
        // ESP32 요청인 경우 특별히 표시
        if (uri.contains("/api/upload") || uri.contains("/upload")) {
            log.info(">>> ESP32 업로드 요청 감지 <<<");
            log.info("요청 헤더:");
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String headerName = headerNames.nextElement();
                String headerValue = request.getHeader(headerName);
                log.info("  {}: {}", headerName, headerValue);
            }
        }
        
        log.info("====================");
        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, 
                               Object handler, Exception ex) {
        if (ex != null) {
            log.error("요청 처리 중 예외 발생: {} {}", request.getMethod(), request.getRequestURI(), ex);
        } else {
            int status = response.getStatus();
            String uri = request.getRequestURI();
            log.info("요청 처리 완료: {} {} - Status: {}", request.getMethod(), uri, status);
        }
    }

    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        return ip;
    }
}


