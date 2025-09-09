# 📚 ReadMark API Documentation

<aside>
📢 **BASE URL**: `http://localhost:5000`

**서버 포트**: 5000 (ESP32와 프론트엔드 통합)

**GUI UUID**: `readmark-api-2024-01-15` (고정)
</aside>

---

## 📋 API 목록

### 👤 [/user](#user-api)
사용자 관리 API - 회원가입, 로인, 사용자 정보 조회

### 📖 [/book](#book-api)
책 관리 API - 책 등록, 검색, 상세 조회

### 🔗 [/userbook](#userbook-api)
사용자-책 관계 API - 서재 관리, 독서 상태 관리

### 📸 [/image](#image-api)
이미지 업로드 API - 책 페이지 이미지 처리, OCR 분석

### 🤖 [/esp32](#esp32-api)
ESP32 전용 API - 임베디드 기기 통신

### 📊 [/readinglog](#readinglog-api)
독서 기록 API - 독서 통계, 기록 관리

### 📅 [/calendar](#calendar-api)
캘린더 API - 월별 독서 데이터

### 👤 [/mypage](#mypage-api)
마이페이지 API - 사용자 통계, 즐겨찾기

---

## 👤 User API

### 회원가입
```http
POST /api/users/join
Content-Type: application/json

{
  "username": "testuser",
  "email": "test@example.com",
  "password": "password123"
}
```

**응답:**
```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "userId": 1,
    "username": "testuser",
    "email": "test@example.com"
  }
}
```

### 로그인
```http
POST /api/users/login
Content-Type: application/json

{
  "email": "test@example.com",
  "password": "password123"
}
```

### 전체 사용자 조회
```http
GET /api/users
```

### 사용자 정보 조회
```http
GET /api/users/{userId}
```

---

## 📖 Book API

### 책 등록
```http
POST /api/books
Content-Type: application/json

{
  "title": "자바의 정석",
  "author": "남궁성",
  "isbn": "9788994492031",
  "totalPages": 1000
}
```

**응답:**
```json
{
  "success": true,
  "message": "책이 등록되었습니다.",
  "bookId": 1
}
```

### 책 검색
```http
GET /api/books/search?keyword=자바
```

**응답:**
```json
{
  "success": true,
  "books": [
    {
      "bookId": 1,
      "title": "자바의 정석",
      "author": "남궁성",
      "isbn": "9788994492031",
      "totalPages": 1000
    }
  ],
  "count": 1
}
```

### 책 상세 조회
```http
GET /api/books/{bookId}
```

---

## 🔗 UserBook API

### 책을 사용자 서재에 추가
```http
POST /api/userbooks
Content-Type: application/json

{
  "userId": 1,
  "bookId": 1,
  "status": "READING",
  "currentPage": 0
}
```

**응답:**
```json
{
  "success": true,
  "message": "책이 서재에 추가되었습니다.",
  "data": {
    "userBookId": 1,
    "userId": 1,
    "bookId": 1,
    "status": "READING",
    "currentPage": 0
  }
}
```

### 사용자 서재 조회
```http
GET /api/userbooks/user/{userId}
```

### 상태별 책 조회
```http
GET /api/userbooks/user/{userId}/status/READING
```

### 책 상태 업데이트
```http
PUT /api/userbooks/{userBookId}/status
Content-Type: application/json

{
  "status": "COMPLETED"
}
```

### 현재 페이지 업데이트
```http
PUT /api/userbooks/{userBookId}/page
Content-Type: application/json

{
  "currentPage": 50
}
```

---

## 📸 Image API

### 책 페이지 이미지 업로드 (프론트엔드용)
```http
POST /api/image/upload
Content-Type: multipart/form-data

userId: 1
bookId: 1
image: [이미지 파일]
deviceInfo: "Web Browser"
captureTime: "2024-01-15 14:30:00"
```

**응답:**
```json
{
  "success": true,
  "message": "책 페이지가 성공적으로 저장되었습니다.",
  "page": {
    "pageId": 123,
    "pageNumber": 45,
    "extractedText": "45",
    "confidence": 0.95,
    "language": "ko",
    "numberCount": 1,
    "textQuality": 85.5,
    "capturedAt": "2024-01-15T14:30:00"
  },
  "deviceInfo": "Web Browser"
}
```

### 독서 세션 시작
```http
POST /api/image/session/start
Content-Type: application/x-www-form-urlencoded

userId=1&bookId=1
```

### 독서 세션 종료
```http
POST /api/image/session/end
Content-Type: application/x-www-form-urlencoded

userId=1
```

### 독서 통계 조회
```http
GET /api/image/stats/{userId}
```

**응답:**
```json
{
  "success": true,
  "stats": {
    "maxConsecutiveDays": 10,
    "totalReadingDays": 25,
    "currentConsecutiveDays": 3,
    "habitAnalysis": {
      "avgPagesPerDay": 15.5,
      "favoriteDayOfWeek": "토요일",
      "readingFrequency": "75.0%"
    }
  }
}
```

### 월별 독서 통계
```http
GET /api/image/stats/{userId}/monthly?months=6
```

### 책 페이지 목록 조회
```http
GET /api/image/pages/{userId}/{bookId}
```

### 특정 페이지 조회
```http
GET /api/image/pages/{userId}/{bookId}/{pageNumber}
```

### 페이지 범위 조회
```http
GET /api/image/pages/{userId}/{bookId}/range?startPage=1&endPage=10
```

### 최근 페이지 조회
```http
GET /api/image/pages/{userId}/{bookId}/recent?limit=10
```

### 페이지 삭제
```http
DELETE /api/image/pages/{userId}/{bookId}/{pageNumber}
```

---

## 🤖 ESP32 API

### ESP32 이미지 업로드
```http
POST /upload/
Content-Type: multipart/form-data

image: [이미지 파일]
```

**응답:**
```json
{
  "success": true,
  "message": "이미지 업로드 성공",
  "pageId": 123,
  "pageNumber": 45,
  "extractedText": "45",
  "confidence": 0.95,
  "textQuality": 85.5,
  "deviceInfo": "ESP32-CAM",
  "capturedAt": "2024-01-15T14:30:00",
  "date": 3,
  "readingPeriod": "2024.01.13 - 2024.01.15",
  "currentConsecutiveDays": 3
}
```

### ESP32 독서 세션 시작
```http
POST /esp32/session/start
```

**응답:**
```json
{
  "success": true,
  "message": "독서 세션이 시작되었습니다.",
  "sessionId": "session_123",
  "startTime": "2024-01-15T14:30:00",
  "userId": 1,
  "bookId": 1,
  "date": 3,
  "readingPeriod": "2024.01.13 - 2024.01.15",
  "currentConsecutiveDays": 3
}
```

### ESP32 독서 세션 종료
```http
POST /esp32/session/end
```

**응답:**
```json
{
  "success": true,
  "message": "독서 세션이 종료되었습니다.",
  "totalPagesRead": 5,
  "totalNumbersRead": 5,
  "readingDurationMinutes": 15,
  "endTime": "2024-01-15T14:45:00",
  "userId": 1,
  "date": 3,
  "readingPeriod": "2024.01.13 - 2024.01.15",
  "currentConsecutiveDays": 3
}
```

### ESP32 통계 조회
```http
GET /esp32/stats
```

### ESP32 헬스체크
```http
GET /upload/health
```

**응답:**
```json
{
  "status": "OK",
  "message": "ESP32 Upload Service is running",
  "timestamp": "2024-01-15T14:30:00"
}
```

---

## 📊 ReadingLog API

### 독서 기록 생성
```http
POST /api/readinglogs
Content-Type: application/json

{
  "userId": 1,
  "readDate": "2024-01-15",
  "pagesRead": 10
}
```

**응답:**
```json
{
  "success": true,
  "message": "독서 기록이 저장되었습니다.",
  "logId": 1,
  "pagesRead": 10
}
```

### 기간별 독서 기록 조회
```http
GET /api/readinglogs/user/{userId}?startDate=2024-01-01&endDate=2024-01-31
```

### 오늘 독서 페이지 수
```http
GET /api/readinglogs/user/{userId}/today
```

**응답:**
```json
{
  "success": true,
  "pagesRead": 15,
  "date": "2024-01-15"
}
```

### 독서 통계
```http
GET /api/readinglogs/user/{userId}/stats
```

### 일일 독서 통계
```http
GET /api/readinglogs/user/{userId}/daily?startDate=2024-01-01&endDate=2024-01-31
```

---

## 📅 Calendar API

### 월별 캘린더 데이터
```http
GET /api/calendar/{userId}/{year}/{month}
```

**응답:**
```json
{
  "success": true,
  "calendar": {
    "year": 2024,
    "month": 1,
    "days": [
      {
        "date": "2024-01-01",
        "pagesRead": 10,
        "hasReading": true
      },
      {
        "date": "2024-01-02",
        "pagesRead": 0,
        "hasReading": false
      }
    ]
  }
}
```

---

## 👤 MyPage API

### 사용자 통계
```http
GET /api/mypage/user/{userId}/stats
```

**응답:**
```json
{
  "success": true,
  "stats": {
    "totalBooks": 5,
    "totalPages": 2500,
    "totalReadingDays": 30,
    "maxConsecutiveDays": 10,
    "currentConsecutiveDays": 3,
    "avgPagesPerDay": 15.5
  }
}
```

### 즐겨찾기 페이지
```http
GET /api/mypage/user/{userId}/favorite-pages
```

---

## 🔧 ESP32 C 코드 예제

### 이미지 업로드 및 응답 처리
```c
#include "cJSON.h"

// ESP32에서 이미지 업로드
char url[128] = "http://192.168.1.51:5000/upload";
char resp[256];
esp_err_t err = sendPhoto(url, resp, sizeof(resp));

// JSON 응답 파싱
cJSON *root = cJSON_Parse(resp);
cJSON *success = cJSON_GetObjectItemCaseSensitive(root, "success");
cJSON *date = cJSON_GetObjectItemCaseSensitive(root, "date");
cJSON *readingPeriod = cJSON_GetObjectItemCaseSensitive(root, "readingPeriod");

if (cJSON_IsBool(success) && cJSON_IsTrue(success)) {
    if (cJSON_IsNumber(date)) {
        int readingDays = date->valueint;
        ESP_LOGI("ReadMark", "연속 독서일: %d일", readingDays);
        
        // LED로 독서일 수만큼 깜빡임
        for(int i = 0; i < readingDays; i++) {
            gpio_set_level(LED_PIN, 1);
            vTaskDelay(pdMS_TO_TICKS(200));
            gpio_set_level(LED_PIN, 0);
            vTaskDelay(pdMS_TO_TICKS(200));
        }
    }
    
    if (cJSON_IsString(readingPeriod)) {
        ESP_LOGI("ReadMark", "독서 기간: %s", readingPeriod->valuestring);
    }
}

cJSON_Delete(root);
```

---

## 🌐 프론트엔드 JavaScript 예제

### 이미지 업로드
```javascript
const uploadImage = async (userId, bookId, imageFile) => {
    const formData = new FormData();
    formData.append('userId', userId);
    formData.append('bookId', bookId);
    formData.append('image', imageFile);
    formData.append('deviceInfo', 'Web Browser');

    try {
        const response = await fetch('http://localhost:5000/api/image/upload', {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        console.log('업로드 결과:', result);
        return result;
    } catch (error) {
        console.error('업로드 실패:', error);
    }
};
```

### 독서 통계 조회
```javascript
const getReadingStats = async (userId) => {
    try {
        const response = await fetch(`http://localhost:5000/api/image/stats/${userId}`);
        const stats = await response.json();
        console.log('독서 통계:', stats);
        return stats;
    } catch (error) {
        console.error('통계 조회 실패:', error);
    }
};
```

### 독서 세션 관리
```javascript
const startReadingSession = async (userId, bookId) => {
    const formData = new FormData();
    formData.append('userId', userId);
    formData.append('bookId', bookId);

    try {
        const response = await fetch('http://localhost:5000/api/image/session/start', {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        console.log('세션 시작:', result);
        return result;
    } catch (error) {
        console.error('세션 시작 실패:', error);
    }
};

const endReadingSession = async (userId) => {
    const formData = new FormData();
    formData.append('userId', userId);

    try {
        const response = await fetch('http://localhost:5000/api/image/session/end', {
            method: 'POST',
            body: formData
        });
        
        const result = await response.json();
        console.log('세션 종료:', result);
        return result;
    } catch (error) {
        console.error('세션 종료 실패:', error);
    }
};
```

---

## 🚀 서버 실행

### 통합 모드 (권장)
```bash
# ESP32와 프론트엔드 모두 지원
java -jar ReadMark.jar
```

### ESP32 전용 모드 (디버깅용)
```bash
# ESP32만 지원
java -jar ReadMark.jar esp32
```

---

## 📝 주요 특징

- **통합 포트**: ESP32와 프론트엔드 모두 포트 5000 사용
- **OCR 처리**: Google Vision API를 통한 텍스트 추출
- **독서 기간 계산**: 연속 독서일 자동 계산
- **실시간 통계**: 독서 습관 분석 및 통계 제공
- **ESP32 최적화**: 임베디드 기기 전용 API 제공
- **CORS 지원**: 모든 도메인에서 접근 가능

---

## 🔗 관련 문서

- [ESP32 통합 가이드](./ESP32_INTEGRATION_README.md)
- [임베디드 API 가이드](./EMBEDDED_API_GUIDE.md)
- [Google Vision 설정](./GOOGLE_VISION_SETUP.md)
