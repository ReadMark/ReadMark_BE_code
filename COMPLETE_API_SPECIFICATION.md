# 📚 ReadMark 완전한 API 명세서

## 🎯 개요
ReadMark 서버의 모든 API 엔드포인트와 JSON 형태를 정리한 완전한 명세서입니다.

## 🔗 기본 정보
- **Base URL**: `http://43.200.102.14:5000`
- **Content-Type**: `application/json` (대부분), `multipart/form-data` (이미지 업로드)
- **Response Format**: JSON

---

## 📸 이미지 업로드 API

### 1. 임베디드용 단순한 이미지 업로드 (ESP32)
```json
{
  "method": "POST",
  "url": "/upload/",
  "description": "임베디드 기기용 단순한 OCR API",
  "request": {
    "contentType": "multipart/form-data",
    "body": {
      "image": "File (required)"
    }
  },
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "pageNumber": 123
      }
    }
  }
}
```

### 2. 프론트엔드용 복잡한 이미지 업로드
```json
{
  "method": "POST",
  "url": "/api/image/upload",
  "description": "프론트엔드용 완전한 독서 관리 API",
  "request": {
    "contentType": "multipart/form-data",
    "body": {
      "userId": "Long (required)",
      "bookId": "Long (required)",
      "image": "File (required)",
      "captureTime": "String (optional, yyyy-MM-dd HH:mm:ss)"
    }
  },
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "책 페이지가 성공적으로 저장되었습니다.",
        "page": {
          "pageId": 1,
          "pageNumber": 123,
          "confidence": 0.95,
          "language": "ko",
          "numberCount": 150,
          "capturedAt": "2025-01-14T12:30:00"
        },
        "userId": 1,
        "bookId": 1
      }
    }
  }
}
```

---

## 👤 사용자 관리 API

### 1. 사용자 회원가입
```json
{
  "method": "POST",
  "url": "/api/users/join",
  "description": "사용자 회원가입",
  "request": {
    "contentType": "application/json",
    "body": {
      "name": "String (required)",
      "username": "String (required)",
      "email": "String (required)",
      "password": "String (required)"
    }
  },
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "회원가입 성공",
        "data": {
          "userId": 1,
          "name": "홍길동",
          "username": "hong123",
          "email": "hong@example.com"
        }
      }
    }
  }
}
```

### 2. 사용자 로그인
```json
{
  "method": "POST",
  "url": "/api/users/login",
  "description": "사용자 로그인",
  "request": {
    "contentType": "application/json",
    "body": {
      "username": "String (required)",
      "password": "String (required)"
    }
  },
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "로그인 성공",
        "data": {
          "userId": 1,
          "name": "홍길동",
          "username": "hong123",
          "email": "hong@example.com"
        }
      }
    }
  }
}
```

### 3. 모든 사용자 조회
```json
{
  "method": "GET",
  "url": "/api/users",
  "description": "모든 사용자 목록 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "사용자 목록 조회 성공",
        "data": [
          {
            "userId": 1,
            "name": "홍길동",
            "username": "hong123",
            "email": "hong@example.com"
          }
        ],
        "count": 1
      }
    }
  }
}
```

---

## 📚 책 관리 API

### 1. 책 등록
```json
{
  "method": "POST",
  "url": "/api/books",
  "description": "새 책 등록",
  "request": {
    "contentType": "application/json",
    "body": {
      "title": "String (required)",
      "author": "String (required)",
      "publisher": "String (required)",
      "coverImageUrl": "String (optional)"
    }
  },
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "책이 등록되었습니다.",
        "bookId": 1
      }
    }
  }
}
```

### 2. 책 검색
```json
{
  "method": "GET",
  "url": "/api/books/search?keyword={keyword}",
  "description": "책 제목/저자로 검색",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "books": [
          {
            "bookId": 1,
            "title": "자바의 정석",
            "author": "남궁성",
            "publisher": "도우출판"
          }
        ],
        "count": 1
      }
    }
  }
}
```

### 3. 책 상세 조회
```json
{
  "method": "GET",
  "url": "/api/books/{bookId}",
  "description": "특정 책 상세 정보 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "book": {
          "bookId": 1,
          "title": "자바의 정석",
          "author": "남궁성",
          "publisher": "도우출판",
          "coverImageUrl": "https://example.com/cover.jpg"
        },
        "message": "책 정보 조회 성공"
      }
    }
  }
}
```

---

## 📖 독서 세션 관리 API

### 1. 독서 세션 시작 (프론트엔드용)
```json
{
  "method": "POST",
  "url": "/api/image/session/start",
  "description": "독서 세션 시작",
  "request": {
    "contentType": "application/x-www-form-urlencoded",
    "body": {
      "userId": "Long (required)",
      "bookId": "Long (required)"
    }
  },
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "독서 세션이 시작되었습니다.",
        "sessionId": 1,
        "startTime": "2025-01-14T12:00:00"
      }
    }
  }
}
```

### 2. 독서 세션 종료 (프론트엔드용)
```json
{
  "method": "POST",
  "url": "/api/image/session/end",
  "description": "독서 세션 종료",
  "request": {
    "contentType": "application/x-www-form-urlencoded",
    "body": {
      "userId": "Long (required)"
    }
  },
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "독서 세션이 종료되었습니다.",
        "totalPagesRead": 5,
        "totalNumbersRead": 750,
        "readingDurationMinutes": 30,
        "endTime": "2025-01-14T12:30:00"
      }
    }
  }
}
```

### 3. ESP32용 독서 세션 시작
```json
{
  "method": "POST",
  "url": "/esp32/session/start",
  "description": "ESP32용 독서 세션 시작 (기본값: userId=1, bookId=1)",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "독서 세션이 시작되었습니다.",
        "sessionId": 1,
        "startTime": "2025-01-14T12:00:00",
        "userId": 1,
        "bookId": 1,
        "date": "2025-01-14",
        "readingPeriod": "1일",
        "currentConsecutiveDays": 1
      }
    }
  }
}
```

### 4. ESP32용 독서 세션 종료
```json
{
  "method": "POST",
  "url": "/esp32/session/end",
  "description": "ESP32용 독서 세션 종료 (기본값: userId=1)",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "독서 세션이 종료되었습니다.",
        "totalPagesRead": 5,
        "totalNumbersRead": 750,
        "readingDurationMinutes": 30,
        "endTime": "2025-01-14T12:30:00",
        "userId": 1,
        "date": "2025-01-14",
        "readingPeriod": "1일",
        "currentConsecutiveDays": 1
      }
    }
  }
}
```

---

## 📊 독서 통계 API

### 1. 독서 통계 조회 (프론트엔드용)
```json
{
  "method": "GET",
  "url": "/api/image/stats/{userId}",
  "description": "사용자 독서 통계 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "stats": {
          "maxConsecutiveDays": 7,
          "totalReadingDays": 15,
          "currentConsecutiveDays": 3,
          "habitAnalysis": {
            "averagePagesPerDay": 5.2,
            "preferredReadingTime": "오후 2-4시",
            "readingConsistency": "보통"
          }
        }
      }
    }
  }
}
```

### 2. 월별 독서 통계 조회
```json
{
  "method": "GET",
  "url": "/api/image/stats/{userId}/monthly?months=6",
  "description": "월별 독서 통계 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "monthlyStats": {
          "2025-01": {
            "totalReadingDays": 15,
            "totalPages": 75,
            "totalMinutes": 450
          }
        }
      }
    }
  }
}
```

### 3. ESP32용 간단한 통계 조회
```json
{
  "method": "GET",
  "url": "/esp32/stats",
  "description": "ESP32용 간단한 통계 조회 (기본값: userId=1)",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "stats": {
          "maxConsecutiveDays": 7,
          "totalReadingDays": 15,
          "currentConsecutiveDays": 3
        },
        "userId": 1
      }
    }
  }
}
```

---

## 📅 캘린더 API

### 1. 특정 월 캘린더 조회
```json
{
  "method": "GET",
  "url": "/api/calendar/{userId}/{year}/{month}",
  "description": "특정 월의 캘린더 데이터 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "calendar": {
          "year": 2025,
          "month": 1,
          "totalReadingDays": 15,
          "totalPages": 75,
          "totalMinutes": 450,
          "maxConsecutiveDays": 5,
          "currentConsecutiveDays": 3,
          "summary": {
            "readingRate": 0.48
          }
        },
        "message": "캘린더 데이터 조회 성공"
      }
    }
  }
}
```

### 2. 현재 월 캘린더 조회
```json
{
  "method": "GET",
  "url": "/api/calendar/{userId}/current",
  "description": "현재 월의 캘린더 데이터 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "calendar": {
          "year": 2025,
          "month": 1,
          "totalReadingDays": 15,
          "totalPages": 75,
          "totalMinutes": 450
        }
      }
    }
  }
}
```

### 3. 특정 날짜 상세 조회
```json
{
  "method": "GET",
  "url": "/api/calendar/{userId}/day/{year}/{month}/{day}",
  "description": "특정 날짜의 상세 독서 정보 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "detail": {
          "date": "2025-01-14",
          "pagesRead": 5,
          "readingTime": 30,
          "books": [
            {
              "bookId": 1,
              "title": "자바의 정석",
              "pagesRead": 5
            }
          ]
        },
        "message": "일별 상세 정보 조회 성공"
      }
    }
  }
}
```

### 4. 오늘 독서 정보 조회
```json
{
  "method": "GET",
  "url": "/api/calendar/{userId}/today",
  "description": "오늘의 독서 정보 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "detail": {
          "date": "2025-01-14",
          "pagesRead": 5,
          "readingTime": 30
        }
      }
    }
  }
}
```

### 5. 캘린더 통계 요약
```json
{
  "method": "GET",
  "url": "/api/calendar/{userId}/stats",
  "description": "캘린더 통계 요약 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "currentMonth": {
          "totalReadingDays": 15,
          "totalPages": 75,
          "totalMinutes": 450,
          "maxConsecutiveDays": 5,
          "currentConsecutiveDays": 3,
          "readingRate": 0.48
        },
        "lastMonth": {
          "totalReadingDays": 12,
          "totalPages": 60,
          "totalMinutes": 360
        },
        "message": "캘린더 통계 조회 성공"
      }
    }
  }
}
```

---

## 📝 독서 기록 API

### 1. 독서 기록 생성
```json
{
  "method": "POST",
  "url": "/api/readinglogs",
  "description": "독서 기록 생성",
  "request": {
    "contentType": "application/json",
    "body": {
      "userId": "Long (required)",
      "readDate": "String (required, yyyy-MM-dd)",
      "pagesRead": "Integer (required)"
    }
  },
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "독서 기록이 저장되었습니다.",
        "logId": 1,
        "pagesRead": 5
      }
    }
  }
}
```

### 2. 기간별 독서 기록 조회
```json
{
  "method": "GET",
  "url": "/api/readinglogs/user/{userId}?startDate=2025-01-01&endDate=2025-01-31",
  "description": "기간별 독서 기록 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "readingLogs": [
          {
            "logId": 1,
            "readDate": "2025-01-14",
            "pagesRead": 5
          }
        ],
        "startDate": "2025-01-01",
        "endDate": "2025-01-31"
      }
    }
  }
}
```

### 3. 오늘 독서 페이지 수 조회
```json
{
  "method": "GET",
  "url": "/api/readinglogs/user/{userId}/today",
  "description": "오늘 독서한 페이지 수 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "pagesRead": 5,
        "date": "2025-01-14"
      }
    }
  }
}
```

### 4. 독서 통계 조회
```json
{
  "method": "GET",
  "url": "/api/readinglogs/user/{userId}/stats",
  "description": "독서 통계 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "maxConsecutiveDays": 7,
        "totalReadingDays": 15
      }
    }
  }
}
```

### 5. 일일 독서 통계 조회
```json
{
  "method": "GET",
  "url": "/api/readinglogs/user/{userId}/daily?startDate=2025-01-01&endDate=2025-01-31",
  "description": "일일 독서 통계 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "dailyStats": [
          {
            "date": "2025-01-14",
            "pagesRead": 5,
            "readingTime": 30
          }
        ],
        "startDate": "2025-01-01",
        "endDate": "2025-01-31"
      }
    }
  }
}
```

---

## 📚 사용자-책 관계 API

### 1. 사용자에게 책 추가
```json
{
  "method": "POST",
  "url": "/api/userbooks",
  "description": "사용자에게 책 추가",
  "request": {
    "contentType": "application/json",
    "body": {
      "userId": "Long (required)",
      "bookId": "Long (required)",
      "status": "String (required, READING/COMPLETED/PLANNED)",
      "currentPage": "Integer (optional, default: 0)"
    }
  },
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "책이 추가되었습니다.",
        "data": {
          "userBookId": 1,
          "userId": 1,
          "bookId": 1,
          "status": "READING",
          "currentPage": 0
        }
      }
    }
  }
}
```

### 2. 사용자 책 목록 조회
```json
{
  "method": "GET",
  "url": "/api/userbooks/user/{userId}",
  "description": "사용자의 책 목록 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "책 목록 조회 성공",
        "data": [
          {
            "userBookId": 1,
            "bookId": 1,
            "title": "자바의 정석",
            "author": "남궁성",
            "status": "READING",
            "currentPage": 50
          }
        ]
      }
    }
  }
}
```

### 3. 상태별 책 목록 조회
```json
{
  "method": "GET",
  "url": "/api/userbooks/user/{userId}/status/{status}",
  "description": "상태별 책 목록 조회 (READING/COMPLETED/PLANNED)",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "책 목록 조회 성공",
        "data": [
          {
            "userBookId": 1,
            "bookId": 1,
            "title": "자바의 정석",
            "author": "남궁성",
            "status": "READING",
            "currentPage": 50
          }
        ]
      }
    }
  }
}
```

### 4. 책 상태 업데이트
```json
{
  "method": "PUT",
  "url": "/api/userbooks/{userBookId}/status",
  "description": "책 상태 업데이트",
  "request": {
    "contentType": "application/json",
    "body": {
      "status": "String (required, READING/COMPLETED/PLANNED)"
    }
  },
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "책 상태가 업데이트되었습니다.",
        "data": {
          "userBookId": 1,
          "status": "COMPLETED"
        }
      }
    }
  }
}
```

### 5. 현재 페이지 업데이트
```json
{
  "method": "PUT",
  "url": "/api/userbooks/{userBookId}/page",
  "description": "현재 페이지 업데이트",
  "request": {
    "contentType": "application/json",
    "body": {
      "currentPage": "Integer (required)"
    }
  },
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "현재 페이지가 업데이트되었습니다.",
        "data": {
          "userBookId": 1,
          "currentPage": 100
        }
      }
    }
  }
}
```

---

## 👤 마이페이지 API

### 1. 사용자 통계 조회
```json
{
  "method": "GET",
  "url": "/api/mypage/user/{userId}/stats",
  "description": "사용자 통계 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "stats": {
          "totalBooks": 5,
          "completedBooks": 2,
          "totalPages": 500,
          "totalReadingDays": 30,
          "maxConsecutiveDays": 7
        }
      }
    }
  }
}
```

### 2. 즐겨찾기 페이지 조회
```json
{
  "method": "GET",
  "url": "/api/mypage/user/{userId}/favorite-pages",
  "description": "즐겨찾기한 페이지 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "favoritePages": [
          {
            "favPageId": 1,
            "bookId": 1,
            "bookTitle": "자바의 정석",
            "pageNumber": 50,
            "createdAt": "2025-01-14T12:00:00"
          }
        ]
      }
    }
  }
}
```

### 3. 즐겨찾기 문장 조회
```json
{
  "method": "GET",
  "url": "/api/mypage/user/{userId}/favorite-quotes",
  "description": "즐겨찾기한 문장 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "favoriteQuotes": [
          {
            "favQuoteId": 1,
            "bookId": 1,
            "bookTitle": "자바의 정석",
            "pageNumber": 50,
            "content": "자바는 객체지향 프로그래밍 언어입니다.",
            "createdAt": "2025-01-14T12:00:00"
          }
        ]
      }
    }
  }
}
```

### 4. 즐겨찾기 페이지 추가
```json
{
  "method": "POST",
  "url": "/api/mypage/user/{userId}/favorite-page",
  "description": "즐겨찾기 페이지 추가",
  "request": {
    "contentType": "application/x-www-form-urlencoded",
    "body": {
      "bookId": "Long (required)",
      "pageNumber": "Integer (required)"
    }
  },
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "즐겨찾기한 페이지가 저장되었습니다.",
        "favoritePageId": 1
      }
    }
  }
}
```

### 5. 즐겨찾기 문장 추가
```json
{
  "method": "POST",
  "url": "/api/mypage/user/{userId}/favorite-quote",
  "description": "즐겨찾기 문장 추가",
  "request": {
    "contentType": "application/x-www-form-urlencoded",
    "body": {
      "bookId": "Long (required)",
      "pageNumber": "Integer (optional)",
      "content": "String (required)"
    }
  },
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "즐겨찾기한 문장이 저장되었습니다.",
        "favoriteQuoteId": 1
      }
    }
  }
}
```

---

## 🔌 ESP32 상태 및 진단 API

### 1. ESP32 헬스체크
```json
{
  "method": "GET",
  "url": "/upload/health",
  "description": "ESP32 서비스 헬스체크",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "status": "OK",
        "message": "ESP32 Upload Service is running",
        "timestamp": "2025-01-14T12:00:00",
        "status": "REST API 모드"
      }
    }
  }
}
```

### 2. ESP32 상태 확인
```json
{
  "method": "GET",
  "url": "/upload/status",
  "description": "ESP32 상태 확인",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "status": "OK",
        "mode": "REST API",
        "timestamp": "2025-01-14T12:00:00",
        "serverPort": "5000",
        "serverAddress": "0.0.0.0"
      }
    }
  }
}
```

### 3. ESP32 테스트 메시지
```json
{
  "method": "POST",
  "url": "/upload/test",
  "description": "ESP32 테스트 메시지 전송",
  "request": {
    "contentType": "application/x-www-form-urlencoded",
    "body": {
      "message": "String (required)"
    }
  },
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "테스트 메시지 수신: Hello ESP32",
        "timestamp": "2025-01-14T12:00:00",
        "mode": "REST API"
      }
    }
  }
}
```

### 4. ESP32 진단 정보
```json
{
  "method": "GET",
  "url": "/upload/diagnostics",
  "description": "ESP32 진단 정보 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "serverPort": "5000",
        "serverAddress": "0.0.0.0",
        "mode": "REST API",
        "corsEnabled": true,
        "timestamp": "2025-01-14T12:00:00",
        "endpoints": {
          "upload": "/upload/",
          "health": "/upload/health",
          "status": "/upload/status",
          "test": "/upload/test",
          "diagnostics": "/upload/diagnostics"
        }
      }
    }
  }
}
```

---

## 🌐 웹소켓 API

### 1. 웹소켓 상태 확인
```json
{
  "method": "GET",
  "url": "/api/esp32/ws/status",
  "description": "웹소켓 연결 상태 확인",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "activeSessions": 2,
        "websocketEnabled": true,
        "endpoint": "ws://43.200.102.14:5000/ws",
        "sockjsEndpoint": "http://43.200.102.14:5000/ws/info",
        "timestamp": 1705123200000
      }
    }
  }
}
```

### 2. 웹소켓 진단 정보
```json
{
  "method": "GET",
  "url": "/api/esp32/ws/diagnostics",
  "description": "웹소켓 진단 정보 조회",
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
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
        "timestamp": 1705123200000
      }
    }
  }
}
```

### 3. 웹소켓 테스트 메시지 전송
```json
{
  "method": "POST",
  "url": "/api/esp32/ws/test",
  "description": "웹소켓 테스트 메시지 전송",
  "request": {
    "contentType": "application/json",
    "body": {
      "message": "String (optional)",
      "data": "Object (optional)"
    }
  },
  "response": {
    "success": {
      "statusCode": 200,
      "body": {
        "success": true,
        "message": "테스트 메시지가 2개의 활성 세션에 전송되었습니다.",
        "activeSessions": 2,
        "timestamp": 1705123200000
      }
    }
  }
}
```

---

## 📋 공통 오류 응답

### 400 Bad Request
```json
{
  "success": false,
  "message": "요청 파라미터가 올바르지 않습니다."
}
```

### 404 Not Found
```json
{
  "success": false,
  "message": "요청한 리소스를 찾을 수 없습니다."
}
```

### 500 Internal Server Error
```json
{
  "success": false,
  "message": "서버 내부 오류가 발생했습니다."
}
```

---

## 📝 사용 예시

### cURL 예시
```bash
# 이미지 업로드 (임베디드)
curl -X POST http://43.200.102.14:5000/upload/ \
  -F "image=@test_image.jpg"

# 이미지 업로드 (프론트엔드)
curl -X POST http://43.200.102.14:5000/api/image/upload \
  -F "userId=1" \
  -F "bookId=1" \
  -F "image=@test_image.jpg"

# 사용자 회원가입
curl -X POST http://43.200.102.14:5000/api/users/join \
  -H "Content-Type: application/json" \
  -d '{"name":"홍길동","username":"hong123","email":"hong@example.com","password":"password123"}'

# 책 등록
curl -X POST http://43.200.102.14:5000/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"자바의 정석","author":"남궁성","publisher":"도우출판"}'
```

---

**문서 버전**: 1.0  
**최종 업데이트**: 2025-01-14  
**총 API 엔드포인트**: 35개
