package com.example.ReadMark.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.example.ReadMark.service.ESP32ReadingSessionService;
import com.example.ReadMark.service.UserBookService;
import com.example.ReadMark.service.BookService;
import com.example.ReadMark.service.UserService;
import com.example.ReadMark.service.BookPageService;
import com.example.ReadMark.repository.BookRepository;
import com.example.ReadMark.repository.UserBookRepository;
import com.example.ReadMark.model.entity.Book;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.*;

import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
@Slf4j
public class ESP32WebSocketHandler implements WebSocketHandler {

    private final ObjectMapper objectMapper;
    private final ESP32ReadingSessionService sessionService;
    private final UserBookService userBookService;
    private final BookService bookService;
    private final UserService userService;
    private final BookPageService bookPageService;
    private final BookRepository bookRepository;
    private final UserBookRepository userBookRepository;
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Long> sessionUserIds = new ConcurrentHashMap<>(); // 세션별 user_id 저장
    private final Map<String, Long> sessionBookIds = new ConcurrentHashMap<>(); // 세션별 book_id 저장
    
    // 세션별 책 목록과 현재 인덱스 저장
    private final Map<String, List<Map<String, Object>>> sessionBookLists = new ConcurrentHashMap<>();
    private final Map<String, Integer> sessionCurrentBookIndex = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        
        log.info("ESP32 WebSocket 연결됨: {}", sessionId);
        log.info("현재 활성 세션 수: {}", sessions.size());
        
        // 연결 확인 메시지 전송 (32자 제한)
        Map<String, Object> connectionMessage = new LinkedHashMap<>();
        connectionMessage.put("comment", "connected");
        sendMessage(session, connectionMessage);
    }

    @Override
    public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
        String sessionId = session.getId();
        String payload = message.getPayload().toString();
        
        log.info("ESP32에서 메시지 수신 [{}]: {}", sessionId, payload);
        
        // 빈 메시지나 공백만 있는 메시지 처리
        if (payload == null || payload.trim().isEmpty()) {
            log.warn("빈 메시지 수신 [{}]", sessionId);
            Map<String, Object> errorMessage = new LinkedHashMap<>();
            errorMessage.put("type", "ERROR" + "\n");
            errorMessage.put("text", "empty message");
            sendMessage(session, errorMessage);
            return;
        }
        
        try {
            // JSON 메시지 파싱 시도
            Map<String, Object> messageData = objectMapper.readValue(payload, Map.class);
            String type = (String) messageData.get("type");
            Object data = messageData.get("data");
            
            log.info("메시지 타입: {}, 데이터: {}", type, data);
            
            // 메시지 타입에 따른 처리
            switch (type) {
                case "ping":
                    handlePing(session, data);
                    break;
                case "status":
                    handleStatus(session, data);
                    break;
                case "image_upload":
                    handleImageUpload(session, data);
                    break;
                case "user_login":
                    handleUserLogin(session, messageData);
                    break;
                case "book_selection":
                    handleBookSelection(session, messageData);
                    break;
                case "session_start":
                    handleSessionStart(session, messageData);
                    break;
                case "next_book":
                    handleNextBook(session, messageData);
                    break;
                default:
                    log.warn("알 수 없는 메시지 타입: {}", type);
                    Map<String, Object> errorMessage = new LinkedHashMap<>();
                    errorMessage.put("type", "ERROR" + "\n");
                    errorMessage.put("text", "unknown type: " + type);
                    sendMessage(session, errorMessage);
            }
            
        } catch (com.fasterxml.jackson.core.JsonParseException e) {
            log.error("JSON 파싱 오류 [{}]: {}", sessionId, e.getMessage());
            log.error("수신된 페이로드: '{}'", payload);
            Map<String, Object> errorMessage = new LinkedHashMap<>();
            errorMessage.put("type", "ERROR" + "\n");
            errorMessage.put("text", "invalid JSON");
            sendMessage(session, errorMessage);
        } catch (Exception e) {
            log.error("메시지 처리 중 오류 발생 [{}]: {}", sessionId, e.getMessage());
            Map<String, Object> errorMessage = new LinkedHashMap<>();
            errorMessage.put("type", "ERROR" + "\n");
            errorMessage.put("text", "processing error");
            sendMessage(session, errorMessage);
        }
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
        String sessionId = session.getId();
        log.error("WebSocket 전송 오류 [{}]: {}", sessionId, exception.getMessage());
        
        // 세션 제거 및 데이터 정리
        sessions.remove(sessionId);
        sessionUserIds.remove(sessionId);
        sessionBookIds.remove(sessionId);
        sessionBookLists.remove(sessionId);
        sessionCurrentBookIndex.remove(sessionId);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        
        // 세션별 데이터 정리
        sessionUserIds.remove(sessionId);
        sessionBookIds.remove(sessionId);
        sessionBookLists.remove(sessionId);
        sessionCurrentBookIndex.remove(sessionId);
        
        log.info("ESP32 WebSocket 연결 종료 [{}]: {}", sessionId, closeStatus);
        log.info("현재 활성 세션 수: {}", sessions.size());
    }

    @Override
    public boolean supportsPartialMessages() {
        return false;
    }

    // 메시지 처리 메서드들
    private void handlePing(WebSocketSession session, Object data) throws Exception {
        log.info("Ping 메시지 수신: {}", data);
        Map<String, Object> pongMessage = new LinkedHashMap<>();
        pongMessage.put("text", "pong");
        sendMessage(session, pongMessage);
    }

    private void handleStatus(WebSocketSession session, Object data) throws Exception {
        log.info("상태 요청 수신: {}", data);
        
        Map<String, Object> statusMessage = new LinkedHashMap<>();
        statusMessage.put("text", "sessions: " + sessions.size());
        
        sendMessage(session, statusMessage);
    }

    private void handleImageUpload(WebSocketSession session, Object data) throws Exception {
        log.info("이미지 업로드 요청 수신: {}", data);
        
        // 이미지 업로드는 REST API를 통해 처리하도록 안내
        Map<String, Object> infoMessage = new LinkedHashMap<>();
        infoMessage.put("text", "use REST API");
        sendMessage(session, infoMessage);
    }

    /**
     * ESP32에서 사용자 로그인 메시지 처리
     */
    private void handleUserLogin(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        String sessionId = session.getId();
        Long userId = Long.valueOf(messageData.get("userId").toString());
        
        log.info("ESP32에서 사용자 로그인 수신 [{}]: userId={}", sessionId, userId);
        
        // 사용자 존재 여부 검증
//        try {
//            var user = userService.getUserById(userId);
//            if (user == null) {
//                log.warn("사용자 로그인 실패 [{}]: 존재하지 않는 사용자 userId={}", sessionId, userId);
//
//                Map<String, Object> errorResponse = new LinkedHashMap<>();
//                errorResponse.put("text", "user not found");
//
//                sendMessage(session, errorResponse);
//                return;
//            }
//        } catch (Exception e) {
//            log.error("사용자 조회 중 오류 발생 [{}]: userId={}", sessionId, userId, e);
//
//            Map<String, Object> errorResponse = new LinkedHashMap<>();
//            errorResponse.put("text", "login failed");
//
//            sendMessage(session, errorResponse);
//            return;
//        }
        
        // 검증 성공 시 세션 정보 저장
        sessionUserIds.put(sessionId, userId);
        
        // 세션 서비스에 user_id 저장
        sessionService.setUserIdForSession(sessionId, userId);
        
        // 사용자의 등록된 책 목록 조회
        List<Map<String, Object>> userBooks = getUserBooks(userId);
        
        // 세션별 책 목록과 인덱스 저장
        sessionBookLists.put(sessionId, userBooks);
        sessionCurrentBookIndex.put(sessionId, 0);
        
        // 첫 번째 책만 전송
        if (!userBooks.isEmpty()) {
            Map<String, Object> firstBook = userBooks.get(0);
            String bookText = String.format("book: %d (1/%d)", firstBook.get("bookId"), userBooks.size());
            
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("bookId", bookText);
            
            sendMessage(session, response);
        } else {
            // 책이 없는 경우
            String text = "no books";
            Map<String, Object> response = new LinkedHashMap<>();
            response.put("bookId", text);
            
            sendMessage(session, response);
        }
        
        log.info("사용자 로그인 성공 [{}]: userId={}, 책 개수={}", sessionId, userId, userBooks.size());
    }
    
    /**
     * ESP32에서 책 선택 메시지 처리
     */
    private void handleBookSelection(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        String sessionId = session.getId();
        Long bookId = Long.valueOf(messageData.get("bookId").toString());
        Long userId = Long.valueOf(messageData.get("userId").toString());
        
        log.info("ESP32에서 책 선택 수신 [{}]: userId={}, bookId={}", sessionId, userId, bookId);
        
        // 사용자 존재 여부 검증
        try {
            var user = userService.getUserById(userId);
            if (user == null) {
                log.warn("책 선택 실패 [{}]: 존재하지 않는 사용자 userId={}", sessionId, userId);
                Map<String, Object> errorResponse = new LinkedHashMap<>();
                errorResponse.put("text", "user not found");
                sendMessage(session, errorResponse);
                return;
            }
        } catch (Exception e) {
            log.error("사용자 조회 중 오류 발생 [{}]: userId={}", sessionId, userId, e);
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("text", "user lookup failed");
            sendMessage(session, errorResponse);
            return;
        }

        // 사용자-책 관계 유효성 검증
        ValidationResult validationResult = validateUserBookRelation(userId, bookId);
        
        if (!validationResult.isValid()) {
            log.warn("책 선택 검증 실패 [{}]: {}", sessionId, validationResult.getMessage());
            
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("text", "invalid book");
            
            sendMessage(session, errorResponse);
            return;
        }
        
        // 검증 성공 시 세션 정보 저장
        sessionUserIds.put(sessionId, userId);
        sessionBookIds.put(sessionId, bookId);
        
        // 세션 서비스에 정보 저장
        sessionService.setBookIdForSession(sessionId, bookId);
        sessionService.setUserIdForSession(sessionId, userId);
        
        // 책 선택과 동시에 세션 시작
        sessionService.startSession(sessionId, bookId, userId);
        
        // 선택된 책 정보 조회
        Map<String, Object> selectedBook = getBookInfo(bookId);
        
        // 96자(3*32) OLED 메시지 전송 - type 없이 text만 전송
        String oled96 = buildOledThreePagesText(userId, bookId);
        Map<String, Object> oledMsg = new LinkedHashMap<>();
        oledMsg.put("text", oled96);
        sendMessage(session, oledMsg);
        
        log.info("책 선택 성공 [{}]: userId={}, bookId={}", sessionId, userId, bookId);
    }

    /**
     * ESP32에서 다음 책 요청 처리
     */
    private void handleNextBook(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        String sessionId = session.getId();
        Long userId = sessionUserIds.get(sessionId);
        
        if (userId == null) {
            log.warn("세션에 사용자 ID가 없습니다: {}", sessionId);
            return;
        }
        
        List<Map<String, Object>> userBooks = sessionBookLists.get(sessionId);
        Integer currentIndex = sessionCurrentBookIndex.get(sessionId);
        
        if (userBooks == null || userBooks.isEmpty() || currentIndex == null) {
            log.warn("책 목록이 없습니다: sessionId={}", sessionId);
            return;
        }
        
        // 다음 인덱스로 이동 (순환)
        int nextIndex = (currentIndex + 1) % userBooks.size();
        sessionCurrentBookIndex.put(sessionId, nextIndex);
        
        Map<String, Object> nextBook = userBooks.get(nextIndex);
        String bookText = String.format("book: %d (%d/%d)", nextBook.get("bookId"), nextIndex + 1, userBooks.size());
        
        Map<String, Object> response = new LinkedHashMap<>();
        response.put("bookId", bookText);
        
        sendMessage(session, response);
        
        log.info("다음 책으로 이동 [{}]: userId={}, bookId={}, index={}/{}", 
                sessionId, userId, nextBook.get("bookId"), nextIndex + 1, userBooks.size());
    }
    

    /**
     * ESP32에서 세션 시작 메시지 처리 (최근 사용한 userId 자동 사용)
     */
    private void handleSessionStart(WebSocketSession session, Map<String, Object> messageData) throws Exception {
        String sessionId = session.getId();
        Long bookId = Long.valueOf(messageData.get("bookId").toString());
        
        // 세션에서 최근 사용한 userId 가져오기
        Long userId = sessionUserIds.get(sessionId);
        
        if (userId == null) {
            log.warn("세션에 사용자 ID가 없습니다. 먼저 user_login을 수행해주세요: {}", sessionId);
            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("type", "ERROR" + "\n");
            errorResponse.put("text", "login required");
            sendMessage(session, errorResponse);
            return;
        }
        
        log.info("ESP32에서 세션 시작 수신 [{}]: userId={} (세션에서 자동), bookId={}", sessionId, userId, bookId);
        
        // 사용자-책 관계 유효성 검증
        ValidationResult validationResult = validateUserBookRelation(userId, bookId);

        if (!validationResult.isValid()) {
            log.warn("세션 시작 검증 실패 [{}]: {}", sessionId, validationResult.getMessage());

            Map<String, Object> errorResponse = new LinkedHashMap<>();
            errorResponse.put("text", "invalid session");

            sendMessage(session, errorResponse);
            return;
        }

        // 검증 성공 시 세션 정보 저장
        sessionBookIds.put(sessionId, bookId);

        // 세션 서비스에 세션 시작
        sessionService.startSession(sessionId, bookId, userId);

        // ESP32 OLED 32자 제한에 맞춰 간단한 세션 시작 메시지 전송
        String text = String.format("id: %d book: %d started", userId, bookId);

        Map<String, Object> response = new LinkedHashMap<>();
        response.put("type", "SESSION_STARTED" + "\n");
        response.put("text", text);

        sendMessage(session, response);

        log.info("세션 시작 성공 [{}]: userId={}, bookId={}", sessionId, userId, bookId);
    }

    /**
     * OCR 결과를 웹소켓으로 전송 (32자 제한)
     */
    public void sendOCRResult(int pageNumber) {
        log.info("OCR 결과 웹소켓 전송: 페이지 {}", pageNumber);

        Map<String, Object> ocrData = new LinkedHashMap<>();
        ocrData.put("text", "page: " + pageNumber);

        broadcastSimpleMessage(ocrData);
    }
    
    /**
     * 독서 완료 결과를 ESP32에게 전송 (32자 제한)
     */
    public void sendReadingCompleteResult(int finalPage, Map<String, Object> readingResult) {
        log.info("독서 완료 결과 웹소켓 전송: 마지막 페이지 {}", finalPage);

        Map<String, Object> completeData = new LinkedHashMap<>();
        completeData.put("type", "READING_COMPLETE");
        completeData.put("text", "completed page: " + finalPage);

        broadcastSimpleMessage(completeData);
    }
    
    /**
     * 현재 페이지 업데이트 결과를 ESP32에게 전송 (32자 제한)
     */
    public void sendCurrentPageUpdate(int currentPage) {
        log.info("현재 페이지 업데이트 웹소켓 전송: 페이지 {}", currentPage);

        Map<String, Object> updateData = new LinkedHashMap<>();
        updateData.put("type", "PAGE_UPDATE");
        updateData.put("text", "current page: " + currentPage);

        broadcastSimpleMessage(updateData);
    }
    
    /**
     * OCR 결과를 ESP32에게 전송 (32자 제한)
     */
    public void sendOCRResultToESP32(Long bookId, Long userId, com.example.ReadMark.model.dto.VisionAnalysisResultDTO visionResult) {
        log.info("OCR 결과를 ESP32에게 전송: bookId={}, userId={}, pageNumber={}", 
                bookId, userId, visionResult.getEstimatedPageNumber());

        Map<String, Object> ocrData = new LinkedHashMap<>();
        ocrData.put("type", "OCR_RESULT" + "\n");
        ocrData.put("text", "id: " + userId + " page: " + visionResult.getEstimatedPageNumber());

        broadcastSimpleMessage(ocrData);
    }

    // === OLED 3페이지(총 96자) 메시지 구성 유틸 ===
    private String buildOledThreePagesText(Long userId, Long bookId) {
        // 1페이지 (1-32자): Page:현재 페이지/전체 페이지(프론트 등록 시 입력한 값)
        int currentPage = getLastReadPage(userId, bookId);
        String totalPagesStr = getTotalPagesForBook(userId, bookId);
        
        // 현재 페이지가 전체 페이지와 같으면 "ReadDone!" 표시
        String page1;
        if (!totalPagesStr.equals("---")) {
            try {
                int totalBook = Integer.parseInt(totalPagesStr);
                if (currentPage >= totalBook && totalBook > 0) {
                    page1 = "Page:ReadDone!";
                } else {
                    page1 = String.format("Page:%d/%s", currentPage, totalPagesStr);
                }
            } catch (NumberFormatException e) {
                // totalPagesStr이 숫자가 아니면 기본 형식 사용
                page1 = String.format("Page:%d/%s", currentPage, totalPagesStr);
            }
        } else {
            page1 = String.format("Page:%d/%s", currentPage, totalPagesStr);
        }
        page1 = padTo32Chars(page1);

        // 2페이지 (33-64자): 책을 얼마나 오랜만에 읽었는지
        String gapText = getReadGapText(userId, bookId);
        // gap 접두사 제거 후 표기 통일: ex) 2d3h, 5h12m, 9m, NA
        String normalizedGap = gapText.startsWith("gap") ? gapText.substring(3) : gapText;
        if (normalizedGap.isEmpty()) normalizedGap = "NA";
        String page2 = String.format("Gap : %s", normalizedGap);
        page2 = padTo32Chars(page2);

        // 3페이지 (65-96자): 책을 읽은 총 날짜, 오늘 날짜
        // totalDays 부분: 16자, Today 부분: 16자로 나누어 패딩
        int totalDays = getTotalReadingDays(userId, bookId);
        String today = java.time.LocalDate.now().toString();
        
        // totalDays 부분: 16자로 패딩
        String totalDaysPart = String.format("totalDays:%d", totalDays);
        totalDaysPart = padToNChars(totalDaysPart, 16);
        
        // Today 부분: 16자로 패딩
        String todayPart = String.format("Today:%s", today);
        todayPart = padToNChars(todayPart, 16);
        
        // 두 부분 합치기 (총 32자)
        String page3 = totalDaysPart + todayPart;

        return page1 + page2 + page3; // 총 96자
    }

    /**
     * 텍스트를 정확히 32자로 패딩 (언더바로 채움)
     * 내용이 32자보다 길면 자르고, 짧으면 언더바로 채움
     */
    private String padTo32Chars(String text) {
        if (text == null) text = "";
        final int target = 32;
        
        // 길면 자르기
        if (text.length() > target) {
            return text.substring(0, target);
        }
        
        // 정확히 32자가 되도록 언더바로 패딩
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() < target) {
            sb.append('_'); // 언더바 사용
        }
        return sb.toString();
    }

    /**
     * 텍스트를 정확히 N자로 패딩 (언더바로 채움)
     * 내용이 N자보다 길면 자르고, 짧으면 언더바로 채움
     */
    private String padToNChars(String text, int n) {
        if (text == null) text = "";
        final int target = n;
        
        // 길면 자르기
        if (text.length() > target) {
            return text.substring(0, target);
        }
        
        // 정확히 N자가 되도록 언더바로 패딩
        StringBuilder sb = new StringBuilder(text);
        while (sb.length() < target) {
            sb.append('_'); // 언더바 사용
        }
        return sb.toString();
    }

    private int getLastReadPage(Long userId, Long bookId) {
        try {
            // 1. 무조건 user_books 테이블의 current_page를 우선 사용 (0이어도 사용)
            var userBookOpt = userBookRepository.findByUser_UserIdAndBook_BookId(userId, bookId);
            if (userBookOpt.isPresent()) {
                int currentPage = userBookOpt.get().getCurrentPage();
                log.info("user_books의 current_page 사용: userId={}, bookId={}, currentPage={}", userId, bookId, currentPage);
                return currentPage;
            }
            
            // 2. user_books에 레코드가 없을 때만 book_pages 테이블의 최근 페이지 조회
            var recents = bookPageService.getRecentPages(userId, bookId, 1);
            if (recents != null && !recents.isEmpty() && recents.get(0).getPageNumber() != null) {
                int pageNumber = recents.get(0).getPageNumber();
                log.info("user_books 없음, book_pages의 최근 페이지 사용: userId={}, bookId={}, pageNumber={}", userId, bookId, pageNumber);
                return pageNumber;
            }
        } catch (Exception e) {
            log.warn("최근 페이지 조회 실패: userId={}, bookId={}", userId, bookId, e);
        }
        return 0;
    }

    private String getTotalPagesForBook(Long userId, Long bookId) {
        // 프론트에서 책 등록 시 입력한 전체 페이지 수만 사용 (books 테이블의 total_book)
        try {
            Optional<Book> bookOpt = bookRepository.findById(bookId);
            if (bookOpt.isPresent() && bookOpt.get().getTotalBook() != null && bookOpt.get().getTotalBook() > 0) {
                int totalBook = bookOpt.get().getTotalBook();
                log.debug("books 테이블의 total_book 사용: bookId={}, totalBook={}", bookId, totalBook);
                return String.valueOf(totalBook);
            }
        } catch (Exception e) {
            log.warn("총 페이지 수 조회 실패: userId={}, bookId={}", userId, bookId, e);
        }
        // 프론트에서 등록한 전체 페이지가 없으면 "---"로 표시
        log.debug("books 테이블의 total_book이 없음: bookId={}, '---' 반환", bookId);
        return "---";
    }

    private String getReadGapText(Long userId, Long bookId) {
        try {
            var recents = bookPageService.getRecentPages(userId, bookId, 2);
            if (recents != null && recents.size() >= 2) {
                var t1 = recents.get(0).getCapturedAt(); // 최신
                var t2 = recents.get(1).getCapturedAt(); // 그 전
                if (t1 != null && t2 != null) {
                    java.time.Duration d = java.time.Duration.between(t2, t1);
                    long days = d.toDays();
                    long hours = d.minusDays(days).toHours();
                    long mins = d.minusDays(days).minusHours(hours).toMinutes();
                    if (days > 0) return String.format("gap%dd%dh", days, hours);
                    if (hours > 0) return String.format("gap%dh%dm", hours, mins);
                    return String.format("gap%dm", mins);
                }
            }
        } catch (Exception e) {
            log.warn("읽기 간격 계산 실패: userId={}, bookId={}", userId, bookId, e);
        }
        return "gapNA";
    }

    private int getTotalReadingDays(Long userId, Long bookId) {
        try {
            var pages = bookPageService.getBookPages(userId, bookId);
            java.util.Set<java.time.LocalDate> days = new java.util.HashSet<>();
            for (var p : pages) {
                if (p.getCapturedAt() != null) days.add(p.getCapturedAt().toLocalDate());
            }
            return days.size();
        } catch (Exception e) {
            log.warn("총 읽은 날짜 계산 실패: userId={}, bookId={}", userId, bookId, e);
            return 0;
        }
    }

    /**
     * 이미지 업로드 완료 알림을 웹소켓으로 전송 (32자 제한)
     */
    public void sendImageUploadComplete(int pageNumber) {
        log.info("이미지 업로드 완료 웹소켓 전송: 페이지 번호 {}", pageNumber);
        
        Map<String, Object> uploadData = new LinkedHashMap<>();
        uploadData.put("text", "uploaded page: " + pageNumber);
        
        broadcastSimpleMessage(uploadData);
    }

    /**
     * 독서 세션 정보를 웹소켓으로 전송 (32자 제한)
     */
    public void sendReadingSessionInfo(int totalPages) {
        log.info("독서 세션 정보 웹소켓 전송: 총 {}페이지", totalPages);
        
        Map<String, Object> sessionData = new LinkedHashMap<>();
        sessionData.put("text", "total pages: " + totalPages);
        
        broadcastSimpleMessage(sessionData);
    }
    
    /**
     * 사용자의 등록된 책 목록 조회
     */
    private List<Map<String, Object>> getUserBooks(Long userId) {
        try {
            return userBookService.getAllUserBooks(userId).stream()
                    .map(userBook -> {
                        Map<String, Object> bookInfo = new LinkedHashMap<>();
                        bookInfo.put("bookId", userBook.getBook().getBookId());
                        bookInfo.put("title", userBook.getBook().getTitle());
                        bookInfo.put("author", userBook.getBook().getAuthor());
                        bookInfo.put("coverImageUrl", userBook.getBook().getCoverImageUrl());
                        return bookInfo;
                    })
                    .toList();
        } catch (Exception e) {
            log.error("사용자 책 목록 조회 실패: userId={}", userId, e);
            return List.of();
        }
    }
    
    /**
     * 책 목록을 ESP32 OLED용 텍스트 형식으로 변환 (최대 32자 제한)
     */
    private String formatBookListAsText(Long userId, List<Map<String, Object>> userBooks) {
        // ESP32 OLED 32자 제한에 맞춰 간단한 형식으로 변환
        // 첫 번째 책만 표시: "book: 32 (1/2)"
        if (userBooks.isEmpty()) {
            return "no books";
        }
        
        Map<String, Object> firstBook = userBooks.get(0);
        return String.format("book: %d (1/%d)", 
            firstBook.get("bookId"), 
            userBooks.size());
    }
    
    /**
     * 단일 책을 ESP32 OLED용 텍스트 형식으로 변환 (최대 32자 제한)
     */
    private String formatSingleBookAsText(Long userId, Map<String, Object> book, int currentIndex, int totalBooks) {
        // ESP32 OLED 32자 제한에 맞춰 간단한 형식으로 변환
        // 형식: "book: 32 (1/2)"
        String text = String.format("book: %d (%d/%d)", 
            book.get("bookId"), 
            currentIndex + 1, 
            totalBooks);
        
        // 32자 제한 확인 및 조정
        if (text.length() > 32) {
            text = String.format("book: %d (%d/%d)", 
                book.get("bookId"), 
                currentIndex + 1, 
                totalBooks);
        }
        
        return text;
    }
    
    /**
     * 책 정보 조회
     */
    private Map<String, Object> getBookInfo(Long bookId) {
        try {
            var book = bookService.getBookById(bookId);
            Map<String, Object> bookInfo = new LinkedHashMap<>();
            bookInfo.put("bookId", book.getBookId());
            bookInfo.put("title", book.getTitle());
            bookInfo.put("author", book.getAuthor());
            bookInfo.put("coverImageUrl", book.getCoverImageUrl());
            return bookInfo;
        } catch (Exception e) {
            log.error("책 정보 조회 실패: bookId={}", bookId, e);
            Map<String, Object> defaultInfo = new LinkedHashMap<>();
            defaultInfo.put("bookId", bookId);
            defaultInfo.put("title", "알 수 없는 책");
            defaultInfo.put("author", "");
            defaultInfo.put("coverImageUrl", "");
            return defaultInfo;
        }
    }
    
    /**
     * 사용자와 책의 관계 유효성 검증
     */
    private ValidationResult validateUserBookRelation(Long userId, Long bookId) {
        try {
            // 1. 사용자 존재 여부 확인
            var user = userService.getUserById(userId);
            if (user == null) {
                return new ValidationResult(false, "사용자를 찾을 수 없습니다", "USER_NOT_FOUND");
            }

            // 2. 책 존재 여부 확인
            var book = bookService.getBookById(bookId);
            if (book == null) {
                return new ValidationResult(false, "책을 찾을 수 없습니다", "BOOK_NOT_FOUND");
            }

            // 3. 사용자-책 관계 확인 (UserBook 테이블에서 확인)
            var userBooks = userBookService.getAllUserBooks(userId);
            boolean hasBook = userBooks.stream()
                    .anyMatch(userBook -> userBook.getBook().getBookId().equals(bookId));


            if (!hasBook) {
                return new ValidationResult(false, "사용자가 등록하지 않은 책입니다", "BOOK_NOT_REGISTERED");
            }

            return new ValidationResult(true, "유효한 사용자-책 관계입니다", "VALID");

        } catch (Exception e) {
            log.error("사용자-책 관계 검증 중 오류 발생: userId={}, bookId={}", userId, bookId, e);
            return new ValidationResult(false, "검증 중 오류가 발생했습니다", "VALIDATION_ERROR");
        }
    }
    
    /**
     * 검증 결과 클래스
     */
    private static class ValidationResult {
        private final boolean valid;
        private final String message;
        private final String code;
        
        public ValidationResult(boolean valid, String message, String code) {
            this.valid = valid;
            this.message = message;
            this.code = code;
        }
        
        public boolean isValid() { return valid; }
        public String getMessage() { return message; }
        public String getCode() { return code; }
    }

    // 유틸리티 메서드들
    private void sendMessage(WebSocketSession session, Map<String, Object> message) {
        try {
            if (session.isOpen()) {
                // 항상 JSON으로 전송
                String jsonMessage = objectMapper.writeValueAsString(message);
                session.sendMessage(new TextMessage(jsonMessage));
                log.debug("JSON 메시지 전송 완료: {}", jsonMessage);
            } else {
                log.warn("세션이 닫혀있어 메시지를 전송할 수 없습니다: {}", session.getId());
            }
        } catch (IOException e) {
            log.error("메시지 전송 중 오류 발생: {}", e.getMessage());
        }
    }

    // 공개 메서드들

    public void broadcastSimpleMessage(Map<String, Object> data) {
        log.info("간단한 브로드캐스트 메시지 전송: {}", data);
        
        sessions.values().forEach(session -> {
            if (session.isOpen()) {
                sendMessage(session, data);
            }
        });
    }

    public int getActiveSessionCount() {
        return sessions.size();
    }

    public boolean isSessionActive(String sessionId) {
        return sessions.containsKey(sessionId);
    }
}
