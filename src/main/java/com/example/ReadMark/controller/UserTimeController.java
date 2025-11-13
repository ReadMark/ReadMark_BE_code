package com.example.ReadMark.controller;

import com.example.ReadMark.service.CalendarService;
import com.example.ReadMark.service.ReadingLogService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user-time")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH}, allowedHeaders = "*", allowCredentials = "false")
public class UserTimeController {

    private final CalendarService calendarService;
    private final ReadingLogService readingLogService;

    /**
     * 특정 유저의 하루 동안 읽은 시간을 분 단위로 조회합니다.
     */
    @GetMapping("/{userId}/daily")
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH}, allowedHeaders = "*", allowCredentials = "false")
    public ResponseEntity<?> getUserDailyReadingTime(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            LocalDate today = LocalDate.now();
            Long todayMinutes = calendarService.getTodayReadingMinutes(userId, today);
            
            response.put("success", true);
            response.put("userId", userId);
            response.put("date", today);
            response.put("readingMinutes", todayMinutes);
            response.put("message", "사용자 하루 독서 시간 조회 성공");
            
            log.info("사용자 하루 독서 시간 조회 완료: 사용자 {}, 오늘 {}분", userId, todayMinutes);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("사용자 하루 독서 시간 조회 중 오류 발생: 사용자 {}", userId, e);
            response.put("success", false);
            response.put("message", "사용자 하루 독서 시간 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

    /**
     * 특정 유저의 연속 독서일수를 조회합니다.
     */
    @GetMapping("/{userId}/consecutive-days")
    @CrossOrigin(origins = "*", methods = {RequestMethod.GET, RequestMethod.POST, RequestMethod.PUT, RequestMethod.DELETE, RequestMethod.OPTIONS, RequestMethod.PATCH}, allowedHeaders = "*", allowCredentials = "false")
    public ResponseEntity<?> getUserConsecutiveReadingDays(@PathVariable Long userId) {
        Map<String, Object> response = new HashMap<>();
        try {
            // CalendarService의 calculateConsecutiveReadingDays 메서드를 사용
            // private 메서드이므로 public 메서드로 만들어야 함
            // 임시로 ReadingLogService를 사용
            Integer consecutiveDays = readingLogService.getMaxConsecutiveReadingDays(userId);
            
            response.put("success", true);
            response.put("userId", userId);
            response.put("consecutiveDays", consecutiveDays != null ? consecutiveDays : 0);
            response.put("message", "사용자 연속 독서일수 조회 성공");
            
            log.info("사용자 연속 독서일수 조회 완료: 사용자 {}, 연속 {}일", userId, consecutiveDays);
            
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            log.error("사용자 연속 독서일수 조회 중 오류 발생: 사용자 {}", userId, e);
            response.put("success", false);
            response.put("message", "사용자 연속 독서일수 조회 실패: " + e.getMessage());
            return ResponseEntity.internalServerError().body(response);
        }
    }

}
