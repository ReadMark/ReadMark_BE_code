# 📚 API 명세서

## 🌐 서버 정보
- **Base URL:** `http://43.200.102.14:5000`
- **WebSocket URL:** `ws://43.200.102.14:5000`
- **프로토콜:** HTTP/HTTPS, WebSocket
- **인코딩:** UTF-8
- **CORS:** 모든 Origin 허용

---

### 💻 HTTP RESTful API

#### /users (사용자 관리)

**회원가입**
`POST /api/users/join`

### Request
```json
{
  "username": "testuser",
  "password": "password123",
  "email": "test@example.com",
  "name": "Test User"
}
```

### Response
```json
{
  "success": true,
  "message": "User registered successfully",
  "data": {
    "userId": 1,
    "username": "testuser"
  }
}
```

**로그인**
`POST /api/users/login`

### Request
```json
{
  "email": "test@example.com",
  "password": "password123"
}
```

### Response
```json
{
  "success": true,
  "message": "Login successful",
  "data": {
    "userId": 1,
    "username": "testuser"
  }
}
```

**사용자 목록 조회**
`GET /api/users`

### Response
```json
{
  "success": true,
  "data": [
    {
      "userId": 1,
      "username": "testuser",
      "email": "test@example.com"
    }
  ]
}
```

**사용자 정보 조회**
`GET /api/users/{userId}`

### Response
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "username": "testuser",
    "email": "test@example.com",
    "name": "Test User"
  }
}
```

**로그인 상태 확인**
`GET /api/users/check-login/{userId}`

### Response
```json
{
  "success": true,
  "data": {
    "userId": 1,
    "isLoggedIn": true,
    "username": "testuser"
  }
}
```

**로그아웃**
`POST /api/users/logout`

### Response
```json
{
  "success": true,
  "message": "Logout successful"
}
```

**프로필 이미지 업로드**
`POST /api/users/{userId}/profile-image`

### Request
- `image` (Multipart/form-data)

### Response
```json
{
  "success": true,
  "message": "프로필 사진이 업로드되었습니다.",
  "profileImageUrl": "/uploads/profile/user_1_image.jpg"
}
```

**프로필 이미지 삭제**
`DELETE /api/users/{userId}/profile-image`

### Response
```json
{
  "success": true,
  "message": "프로필 사진이 삭제되었습니다."
}
```

#### /books (책 관리)

**새 책 등록**
`POST /api/books`

### Request
- `title` (String)
- `author` (String)
- `coverImage` (Multipart/form-data)

### Response
```json
{
  "success": true,
  "message": "책이 등록되었습니다.",
  "bookId": 1,
  "coverImageUrl": "/uploads/books/book_1_cover.jpg"
}
```

**모든 책 목록 조회**
`GET /api/books`

### Response
```json
{
  "success": true,
  "books": [
    {
      "bookId": 1,
      "title": "어린왕자",
      "author": "앙투안 드 생텍쥐페리"
    }
  ],
  "count": 1
}
```

**책 검색**
`GET /api/books/search?keyword=검색어`

### Response
```json
{
  "success": true,
  "books": [
    {
      "bookId": 1,
      "title": "어린왕자"
    }
  ],
  "count": 1
}
```

**특정 책 정보 조회**
`GET /api/books/{bookId}`

### Response
```json
{
  "success": true,
  "book": {
    "bookId": 1,
    "title": "어린왕자",
    "author": "앙투안 드 생텍쥐페리"
  }
}
```

**책 정보 업데이트**
`PUT /api/books/{bookId}`

### Request
```json
{
  "title": "업데이트된 책 제목",
  "author": "새로운 저자"
}
```

### Response
```json
{
  "success": true,
  "message": "책 정보가 수정되었습니다.",
  "book": {
    "bookId": 1,
    "title": "업데이트된 책 제목",
    "author": "새로운 저자"
  }
}
```

**책 삭제**
`DELETE /api/books/{bookId}`

### Response
```json
{
  "success": true,
  "message": "책이 삭제되었습니다.",
  "deletedBookId": 1
}
```

**책 표지 업로드**
`POST /api/books/{bookId}/cover`

### Request
- `coverImage` (Multipart/form-data)

### Response
```json
{
  "success": true,
  "message": "책 표지가 업로드되었습니다.",
  "coverImageUrl": "/uploads/books/book_1_cover.jpg"
}
```

**책 표지 삭제**
`DELETE /api/books/{bookId}/cover`

### Response
```json
{
  "success": true,
  "message": "책 표지가 삭제되었습니다."
}
```

#### /userbooks (사용자-책 관계 관리)

**사용자에게 책 할당**
`POST /api/userbooks`

### Request
```json
{
  "userId": 1,
  "bookId": 1,
  "status": "READING",
  "currentPage": 0
}
```

### Response
```json
{
  "success": true,
  "message": "사용자-책 관계가 생성되었습니다.",
  "data": {
    "userBookId": 1
  }
}
```

**특정 사용자의 모든 책 조회**
`GET /api/userbooks/user/{userId}`

### Response
```json
{
  "success": true,
  "data": [
    {
      "userBookId": 1,
      "bookId": 1,
      "title": "어린왕자",
      "status": "READING"
    }
  ]
}
```

**상태별 사용자 책 조회**
`GET /api/userbooks/user/{userId}/status/{status}`

### Response
```json
{
  "success": true,
  "data": [
    {
      "userBookId": 1,
      "bookId": 1,
      "title": "어린왕자",
      "status": "READING"
    }
  ]
}
```

**책 상태 업데이트**
`PUT /api/userbooks/{userBookId}/status`

### Request
```json
{
  "status": "COMPLETED"
}
```

### Response
```json
{
  "success": true,
  "message": "책 상태가 업데이트되었습니다.",
  "data": {
    "userBookId": 1,
    "status": "COMPLETED"
  }
}
```

**현재 페이지 업데이트**
`PUT /api/userbooks/{userBookId}/page`

### Request
```json
{
  "currentPage": 50
}
```

### Response
```json
{
  "success": true,
  "message": "현재 페이지가 업데이트되었습니다.",
  "data": {
    "userBookId": 1,
    "currentPage": 50
  }
}
```

**사용자-책 관계 삭제**
`DELETE /api/userbooks/{userBookId}`

### Response
```json
{
  "success": true,
  "message": "사용자-책 관계가 삭제되었습니다."
}
```

#### /readinglogs (독서 로그 관리)

**독서 로그 기록**
`POST /api/readinglogs`

### Request
```json
{
  "userId": 1,
  "readDate": "2025-09-30",
  "pagesRead": 10
}
```

### Response
```json
{
  "success": true,
  "message": "독서 기록이 저장되었습니다.",
  "logId": 1,
  "pagesRead": 10
}
```

**독서 로그 조회 (날짜 범위)**
`GET /api/readinglogs/user/{userId}?startDate=2025-09-01&endDate=2025-09-30`

### Response
```json
{
  "success": true,
  "readingLogs": [
    {
      "logId": 1,
      "readDate": "2025-09-30",
      "pagesRead": 10
    }
  ]
}
```

**오늘 읽은 페이지 수**
`GET /api/readinglogs/user/{userId}/today`

### Response
```json
{
  "success": true,
  "pagesRead": 5,
  "date": "2025-09-30"
}
```

**독서 통계**
`GET /api/readinglogs/user/{userId}/stats`

### Response
```json
{
  "success": true,
  "maxConsecutiveDays": 5,
  "totalReadingDays": 10
}
```

**일일 독서 통계**
`GET /api/readinglogs/user/{userId}/daily?startDate=2025-09-01&endDate=2025-09-30`

### Response
```json
{
  "success": true,
  "dailyStats": [
    {
      "date": "2025-09-30",
      "pagesRead": 10
    }
  ]
}
```

#### /mypage (마이페이지)

**사용자 통계**
`GET /api/mypage/user/{userId}/stats`

### Response
```json
{
  "success": true,
  "stats": {
    "totalBooks": 5,
    "completedBooks": 2,
    "totalPages": 1000
  }
}
```

**즐겨찾기 페이지 조회**
`GET /api/mypage/user/{userId}/favorite-pages`

### Response
```json
{
  "success": true,
  "favoritePages": [
    {
      "favPageId": 1,
      "bookId": 1,
      "pageNumber": 79
    }
  ]
}
```

**즐겨찾기 페이지 추가**
`POST /api/mypage/user/{userId}/favorite-page`

### Request
- `bookId` (Long)
- `pageNumber` (Integer)

### Response
```json
{
  "success": true,
  "message": "즐겨찾기한 페이지가 저장되었습니다.",
  "favoritePageId": 1
}
```

**즐겨찾기 페이지 수정**
`PUT /api/mypage/favorite-page/{favPageId}`

### Request
- `pageNumber` (Integer, 선택사항)

### Response
```json
{
  "success": true,
  "message": "즐겨찾기한 페이지가 수정되었습니다.",
  "favoritePage": {
    "favPageId": 1,
    "pageNumber": 80
  }
}
```

**즐겨찾기 페이지 삭제**
`DELETE /api/mypage/favorite-page/{favPageId}`

### Response
```json
{
  "success": true,
  "message": "즐겨찾기한 페이지가 삭제되었습니다.",
  "deletedFavPageId": 1
}
```

**즐겨찾기 문장 조회**
`GET /api/mypage/user/{userId}/favorite-quotes`

### Response
```json
{
  "success": true,
  "favoriteQuotes": [
    {
      "favQuoteId": 1,
      "pageNumber": 79,
      "content": "인용문",
      "bookTitle": "책제목"
    }
  ]
}
```

**즐겨찾기 문장 추가**
`POST /api/mypage/user/{userId}/favorite-quote`

### Request
- `pageNumber` (Integer)
- `content` (String)
- `bookTitle` (String)
- `coverImage` (Multipart/form-data, 선택사항)
- `coverImageUrl` (String, 선택사항)

### Response
```json
{
  "success": true,
  "message": "즐겨찾기한 문장이 저장되었습니다.",
  "favoriteQuoteId": 1,
  "coverImageUrl": "/uploads/favorite-quote-covers/user_1_quote.jpg"
}
```

**즐겨찾기 문장 수정**
`PUT /api/mypage/favorite-quote/{favQuoteId}`

### Request
- `pageNumber` (Integer, 선택사항)
- `content` (String, 선택사항)
- `bookTitle` (String, 선택사항)
- `coverImage` (Multipart/form-data, 선택사항)
- `coverImageUrl` (String, 선택사항)

### Response
```json
{
  "success": true,
  "message": "즐겨찾기한 문장이 수정되었습니다.",
  "favoriteQuote": {
    "favQuoteId": 1,
    "pageNumber": 79,
    "content": "수정된 인용문",
    "bookTitle": "책제목"
  }
}
```

**즐겨찾기 문장 삭제**
`DELETE /api/mypage/favorite-quote/{favQuoteId}`

### Response
```json
{
  "success": true,
  "message": "즐겨찾기한 문장이 삭제되었습니다.",
  "deletedFavQuoteId": 1
}
```

#### /upload (ESP32-CAM 이미지 업로드)

**ESP32-CAM 이미지 업로드 및 OCR 처리**
`POST /upload/esp32-cam/upload`

### Request
- `image` (Multipart/form-data)

### Response
```json
{
  "success": true,
  "message": "페이지 번호가 성공적으로 추출되었습니다.",
  "pageNumber": 79,
  "bookId": 54,
  "userId": 4,
  "color": "#00FF00"
}
```

**현재 페이지 이미지 업로드 및 OCR 처리**
`POST /upload/current-page`

### Request
- `image` (Multipart/form-data)
- `deviceId` (String, 선택사항)

### Response
```json
{
  "success": true,
  "currentPage": 79,
  "confidence": 0.95,
  "bookId": 54,
  "userId": 4
}
```

**마지막 페이지 이미지 업로드 및 OCR 처리**
`POST /upload/final-page`

### Request
- `image` (Multipart/form-data)

### Response
```json
{
  "success": true,
  "finalPage": 200,
  "confidence": 0.95,
  "readingStartDate": "2025-09-30",
  "totalReadingDays": 5,
  "startPage": 1,
  "message": "독서 완료!"
}
```

**일반 이미지 업로드 (OCR 처리 포함)**
`POST /upload`

### Request
- `image` (Multipart/form-data)

### Response
```json
{
  "success": true,
  "pageNumber": 79
}
```

**독서 세션 초기화**
`POST /upload/reset-session`

### Response
```json
{
  "success": true,
  "message": "독서 세션이 초기화되었습니다.",
  "deviceId": "ESP32-DEFAULT"
}
```

**상태 확인**
`GET /upload/health`

### Response
```json
{
  "status": "OK",
  "message": "ESP32 Upload Service is running",
  "timestamp": "2025-09-30T10:00:00"
}
```

**서버 상태 조회**
`GET /upload/status`

### Response
```json
{
  "status": "OK",
  "mode": "REST API",
  "timestamp": "2025-09-30T10:00:00",
  "serverPort": "5000"
}
```

**진단 정보**
`GET /upload/diagnostics`

### Response
```json
{
  "serverPort": "5000",
  "serverAddress": "0.0.0.0",
  "mode": "REST API",
  "corsEnabled": true,
  "endpoints": {
    "upload": "/upload/",
    "health": "/upload/health",
    "status": "/upload/status",
    "test": "/upload/test",
    "diagnostics": "/upload/diagnostics"
  }
}
```

**테스트 메시지**
`POST /upload/test`

### Request
- `message` (String)

### Response
```json
{
  "success": true,
  "message": "테스트 메시지 수신: Hello",
  "timestamp": "2025-09-30T10:00:00",
  "mode": "REST API"
}
```

#### /esp32-session (ESP32 세션 관리)

**ESP32 세션 시작**
`POST /esp32/session/start`

### Response
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

**ESP32 세션 종료**
`POST /esp32/session/end`

### Response
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

**ESP32 통계 조회**
`GET /esp32/stats`

### Response
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

#### /calendar (캘린더)

**캘린더 통계 조회**
`GET /api/calendar/{userId}/stats`

### Response
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

**현재 연속 독서일**
`GET /api/calendar/{userId}/current-consecutive`

### Response
```json
{
  "success": true,
  "currentConsecutiveDays": 5,
  "message": "현재 연속 독서일 조회 성공"
}
```

#### /missions (미션)

**오늘의 미션 조회**
`GET /api/missions/user/{userId}/today`

### Response
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

**미션 완료**
`POST /api/missions/{missionId}/complete?userId=4`

### Response
```json
{
  "success": true,
  "message": "미션이 완료되었습니다.",
  "completedMissionId": 1
}
```

**미션 진행 현황**
`GET /api/missions/user/{userId}/progress`

### Response
```json
{
  "success": true,
  "progress": {
    "completedMissions": 5,
    "totalMissions": 10,
    "completionRate": 50.0
  },
  "message": "미션 진행 현황 조회 성공"
}
```

**모든 미션 목록**
`GET /api/missions`

### Response
```json
{
  "success": true,
  "missions": [
    {
      "missionId": 1,
      "title": "10페이지 읽기",
      "description": "오늘 10페이지를 읽어보세요"
    }
  ],
  "count": 1,
  "message": "미션 목록 조회 성공"
}
```

#### /ws (WebSocket 정보 - HTTP GET)

**WebSocket 연결 정보 제공**
`GET /ws`

### Response
```json
{
  "message": "이 경로는 WebSocket 연결을 위한 것입니다.",
  "info": "WebSocket 클라이언트를 사용하여 연결해주세요.",
  "esp32_websocket_url": "ws://43.200.102.14:5000/ws/esp32",
  "esp32_cam_websocket_url": "ws://43.200.102.14:5000/ws/esp32-cam",
  "generic_websocket_url": "ws://43.200.102.14:5000/ws"
}
```

---

### 🔌 WebSocket API

#### ESP32 WebSocket 연결
- **URL:** `ws://43.200.102.14:5000/ws/esp32`
- **용도:** ESP32와 실시간 통신

**연결 시 초기 응답**
```json
{
  "pageNumber": "0\n",
  "date": "2025-09-30\n",
  "color": "#00FF00\n"
}
```

**ESP32 → 서버 메시지**

**사용자 로그인**
```json
{
  "type": "user_login",
  "userId": 4
}
```

**다음 책 요청**
```json
{
  "type": "next_book"
}
```

**책 선택**
```json
{
  "type": "book_selection",
  "userId": 4,
  "bookId": 54
}
```

**세션 시작**
```json
{
  "type": "session_start",
  "userId": 4,
  "bookId": 54
}
```

**연결 유지 (Heartbeat)**
```json
{
  "type": "ping"
}
```

**서버 → ESP32 메시지**

**사용자 로그인 후 첫 책 정보**
```json
{
  "text": "유저: 4\n\n책: 54\n제목: ㄷㄱㄷ\n\n(1/2)\n\n색깔: #00FF00"
}
```

**다음 책 정보 (순환)**
```json
{
  "text": "유저: 4\n\n책: 32\n제목: 제발\n\n(2/2)\n\n색깔: #00FF00"
}
```

**책 선택 확인**
```json
{
  "text": "BOOK_SELECTED\nuser : 4\nbook : 54\n책 제목 : ㄷㄱㄷ\n상태 : success\n색깔: #00FF00"
}
```

**OCR 결과 (페이지 번호)**
```json
{
  "pageNumber": "10\n",
  "date": "2025-09-30\n",
  "color": "#00FF00\n"
}
```

**세션 시작 알림**
```json
{
  "status": "started\n",
  "message": "세션 시작됨\n",
  "color": "#00FF00\n"
}
```

**오류 메시지**
```json
{
  "message": "오류 발생\n",
  "color": "#FF0000\n"
}
```

**Ping 응답**
```json
{
  "message": "pong\n",
  "color": "#00FF00\n"
}
```

---

### ⚠️ 참고 사항
- 모든 HTTP API는 JWT 토큰을 통한 인증이 필요할 수 있습니다 (명세서에 명시되지 않은 경우).
- `userId` 및 `bookId`는 실제 데이터베이스에 존재하는 유효한 ID여야 합니다.
- `\n` 문자는 실제 줄바꿈과 함께 문자열 내에 포함될 수 있습니다.
- `color` 필드는 ESP32 OLED 디스플레이에 사용될 색상 코드를 나타냅니다 (`#00FF00` for 성공, `#FF0000` for 실패, `#FFFF00` for 경고).
