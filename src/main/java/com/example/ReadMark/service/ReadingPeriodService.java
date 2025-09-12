package com.example.ReadMark.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReadingPeriodService {
    
    private final ReadingSessionService readingSessionService;
    
    /**
     * 독서 기간을 계산합니다.
     * ESP32에서 사용할 간단한 값과 전체 기간 정보를 반환합니다.
     */
    public Map<String, Object> calculateReadingPeriod(Long userId) {
        Map<String, Object> result = new HashMap<>();
        
        try {
            // 현재 연속 독서일 계산
            int consecutiveDays = readingSessionService.getCurrentConsecutiveDays(userId);
            
            // 독서 통계 가져오기
            Map<String, Object> readingStats = readingSessionService.getReadingHabitAnalysis(userId);
            
            // 현재 날짜
            LocalDate today = LocalDate.now();
            
            // 독서 시작일 계산 (연속 독서일 기준)
            LocalDate readingStartDate = today.minusDays(consecutiveDays - 1);
            
            // 날짜 형식 포맷터
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
            
            // 기간 문자열 생성 (예: "2023.01.02 - 2023.01.05")
            String periodString = readingStartDate.format(formatter) + " - " + today.format(formatter);
            
            // ESP32에서 사용할 간단한 값 (연속 독서일 수)
            result.put("date", consecutiveDays);
            result.put("period", periodString);
            result.put("consecutiveDays", consecutiveDays);
            result.put("startDate", readingStartDate.format(formatter));
            result.put("endDate", today.format(formatter));
            result.put("totalReadingDays", readingStats.get("totalReadingDays"));
            
            log.info("독서 기간 계산 완료: 사용자 {}, 연속 {}일, 기간: {}", 
                    userId, consecutiveDays, periodString);
            
        } catch (Exception e) {
            log.error("독서 기간 계산 중 오류 발생", e);
            // 오류 시 기본값 설정
            result.put("date", 1);
            result.put("period", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
            result.put("consecutiveDays", 1);
            result.put("startDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
            result.put("endDate", LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy.MM.dd")));
            result.put("totalReadingDays", 1);
        }
        
        return result;
    }
}
