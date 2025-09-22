# 📚 ReadMark API 명세서 (최종본)

## 🚀 서버 정보

- **서버 주소**: `43.200.102.14:5000`
- **프로토콜**: HTTP/HTTPS
- **CORS**: 모든 도메인 허용 (`*`)
- **데이터베이스**: MySQL 8.0 (43.200.102.14:3306)
- **이미지 저장**: MEDIUMBLOB (최대 16MB)

---

## 📋 목차

1. [사용자 관리 API](#1-사용자-관리-api)
2. [책 관리 API](#2-책-관리-api)
3. [사용자-책 관계 API](#3-사용자-책-관계-api)
4. [이미지 업로드 API](#4-이미지-업로드-api)
5. [ESP32 전용 API](#5-esp32-전용-api)
6. [독서 로그 API](#6-독서-로그-api)
7. [캘린더 API](#7-캘린더-api)
8. [마이페이지 API](#8-마이페이지-api)
9. [웹소켓 API](#9-웹소켓-api)
10. [공통 응답 형식](#10-공통-응답-형식)
11. [에러 코드](#11-에러-코드)

---

## 1. 사용자 관리 API

### 1.1 사용자 회원가입

**POST** `/api/users/join`

**Request Body:**
```json
{
  "name": "홍길동",
  "username": "hong123",
  "email": "hong@example.com",
  "password": "password123!"
}
```

**Response:**
```json
{
  "success": true,
  "message": "회원가입이 완료되었습니다.",
  "data": {
    "userId": 1,
    "name": "홍길동",
    "username": "hong123",
    "email": "hong@example.com",
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

### 1.2 사용자 로그인

**POST** `/api/users/login`

**Request Body:**
```json
{
  "email": "hong@example.com",
  "password": "password123!"
}
```

**Response:**
```json
{
  "success": true,
  "message": "로그인에 성공했습니다.",
  "data": {
    "userId": 1,
    "name": "홍길동",
    "username": "hong123",
    "email": "hong@example.com"
  }
}
```

### 1.3 전체 사용자 조회

**GET** `/api/users`

**Response:**
```json
{
  "success": true,
  "message": "사용자 목록 조회에 성공했습니다.",
  "data": [
    {
      "userId": 1,
      "name": "홍길동",
      "username": "hong123",
      "email": "hong@example.com",
      "createdAt": "2024-01-15T10:30:00"
    }
  ],
  "count": 1
}
```

### 1.4 사용자 정보 조회

**GET** `/api/users/{userId}`

**Response:**
```json
{
  "success": true,
  "message": "사용자 정보 조회에 성공했습니다.",
  "data": null
}
```

---

## 2. 책 관리 API

### 2.1 책 등록

**POST** `/api/books`

**Request Body:**
```json
{
  "title": "자바의 정석",
  "author": "남궁성",
  "publisher": "도우출판",
  "coverImageUrl": "https://example.com/cover.jpg",
  "publishedAt": "2020-01-01"
}
```

**Response:**
```json
{
  "success": true,
  "message": "책이 등록되었습니다.",
  "bookId": 1
}
```

### 2.2 책 검색

**GET** `/api/books/search?keyword=자바`

**Response:**
```json
{
  "success": true,
  "books": [
    {
      "bookId": 1,
      "title": "자바의 정석",
      "author": "남궁성",
      "publisher": "도우출판",
      "coverImageUrl": "https://example.com/cover.jpg",
      "publishedAt": "2020-01-01"
    }
  ],
  "count": 1
}
```

### 2.3 책 상세 조회

**GET** `/api/books/{bookId}`

**Response:**
```json
{
  "success": true,
  "book": {
    "bookId": 1,
    "title": "자바의 정석",
    "author": "남궁성",
    "publisher": "도우출판",
    "coverImageUrl": "https://example.com/cover.jpg",
    "publishedAt": "2020-01-01",
    "createdAt": "2024-01-15T10:30:00"
  },
  "message": "책 정보 조회 성공"
}
```

---

## 3. 사용자-책 관계 API

### 3.1 사용자 책 추가

**POST** `/api/userbooks`

**Request Body:**
```json
{
  "userId": 1,
  "bookId": 1,
  "status": "WANNA_READ",
  "currentPage": 0
}
```

**Status 값:**
- `WANNA_READ`: 읽고 싶은 책
- `NOW_READ`: 현재 읽는 책
- `READ_DONE`: 읽은 책
- `READING`: 읽는 중 (레거시)
- `COMPLETED`: 완료 (레거시)

**Response:**
```json
{
  "success": true,
  "message": "사용자 책이 추가되었습니다.",
  "data": {
    "userBookId": 1,
    "userId": 1,
    "bookId": 1,
    "status": "WANNA_READ",
    "currentPage": 0,
    "createdAt": "2024-01-15T10:30:00"
  }
}
```

### 3.2 사용자 책 목록 조회

**GET** `/api/userbooks/user/{userId}`

**Response:**
```json
{
  "success": true,
  "message": "사용자 책 목록 조회에 성공했습니다.",
  "data": [
    {
      "userBookId": 1,
      "bookId": 1,
      "title": "자바의 정석",
      "author": "남궁성",
      "status": "NOW_READ",
      "currentPage": 45,
      "createdAt": "2024-01-15T10:30:00"
    }
  ]
}
```

### 3.3 상태별 사용자 책 조회

**GET** `/api/userbooks/user/{userId}/status/{status}`

**Response:**
```json
{
  "success": true,
  "message": "사용자 책 목록 조회에 성공했습니다.",
  "data": [
    {
      "userBookId": 1,
      "bookId": 1,
      "title": "자바의 정석",
      "author": "남궁성",
      "status": "NOW_READ",
      "currentPage": 45
    }
  ]
}
```

### 3.4 책 상태 업데이트

**PUT** `/api/userbooks/{userBookId}/status`

**Request Body:**
```json
{
  "status": "READ_DONE"
}
```

**Response:**
```json
{
  "success": true,
  "message": "책 상태가 업데이트되었습니다.",
  "data": {
    "userBookId": 1,
    "status": "READ_DONE",
    "updatedAt": "2024-01-15T11:00:00"
  }
}
```

### 3.5 현재 페이지 업데이트

**PUT** `/api/userbooks/{userBookId}/page`

**Request Body:**
```json
{
  "currentPage": 50
}
```

**Response:**
```json
{
  "success": true,
  "message": "현재 페이지가 업데이트되었습니다.",
  "data": {
    "userBookId": 1,
    "currentPage": 50,
    "updatedAt": "2024-01-15T11:00:00"
  }
}
```

---

## 4. 이미지 업로드 API

### 4.1 일반 이미지 업로드

**POST** `/api/image/upload`

**Request (multipart/form-data):**
- `userId`: Long (필수)
- `bookId`: Long (필수)
- `image`: File (필수, 최대 10MB)
- `deviceInfo`: String (선택)
- `captureTime`: String (선택, 형식: "yyyy-MM-dd HH:mm:ss")

**Response:**
```json
{
  "success": true,
  "message": "책 페이지가 성공적으로 저장되었습니다.",
  "page": {
    "pageId": 123,
    "pageNumber": 45,
    "confidence": 0.95,
    "language": "ko",
    "numberCount": 1,
    "capturedAt": "2024-01-15T14:30:00"
  },
  "deviceInfo": "ESP32-CAM"
}
```

### 4.2 독서 세션 시작

**POST** `/api/image/session/start`

**Request (multipart/form-data):**
- `userId`: Long (필수)
- `bookId`: Long (필수)

**Response:**
```json
{
  "success": true,
  "message": "독서 세션이 시작되었습니다.",
  "sessionId": 1,
  "startTime": "2024-01-15T14:30:00"
}
```

### 4.3 독서 세션 종료

**POST** `/api/image/session/end`

**Request (multipart/form-data):**
- `userId`: Long (필수)

**Response:**
```json
{
  "success": true,
  "message": "독서 세션이 종료되었습니다.",
  "totalPagesRead": 5,
  "totalNumbersRead": 3,
  "readingDurationMinutes": 30,
  "endTime": "2024-01-15T15:00:00"
}
```

### 4.4 독서 통계 조회

**GET** `/api/image/stats/{userId}`

**Response:**
```json
{
  "success": true,
  "stats": {
    "maxConsecutiveDays": 7,
    "totalReadingDays": 25,
    "currentConsecutiveDays": 3,
    "habitAnalysis": {
      "averagePagesPerDay": 5.2,
      "favoriteReadingTime": "14:00-16:00",
      "readingFrequency": "매일"
    }
  }
}
```

### 4.5 월별 독서 통계

**GET** `/api/image/stats/{userId}/monthly?months=6`

**Response:**
```json
{
  "success": true,
  "monthlyStats": {
    "2024-01": {
      "totalPages": 150,
      "readingDays": 25,
      "averagePagesPerDay": 6.0
    },
    "2023-12": {
      "totalPages": 120,
      "readingDays": 20,
      "averagePagesPerDay": 6.0
    }
  }
}
```

### 4.6 책 페이지 목록 조회

**GET** `/api/image/pages/{userId}/{bookId}`

**Response:**
```json
{
  "success": true,
  "pages": [
    {
      "pageId": 123,
      "pageNumber": 45,
      "confidence": 0.95,
      "language": "ko",
      "capturedAt": "2024-01-15T14:30:00"
    }
  ],
  "totalPages": 1,
  "count": 1
}
```

### 4.7 특정 페이지 조회

**GET** `/api/image/pages/{userId}/{bookId}/{pageNumber}`

**Response:**
```json
{
  "success": true,
  "page": {
    "pageId": 123,
    "pageNumber": 45,
    "confidence": 0.95,
    "language": "ko",
    "capturedAt": "2024-01-15T14:30:00"
  }
}
```

### 4.8 페이지 범위 조회

**GET** `/api/image/pages/{userId}/{bookId}/range?startPage=40&endPage=50`

**Response:**
```json
{
  "success": true,
  "pages": [
    {
      "pageId": 123,
      "pageNumber": 45,
      "confidence": 0.95,
      "capturedAt": "2024-01-15T14:30:00"
    }
  ],
  "count": 1,
  "range": "40 ~ 50"
}
```

### 4.9 최근 페이지 조회

**GET** `/api/image/pages/{userId}/{bookId}/recent?limit=10`

**Response:**
```json
{
  "success": true,
  "pages": [
    {
      "pageId": 123,
      "pageNumber": 45,
      "confidence": 0.95,
      "capturedAt": "2024-01-15T14:30:00"
    }
  ],
  "count": 1
}
```

### 4.10 페이지 삭제

**DELETE** `/api/image/pages/{userId}/{bookId}/{pageNumber}`

**Response:**
```json
{
  "success": true,
  "message": "페이지가 삭제되었습니다.",
  "deletedPage": 45
}
```

---

## 5. ESP32 전용 API

### 5.1 ESP32 이미지 업로드 (간단)

**POST** `/upload/`

**Request (multipart/form-data):**
- `image`: File (필수, 최대 10MB)

**기본값:**
- `userId`: 1
- `bookId`: 1
- `deviceInfo`: "ESP32-CAM"

**Response:**
```json
{
  "success": true,
  "message": "이미지 업로드 성공",
  "pageId": 123,
  "pageNumber": 45,
  "confidence": 0.95,
  "deviceInfo": "ESP32-CAM",
  "capturedAt": "2024-01-15T14:30:00",
  "date": 3,
  "readingPeriod": "2024.01.13 - 2024.01.15",
  "currentConsecutiveDays": 3
}
```

### 5.2 ESP32 독서 세션 시작

**POST** `/esp32/session/start`

**기본값:**
- `userId`: 1
- `bookId`: 1

**Response:**
```json
{
  "success": true,
  "message": "독서 세션이 시작되었습니다.",
  "sessionId": 1,
  "startTime": "2024-01-15T14:30:00",
  "userId": 1,
  "bookId": 1,
  "date": 3,
  "readingPeriod": "2024.01.13 - 2024.01.15",
  "currentConsecutiveDays": 3
}
```

### 5.3 ESP32 독서 세션 종료

**POST** `/esp32/session/end`

**기본값:**
- `userId`: 1

**Response:**
```json
{
  "success": true,
  "message": "독서 세션이 종료되었습니다.",
  "totalPagesRead": 5,
  "totalNumbersRead": 3,
  "readingDurationMinutes": 30,
  "endTime": "2024-01-15T15:00:00",
  "userId": 1,
  "date": 3,
  "readingPeriod": "2024.01.13 - 2024.01.15",
  "currentConsecutiveDays": 3
}
```

### 5.4 ESP32 통계 조회

**GET** `/esp32/stats`

**기본값:**
- `userId`: 1

**Response:**
```json
{
  "success": true,
  "stats": {
    "maxConsecutiveDays": 7,
    "totalReadingDays": 25,
    "currentConsecutiveDays": 3
  },
  "userId": 1
}
```

### 5.5 ESP32 상태 확인

**GET** `/upload/health`

**Response:**
```json
{
  "status": "OK",
  "message": "ESP32 Upload Service is running",
  "timestamp": "2024-01-15T14:30:00",
  "status": "REST API 모드"
}
```

### 5.6 ESP32 서버 상태

**GET** `/upload/status`

**Response:**
```json
{
  "status": "OK",
  "mode": "REST API",
  "timestamp": "2024-01-15T14:30:00",
  "serverPort": "5000",
  "serverAddress": "0.0.0.0"
}
```

### 5.7 ESP32 테스트 메시지

**POST** `/upload/test`

**Request (multipart/form-data):**
- `message`: String (필수)

**Response:**
```json
{
  "success": true,
  "message": "테스트 메시지 수신: Hello ESP32",
  "timestamp": "2024-01-15T14:30:00",
  "mode": "REST API"
}
```

### 5.8 ESP32 진단 정보

**GET** `/upload/diagnostics`

**Response:**
```json
{
  "serverPort": "5000",
  "serverAddress": "0.0.0.0",
  "mode": "REST API",
  "corsEnabled": true,
  "timestamp": "2024-01-15T14:30:00",
  "endpoints": {
    "upload": "/upload/",
    "health": "/upload/health",
    "status": "/upload/status",
    "test": "/upload/test",
    "diagnostics": "/upload/diagnostics"
  }
}
```

---

## 6. 독서 로그 API

### 6.1 독서 로그 생성

**POST** `/api/readinglogs`

**Request Body:**
```json
{
  "userId": 1,
  "readDate": "2024-01-15",
  "pagesRead": 10
}
```

**Response:**
```json
{
  "success": true,
  "message": "독서 기록이 저장되었습니다.",
  "logId": 1,
  "pagesRead": 10
}
```

### 6.2 기간별 독서 로그 조회

**GET** `/api/readinglogs/user/{userId}?startDate=2024-01-01&endDate=2024-01-31`

**Response:**
```json
{
  "success": true,
  "readingLogs": [
    {
      "logId": 1,
      "userId": 1,
      "readDate": "2024-01-15",
      "pagesRead": 10,
      "createdAt": "2024-01-15T14:30:00"
    }
  ],
  "startDate": "2024-01-01",
  "endDate": "2024-01-31"
}
```

### 6.3 오늘 독서 페이지 수 조회

**GET** `/api/readinglogs/user/{userId}/today`

**Response:**
```json
{
  "success": true,
  "pagesRead": 10,
  "date": "2024-01-15"
}
```

### 6.4 독서 통계 조회

**GET** `/api/readinglogs/user/{userId}/stats`

**Response:**
```json
{
  "success": true,
  "maxConsecutiveDays": 7,
  "totalReadingDays": 25
}
```

### 6.5 일일 독서 통계

**GET** `/api/readinglogs/user/{userId}/daily?startDate=2024-01-01&endDate=2024-01-31`

**Response:**
```json
{
  "success": true,
  "dailyStats": [
    {
      "date": "2024-01-15",
      "pagesRead": 10,
      "readingTime": 30
    }
  ],
  "startDate": "2024-01-01",
  "endDate": "2024-01-31"
}
```

---

## 7. 캘린더 API

### 7.1 특정 월 캘린더 조회

**GET** `/api/calendar/{userId}/{year}/{month}`

**Response:**
```json
{
  "success": true,
  "calendar": {
    "year": 2024,
    "month": 1,
    "totalReadingDays": 25,
    "totalPages": 150,
    "totalMinutes": 450,
    "maxConsecutiveDays": 7,
    "currentConsecutiveDays": 3,
    "summary": {
      "readingRate": 0.81,
      "averagePagesPerDay": 6.0,
      "averageMinutesPerDay": 18.0
    },
    "dailyData": [
      {
        "date": "2024-01-15",
        "pagesRead": 10,
        "readingTime": 30,
        "hasReading": true
      }
    ]
  },
  "message": "캘린더 데이터 조회 성공"
}
```

### 7.2 현재 월 캘린더 조회

**GET** `/api/calendar/{userId}/current`

**Response:**
```json
{
  "success": true,
  "calendar": {
    "year": 2024,
    "month": 1,
    "totalReadingDays": 25,
    "totalPages": 150,
    "totalMinutes": 450,
    "maxConsecutiveDays": 7,
    "currentConsecutiveDays": 3,
    "summary": {
      "readingRate": 0.81,
      "averagePagesPerDay": 6.0,
      "averageMinutesPerDay": 18.0
    },
    "dailyData": [
      {
        "date": "2024-01-15",
        "pagesRead": 10,
        "readingTime": 30,
        "hasReading": true
      }
    ]
  },
  "message": "캘린더 데이터 조회 성공"
}
```

### 7.3 특정 날짜 상세 조회

**GET** `/api/calendar/{userId}/day/{year}/{month}/{day}`

**Response:**
```json
{
  "success": true,
  "detail": {
    "date": "2024-01-15",
    "pagesRead": 10,
    "readingTime": 30,
    "booksRead": [
      {
        "bookId": 1,
        "title": "자바의 정석",
        "pagesRead": 10
      }
    ],
    "sessions": [
      {
        "sessionId": 1,
        "startTime": "2024-01-15T14:00:00",
        "endTime": "2024-01-15T14:30:00",
        "duration": 30,
        "pagesRead": 10
      }
    ]
  },
  "message": "일별 상세 정보 조회 성공"
}
```

### 7.4 오늘 상세 조회

**GET** `/api/calendar/{userId}/today`

**Response:**
```json
{
  "success": true,
  "detail": {
    "date": "2024-01-15",
    "pagesRead": 10,
    "readingTime": 30,
    "booksRead": [
      {
        "bookId": 1,
        "title": "자바의 정석",
        "pagesRead": 10
      }
    ],
    "sessions": [
      {
        "sessionId": 1,
        "startTime": "2024-01-15T14:00:00",
        "endTime": "2024-01-15T14:30:00",
        "duration": 30,
        "pagesRead": 10
      }
    ]
  },
  "message": "일별 상세 정보 조회 성공"
}
```

### 7.5 캘린더 통계 요약

**GET** `/api/calendar/{userId}/stats`

**Response:**
```json
{
  "success": true,
  "currentMonth": {
    "totalReadingDays": 25,
    "totalPages": 150,
    "totalMinutes": 450,
    "maxConsecutiveDays": 7,
    "currentConsecutiveDays": 3,
    "readingRate": 0.81
  },
  "lastMonth": {
    "totalReadingDays": 20,
    "totalPages": 120,
    "totalMinutes": 360
  },
  "message": "캘린더 통계 조회 성공"
}
```

---

## 8. 마이페이지 API

### 8.1 사용자 통계 조회

**GET** `/api/mypage/user/{userId}/stats`

**Response:**
```json
{
  "success": true,
  "stats": {
    "totalBooks": 10,
    "readBooks": 5,
    "readingBooks": 2,
    "wantToReadBooks": 3,
    "totalPages": 1500,
    "totalReadingDays": 100,
    "maxConsecutiveDays": 15,
    "currentConsecutiveDays": 5,
    "averagePagesPerDay": 15.0,
    "favoriteGenres": ["소설", "자기계발"]
  }
}
```

### 8.2 즐겨찾기 페이지 조회

**GET** `/api/mypage/user/{userId}/favorite-pages`

**Response:**
```json
{
  "success": true,
  "favoritePages": [
    {
      "favPageId": 1,
      "userId": 1,
      "bookId": 1,
      "pageNumber": 45,
      "bookTitle": "자바의 정석",
      "createdAt": "2024-01-15T14:30:00"
    }
  ]
}
```

### 8.3 즐겨찾기 문장 조회

**GET** `/api/mypage/user/{userId}/favorite-quotes`

**Response:**
```json
{
  "success": true,
  "favoriteQuotes": [
    {
      "favQuoteId": 1,
      "userId": 1,
      "bookId": 1,
      "pageNumber": 45,
      "content": "프로그래밍은 예술이다.",
      "bookTitle": "자바의 정석",
      "createdAt": "2024-01-15T14:30:00"
    }
  ]
}
```

### 8.4 즐겨찾기 페이지 추가

**POST** `/api/mypage/user/{userId}/favorite-page`

**Request (multipart/form-data):**
- `bookId`: Long (필수)
- `pageNumber`: Integer (필수)

**Response:**
```json
{
  "success": true,
  "message": "즐겨찾기한 페이지가 저장되었습니다.",
  "favoritePageId": 1
}
```

### 8.5 즐겨찾기 문장 추가

**POST** `/api/mypage/user/{userId}/favorite-quote`

**Request (multipart/form-data):**
- `bookId`: Long (필수)
- `pageNumber`: Integer (선택)
- `content`: String (필수)

**Response:**
```json
{
  "success": true,
  "message": "즐겨찾기한 문장이 저장되었습니다.",
  "favoriteQuoteId": 1
}
```

---

## 9. 웹소켓 API

### 9.1 웹소켓 연결

**WebSocket** `ws://43.200.102.14:5000/ws`

**SockJS** `http://43.200.102.14:5000/ws/info`

### 9.2 웹소켓 상태 확인

**GET** `/api/esp32/ws/status`

**Response:**
```json
{
  "success": true,
  "activeSessions": 2,
  "websocketEnabled": true,
  "endpoint": "ws://43.200.102.14:5000/ws",
  "sockjsEndpoint": "http://43.200.102.14:5000/ws/info",
  "timestamp": 1705312200000
}
```

### 9.3 웹소켓 진단 정보

**GET** `/api/esp32/ws/diagnostics`

**Response:**
```json
{
  "success": true,
  "activeSessions": 2,
  "websocketEnabled": true,
  "serverInfo": {
    "serverAddress": "43.200.102.14",
    "serverPort": 5000,
    "protocol": "WebSocket",
    "supportedProtocols": ["ws", "wss", "sockjs"]
  },
  "endpoints": {
    "websocket": "ws://43.200.102.14:5000/ws",
    "sockjs": "http://43.200.102.14:5000/ws/info",
    "status": "http://43.200.102.14:5000/api/esp32/ws/status"
  },
  "timestamp": 1705312200000
}
```

### 9.4 웹소켓 테스트 메시지 전송

**POST** `/api/esp32/ws/test`

**Request Body:**
```json
{
  "message": "테스트 메시지",
  "data": {
    "type": "test",
    "value": 123
  }
}
```

**Response:**
```json
{
  "success": true,
  "message": "테스트 메시지가 2개의 활성 세션에 전송되었습니다.",
  "activeSessions": 2,
  "timestamp": 1705312200000
}
```

### 9.5 웹소켓 메시지 (간단한 형식)

**자동 전송** (이미지 업로드 완료 시)

**메시지 형식:**
```json
{
  "pageNumber": 45,
  "date": "2024-01-15\n",
  "ocrText": "페이지 45 인식 완료\n"
}
```

**설명:**
- `pageNumber`: 페이지 번호 (Integer)
- `date`: 날짜 (String, YYYY-MM-DD 형식 + 개행문자)
- `ocrText`: OCR 인식 텍스트 (String + 개행문자)

---

## 10. 공통 응답 형식

### 10.1 성공 응답

```json
{
  "success": true,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": { ... },
  "count": 1
}
```

### 10.2 에러 응답

```json
{
  "success": false,
  "message": "에러 메시지",
  "data": null,
  "count": null
}
```

### 10.3 ApiResponse 형식 (일부 API)

```json
{
  "success": true,
  "message": "요청이 성공적으로 처리되었습니다.",
  "data": { ... },
  "count": 1,
  "timestamp": "2024-01-15T14:30:00"
}
```

---

## 11. 에러 코드

### 11.1 HTTP 상태 코드

- **200 OK**: 요청 성공
- **400 Bad Request**: 잘못된 요청
- **404 Not Found**: 리소스를 찾을 수 없음
- **500 Internal Server Error**: 서버 내부 오류

### 11.2 일반적인 에러 메시지

- `"이미지가 비어있습니다."`
- `"이미지 크기가 너무 큽니다. (최대 10MB)"`
- `"올바른 이미지 형식이 아닙니다."`
- `"책 페이지로 인식되지 않습니다. 더 명확한 이미지를 촬영해주세요."`
- `"사용자를 찾을 수 없습니다."`
- `"책을 찾을 수 없습니다."`
- `"활성 독서 세션이 없습니다."`

### 11.3 데이터베이스 관련 에러

- `"Field 'username' doesn't have a default value"`
- `"Data truncated for column 'status'"`
- `"could not execute statement"`

---

## 12. ESP32 개발 가이드

### 12.1 ESP32 이미지 업로드 예시

```cpp
#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>

void uploadImage() {
    HTTPClient http;
    http.begin("http://43.200.102.14:5000/upload/");
    
    String postData = "------WebKitFormBoundary7MA4YWxkTrZu0gW\r\n";
    postData += "Content-Disposition: form-data; name=\"image\"; filename=\"image.jpg\"\r\n";
    postData += "Content-Type: image/jpeg\r\n\r\n";
    postData += imageData;
    postData += "\r\n------WebKitFormBoundary7MA4YWxkTrZu0gW--\r\n";
    
    http.addHeader("Content-Type", "multipart/form-data; boundary=----WebKitFormBoundary7MA4YWxkTrZu0gW");
    
    int httpResponseCode = http.POST(postData);
    
    if (httpResponseCode > 0) {
        String response = http.getString();
        Serial.println(response);
    }
    
    http.end();
}
```

### 12.2 ESP32 세션 관리 예시

```cpp
void startReadingSession() {
    HTTPClient http;
    http.begin("http://43.200.102.14:5000/esp32/session/start");
    
    int httpResponseCode = http.POST("");
    
    if (httpResponseCode > 0) {
        String response = http.getString();
        Serial.println("Session started: " + response);
    }
    
    http.end();
}

void endReadingSession() {
    HTTPClient http;
    http.begin("http://43.200.102.14:5000/esp32/session/end");
    
    int httpResponseCode = http.POST("");
    
    if (httpResponseCode > 0) {
        String response = http.getString();
        Serial.println("Session ended: " + response);
    }
    
    http.end();
}
```

---

## 13. 테스트 및 디버깅

### 13.1 서버 상태 확인

```bash
curl http://43.200.102.14:5000/upload/health
```

### 13.2 웹소켓 연결 테스트

```javascript
const ws = new WebSocket('ws://43.200.102.14:5000/ws');

ws.onopen = function() {
    console.log('WebSocket 연결됨');
};

ws.onmessage = function(event) {
    console.log('메시지 수신:', event.data);
};
```

### 13.3 이미지 업로드 테스트

```bash
curl -X POST \
  http://43.200.102.14:5000/upload/ \
  -F "image=@test_image.jpg"
```

---

## 14. 주의사항

1. **이미지 크기**: 최대 10MB까지 지원
2. **이미지 형식**: JPEG, PNG 등 일반적인 이미지 형식
3. **ESP32 기본값**: userId=1, bookId=1 사용
4. **CORS**: 모든 도메인에서 접근 가능
5. **데이터베이스**: MySQL 8.0, MEDIUMBLOB 사용
6. **타임존**: Asia/Seoul 기준
7. **SSL**: 현재 비활성화 상태

---

## 15. 버전 정보

- **API 버전**: 1.0
- **서버 버전**: ReadMark-0.0.1-SNAPSHOT
- **Spring Boot**: 3.x
- **Java**: 17+
- **MySQL**: 8.0
- **최종 업데이트**: 2024-01-15

---

**📞 문의사항이 있으시면 개발팀에 연락해주세요.**
