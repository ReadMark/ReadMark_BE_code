package com.example.ReadMark.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /**
     * PathVariable이나 RequestParam에서 "null" 문자열이나 잘못된 타입이 들어올 때 처리
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, Object>> handleMethodArgumentTypeMismatch(MethodArgumentTypeMismatchException e) {
        log.warn("PathVariable/RequestParam 타입 변환 실패: {} = '{}'", e.getName(), e.getValue());
        
        Map<String, Object> response = new HashMap<>();
        response.put("success", false);
        response.put("message", String.format("잘못된 파라미터 값입니다: %s (값: '%s')", e.getName(), e.getValue()));
        
        return ResponseEntity.badRequest().body(response);
    }
}
