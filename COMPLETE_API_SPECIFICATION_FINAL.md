# 📚 **ReadMark 서버 완전 API 명세서**

## 🌐 **서버 정보**
- **Base URL:** `http://43.200.102.14:5000`
- **WebSocket URL:** `ws://43.200.102.14:5000`
- **프로토콜:** HTTP/HTTPS, WebSocket
- **인코딩:** UTF-8
- **CORS:** 모든 Origin 허용 (`@CrossOrigin(origins = "*")`)

---

## 🔌 **WebSocket API**

### **ESP32 WebSocket 연결**
- **URL:** `ws://43.200.102.14:5000/ws/esp32`
- **용도:** ESP32와 실시간 통신

#### **연결 시 초기 응답**
```json
{
    "pageNumber": "0\n",
    "date": "2025-09-30\n",
    "color": "#00FF00\n"
}
```

#### **ESP32 → 서버 메시지**

| 메시지 타입 | 요청 JSON | 설명 |
|------------|-----------|------|
| **user_login** | `{"type": "user_login", "userId": 4}` | 사용자 로그인 |
| **next_book** | `{"type": "next_book"}` | 다음 책 요청 (순환) |
| **book_selection** | `{"type": "book_selection", "userId": 4, "bookId": 54}` | 책 선택 |
| **session_start** | `{"type": "session_start", "userId": 4, "bookId": 54}` | 독서 세션 시작 |
| **ping** | `{"type": "ping"}` | 연결 확인 |
| **status** | `{"type": "status"}` | 상태 요청 |

#### **서버 → ESP32 응답**

| 응답 타입 | 응답 JSON | 설명 |
|-----------|-----------|------|
| **user_login** | `{"text": "유저: 4\\n\n책: 54\\n\n제목: ㄷㄱㄷ\\n\n(1/2)\\n\n색깔: #00FF00"}` | 첫 번째 책 정보 |
| **next_book** | `{"text": "유저: 4\\n\n책: 32\\n\n제목: 제발\\n\n(2/2)\\n\n색깔: #00FF00"}` | 다음 책 정보 |
| **book_selection** | `{"type": "book_selection_response\n", "text": "BOOK_SELECTED\nuser : 4\nbook : 54\n책 제목 : ㄷㄱㄷ\n상태 : success\n색깔:#00FF00\n", "color": "#00FF00\n"}` | 책 선택 확인 |
| **session_start** | `{"type": "SESSION_STARTED\n", "bookId": "54\n", "userId": "4\n", "status": "success\n", "message": "읽기 세션이 시작되었습니다.\n", "color": "#00FF00\n"}` | 세션 시작 확인 |

### **ESP32-CAM WebSocket 연결**
- **URL:** `ws://43.200.102.14:5000/ws/esp32-cam`
- **용도:** ESP32-CAM과 실시간 통신

---

## 📡 **REST API**

### **1. 이미지 업로드 (ESP32-CAM)**

#### **단일 이미지 업로드**
- **URL:** `POST /upload/esp32-cam/upload`
- **Content-Type:** `multipart/form-data`
- **Body:**
  ```
  image: [이미지 파일]
  ```
- **응답:**
```json
{
    "pageNumber": 79,
    "date": "2025-09-30",
    "color": "#00FF00",
    "success": true,
    "message": "페이지 번호가 성공적으로 추출되었습니다.",
    "userId": 4,
    "bookId": 54
}
```

#### **현재 페이지 업데이트**
- **URL:** `POST /upload/current-page`
- **Content-Type:** `multipart/form-data`
- **Body:**
  ```
  image: [이미지 파일]
  deviceId: [선택사항]
  ```

#### **마지막 페이지 전송**
- **URL:** `POST /upload/final-page`
- **Content-Type:** `multipart/form-data`
- **Body:**
  ```
  image: [이미지 파일]
  ```

#### **독서 세션 초기화**
- **URL:** `POST /upload/reset-session`
- **응답:**
```json
{
    "success": true,
    "message": "독서 세션이 초기화되었습니다.",
    "deviceId": "ESP32-DEFAULT"
}
```

#### **상태 확인**
- **URL:** `GET /upload/health`
- **URL:** `GET /upload/status`
- **URL:** `GET /upload/diagnostics`

### **2. 사용자 관리**

#### **사용자 회원가입**
- **URL:** `POST /api/users/join`
- **Content-Type:** `application/json`
- **Body:**
```json
{
    "username": "testuser",
    "email": "test@example.com",
    "password": "password123",
    "name": "테스트 사용자"
}
```

#### **사용자 로그인**
- **URL:** `POST /api/users/login`
- **Content-Type:** `application/json`
- **Body:**
```json
{
    "email": "test@example.com",
    "password": "password123"
}
```

#### **사용자 목록 조회**
- **URL:** `GET /api/users`

#### **사용자 정보 조회**
- **URL:** `GET /api/users/{userId}`

#### **로그인 상태 확인**
- **URL:** `GET /api/users/check-login/{userId}`

#### **로그아웃**
- **URL:** `POST /api/users/logout`

#### **프로필 이미지 업로드**
- **URL:** `POST /api/users/{userId}/profile-image`
- **Content-Type:** `multipart/form-data`
- **Body:**
  ```
  image: [이미지 파일]
  ```

#### **프로필 이미지 삭제**
- **URL:** `DELETE /api/users/{userId}/profile-image`

### **3. 책 관리**

#### **책 등록**
- **URL:** `POST /api/books`
- **Content-Type:** `multipart/form-data`
- **Body:**
  ```
  title: "어린왕자"
  author: "앙투안 드 생텍쥐페리"
  coverImage: [이미지 파일]
  ```

#### **책 목록 조회**
- **URL:** `GET /api/books`

#### **책 검색**
- **URL:** `GET /api/books/search?keyword=검색어`

#### **책 상세 조회**
- **URL:** `GET /api/books/{bookId}`

#### **책 정보 수정**
- **URL:** `PUT /api/books/{bookId}`
- **Content-Type:** `application/json`

#### **책 삭제**
- **URL:** `DELETE /api/books/{bookId}`

#### **책 표지 업로드**
- **URL:** `POST /api/books/{bookId}/cover`
- **Content-Type:** `multipart/form-data`
- **Body:**
  ```
  coverImage: [이미지 파일]
  ```

#### **책 표지 삭제**
- **URL:** `DELETE /api/books/{bookId}/cover`

### **4. 사용자 책 관리**

#### **책을 내 서재에 추가**
- **URL:** `POST /api/userbooks`
- **Content-Type:** `application/json`
- **Body:**
```json
{
    "userId": 4,
    "bookId": 54,
    "status": "READING",
    "currentPage": 0
}
```

#### **사용자 책 목록 조회**
- **URL:** `GET /api/userbooks/user/{userId}`

#### **상태별 사용자 책 조회**
- **URL:** `GET /api/userbooks/user/{userId}/status/{status}`
- **Status:** `READING`, `COMPLETED`, `PAUSED`, `PLANNED`

#### **책 상태 업데이트**
- **URL:** `PUT /api/userbooks/{userBookId}/status`
- **Content-Type:** `application/json`
- **Body:**
```json
{
    "status": "COMPLETED"
}
```

#### **현재 페이지 업데이트**
- **URL:** `PUT /api/userbooks/{userBookId}/page`
- **Content-Type:** `application/json`
- **Body:**
```json
{
    "currentPage": 79
}
```

#### **사용자 책 삭제**
- **URL:** `DELETE /api/userbooks/{userBookId}`

### **5. 독서 세션 관리**

#### **독서 세션 시작**
- **URL:** `POST /esp32/session/start`
- **응답:**
```json
{
    "success": true,
    "message": "독서 세션이 시작되었습니다.",
    "sessionId": 1,
    "startTime": "2025-09-30T10:00:00",
    "userId": 1,
    "bookId": 1,
    "date": "2025-09-30",
    "readingPeriod": "1일차",
    "currentConsecutiveDays": 1
}
```

#### **독서 세션 종료**
- **URL:** `POST /esp32/session/end`
- **응답:**
```json
{
    "success": true,
    "message": "독서 세션이 종료되었습니다.",
    "totalPagesRead": 10,
    "totalNumbersRead": 5,
    "endTime": "2025-09-30T11:00:00",
    "userId": 1,
    "date": "2025-09-30",
    "readingPeriod": "1일차",
    "currentConsecutiveDays": 1
}
```

#### **독서 통계 조회**
- **URL:** `GET /esp32/stats`
- **응답:**
```json
{
    "success": true,
    "stats": {
        "maxConsecutiveDays": 5,
        "totalReadingDays": 10,
        "currentConsecutiveDays": 3
    },
    "userId": 1
}
```

### **6. 독서 로그**

#### **독서 로그 생성**
- **URL:** `POST /api/readinglogs`
- **Content-Type:** `application/json`
- **Body:**
```json
{
    "userId": 4,
    "readDate": "2025-09-30",
    "pagesRead": 10
}
```

#### **독서 로그 조회 (날짜 범위)**
- **URL:** `GET /api/readinglogs/user/{userId}?startDate=2025-09-01&endDate=2025-09-30`

#### **오늘 읽은 페이지 수**
- **URL:** `GET /api/readinglogs/user/{userId}/today`

#### **독서 통계**
- **URL:** `GET /api/readinglogs/user/{userId}/stats`
- **응답:**
```json
{
    "success": true,
    "maxConsecutiveDays": 5,
    "totalReadingDays": 10
}
```

#### **일일 독서 통계**
- **URL:** `GET /api/readinglogs/user/{userId}/daily?startDate=2025-09-01&endDate=2025-09-30`

### **7. 캘린더**

#### **캘린더 통계 조회**
- **URL:** `GET /api/calendar/{userId}/stats`
- **응답:**
```json
{
    "success": true,
    "currentMonth": {
        "readingRate": 80.5,
        "maxConsecutiveDays": 5,
        "totalReadingDays": 10,
        "todayPagesRead": 5,
        "totalStamps": 3,
        "stampDates": ["2025-09-28", "2025-09-29", "2025-09-30"],
        "consecutiveNonReadingDays": 0
    },
    "message": "캘린더 통계 조회 성공"
}
```

#### **현재 연속 독서일**
- **URL:** `GET /api/calendar/{userId}/current-consecutive`

### **8. 미션**

#### **오늘의 미션 조회**
- **URL:** `GET /api/missions/user/{userId}/today`
- **응답:**
```json
{
    "success": true,
    "missions": [
        {
            "missionId": 1,
            "title": "10페이지 읽기",
            "description": "오늘 10페이지를 읽어보세요",
            "targetValue": 10,
            "currentValue": 5,
            "completed": false,
            "reward": 10
        }
    ],
    "count": 1,
    "message": "오늘의 미션 조회 성공"
}
```

#### **미션 완료**
- **URL:** `POST /api/missions/{missionId}/complete?userId=4`

#### **미션 진행 현황**
- **URL:** `GET /api/missions/user/{userId}/progress`

#### **모든 미션 목록**
- **URL:** `GET /api/missions`

### **9. 마이페이지**

#### **사용자 통계**
- **URL:** `GET /api/mypage/user/{userId}/stats`

#### **즐겨찾기 페이지 조회**
- **URL:** `GET /api/mypage/user/{userId}/favorite-pages`

#### **즐겨찾기 페이지 추가**
- **URL:** `POST /api/mypage/user/{userId}/favorite-page`
- **Body:**
  ```
  bookId: 54
  pageNumber: 79
  ```

#### **즐겨찾기 페이지 수정**
- **URL:** `PUT /api/mypage/favorite-page/{favPageId}`
- **Body:**
  ```
  pageNumber: 80
  ```

#### **즐겨찾기 페이지 삭제**
- **URL:** `DELETE /api/mypage/favorite-page/{favPageId}`

#### **즐겨찾기 문장 조회**
- **URL:** `GET /api/mypage/user/{userId}/favorite-quotes`

#### **즐겨찾기 문장 추가**
- **URL:** `POST /api/mypage/user/{userId}/favorite-quote`
- **Content-Type:** `multipart/form-data`
- **Body:**
  ```
  pageNumber: 79
  content: "인용문 내용"
  bookTitle: "책 제목"
  coverImage: [이미지 파일] (선택사항)
  coverImageUrl: "이미지 URL" (선택사항)
  ```

#### **즐겨찾기 문장 수정**
- **URL:** `PUT /api/mypage/favorite-quote/{favQuoteId}`
- **Content-Type:** `multipart/form-data`

#### **즐겨찾기 문장 삭제**
- **URL:** `DELETE /api/mypage/favorite-quote/{favQuoteId}`

---

## 🎨 **색상 코드**

| 색상 | 코드 | 용도 |
|------|------|------|
| **성공** | `#00FF00` | 정상 처리, 연결 성공 |
| **에러** | `#FF0000` | 오류 발생, 실패 |
| **경고** | `#FFFF00` | 주의, 안내 메시지 |

---

## 📝 **응답 형식**

### **성공 응답**
```json
{
    "success": true,
    "message": "요청이 성공적으로 처리되었습니다.",
    "data": { ... }
}
```

### **에러 응답**
```json
{
    "success": false,
    "message": "오류 메시지",
    "error": "ERROR_CODE"
}
```

---

## 🔧 **에러 코드**

| 코드 | 설명 |
|------|------|
| `400` | 잘못된 요청 |
| `401` | 인증 실패 |
| `403` | 권한 없음 |
| `404` | 리소스 없음 |
| `500` | 서버 내부 오류 |

---

## 📋 **사용 예시**

### **ESP32 연결 및 책 탐색**
```cpp
// 1. WebSocket 연결
webSocket.begin("43.200.102.14", 5000, "/ws/esp32");

// 2. 사용자 로그인
char jsonLogin[50];
snprintf(jsonLogin, sizeof(jsonLogin), "{\"type\":\"user_login\",\"userId\":4}");
webSocket.sendTXT(jsonLogin);

// 3. 다음 책 요청
char jsonNextBook[50];
snprintf(jsonNextBook, sizeof(jsonNextBook), "{\"type\":\"next_book\"}");
webSocket.sendTXT(jsonNextBook);

// 4. 책 선택
char jsonSelectBook[100];
snprintf(jsonSelectBook, sizeof(jsonSelectBook), 
         "{\"type\":\"book_selection\",\"userId\":4,\"bookId\":54}");
webSocket.sendTXT(jsonSelectBook);
```

### **ESP32-CAM 이미지 업로드**
```cpp
// HTTP POST 요청
http.begin("http://43.200.102.14:5000/upload/esp32-cam/upload");
http.addHeader("Content-Type", "multipart/form-data");

// 이미지 파일 전송
http.POST(imageData);
```

---

## 🚀 **주요 특징**

1. **실시간 통신**: WebSocket을 통한 ESP32와의 실시간 통신
2. **OCR 처리**: Google Vision API를 통한 페이지 번호 추출
3. **세션 관리**: 사용자별 독서 세션 추적
4. **이미지 처리**: 책 표지, 프로필 이미지, OCR 이미지 처리
5. **통계 제공**: 독서 통계, 캘린더, 미션 시스템
6. **즐겨찾기**: 페이지와 문장 즐겨찾기 기능
7. **CORS 지원**: 모든 Origin에서 접근 가능

이 API 명세서는 ReadMark 서버의 모든 기능을 포함하고 있으며, ESP32와 ESP32-CAM이 서버와 통신할 때 필요한 모든 정보를 제공합니다.