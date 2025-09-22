# ReadMark 실제 API 엔드포인트 가이드

## 🚀 **실제 구현된 API 엔드포인트들**

### **1. 이미지 업로드**
```bash
# 기본 이미지 업로드
curl -X POST http://43.200.102.14:5000/api/image/upload \
  -F "userId=1" \
  -F "bookId=1" \
  -F "image=@test_image.jpg" \
  -F "deviceInfo=Test-Device" \
  -F "captureTime=2025-01-14 12:30:00"

# ESP32 이미지 업로드
curl -X POST http://43.200.102.14:5000/api/esp32/upload \
  -F "image=@test_image.jpg"
```

### **2. 사용자 관리**
```bash
# 사용자 등록 (JOIN)
curl -X POST http://43.200.102.14:5000/api/users/join \
  -H "Content-Type: application/json" \
  -d '{
    "name": "홍길동",
    "username": "hong123",
    "email": "hong@example.com",
    "password": "password123"
  }'

# 사용자 로그인
curl -X POST http://43.200.102.14:5000/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'

# 모든 사용자 조회
curl -X GET http://43.200.102.14:5000/api/users

# 특정 사용자 조회
curl -X GET http://43.200.102.14:5000/api/users/1
```

### **3. 책 관리**
```bash
# 새 책 추가
curl -X POST http://43.200.102.14:5000/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "새로운 책",
    "author": "새로운 저자",
    "publisher": "새로운 출판사",
    "coverImageUrl": "https://example.com/cover.jpg"
  }'

# 책 검색
curl -X GET "http://43.200.102.14:5000/api/books/search?keyword=자바"

# 특정 책 조회
curl -X GET http://43.200.102.14:5000/api/books/1
```

### **4. 사용자-책 관계 관리**
```bash
# 사용자에게 책 추가
curl -X POST http://43.200.102.14:5000/api/user-books \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "bookId": 1,
    "status": "NOW_READ",
    "currentPage": 0
  }'

# 사용자의 책 목록 조회
curl -X GET http://43.200.102.14:5000/api/user-books/user/1

# 특정 상태의 책 조회
curl -X GET http://43.200.102.14:5000/api/user-books/user/1/status/NOW_READ

# 책 상태 업데이트
curl -X PUT http://43.200.102.14:5000/api/user-books/1/status \
  -H "Content-Type: application/json" \
  -d '{
    "status": "READ_DONE"
  }'

# 현재 페이지 업데이트
curl -X PUT http://43.200.102.14:5000/api/user-books/1/page \
  -H "Content-Type: application/json" \
  -d '{
    "currentPage": 50
  }'
```

### **5. 독서 세션 관리**
```bash
# ESP32 독서 세션 시작
curl -X POST http://43.200.102.14:5000/api/esp32/session/start

# ESP32 독서 세션 종료
curl -X POST http://43.200.102.14:5000/api/esp32/session/end

# ESP32 통계 조회
curl -X GET http://43.200.102.14:5000/api/esp32/stats

# 일반 독서 세션 시작
curl -X POST http://43.200.102.14:5000/api/image/session/start \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "bookId": 1
  }'

# 일반 독서 세션 종료
curl -X POST http://43.200.102.14:5000/api/image/session/end?userId=1
```

### **6. 독서 로그 관리**
```bash
# 독서 로그 생성
curl -X POST http://43.200.102.14:5000/api/reading-logs \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "readDate": "2025-01-14",
    "pagesRead": 10,
    "readingDurationMinutes": 30
  }'

# 사용자의 독서 로그 조회 (날짜 범위)
curl -X GET "http://43.200.102.14:5000/api/reading-logs/user/1?startDate=2025-01-01&endDate=2025-01-31"

# 오늘 읽은 페이지 수
curl -X GET http://43.200.102.14:5000/api/reading-logs/user/1/today

# 독서 통계 조회
curl -X GET http://43.200.102.14:5000/api/reading-logs/user/1/stats

# 일별 독서 통계
curl -X GET "http://43.200.102.14:5000/api/reading-logs/user/1/daily?startDate=2025-01-01&endDate=2025-01-31"
```

### **7. 책 페이지 관리**
```bash
# 사용자의 책 페이지 목록 조회
curl -X GET http://43.200.102.14:5000/api/image/pages/1/1

# 특정 페이지 조회
curl -X GET http://43.200.102.14:5000/api/image/pages/1/1/1

# 페이지 범위 조회
curl -X GET "http://43.200.102.14:5000/api/image/pages/1/1/range?startPage=1&endPage=10"

# 최근 페이지 조회
curl -X GET "http://43.200.102.14:5000/api/image/pages/1/1/recent?limit=5"

# 페이지 삭제
curl -X DELETE http://43.200.102.14:5000/api/image/pages/1/1/1
```

### **8. 독서 통계**
```bash
# 사용자 독서 통계
curl -X GET http://43.200.102.14:5000/api/image/stats/1

# 월별 독서 통계
curl -X GET "http://43.200.102.14:5000/api/image/stats/1/monthly?year=2025&month=1"
```

### **9. 캘린더 기능**
```bash
# 특정 월 캘린더 조회
curl -X GET http://43.200.102.14:5000/api/calendar/1/2025/1

# 현재 월 캘린더 조회
curl -X GET http://43.200.102.14:5000/api/calendar/1/current

# 특정 일 상세 조회
curl -X GET http://43.200.102.14:5000/api/calendar/1/day/2025/1/14

# 오늘 상세 조회
curl -X GET http://43.200.102.14:5000/api/calendar/1/today

# 캘린더 통계
curl -X GET http://43.200.102.14:5000/api/calendar/1/stats
```

### **10. 마이페이지 기능**
```bash
# 사용자 통계
curl -X GET http://43.200.102.14:5000/api/mypage/user/1/stats

# 즐겨찾기 페이지 목록
curl -X GET http://43.200.102.14:5000/api/mypage/user/1/favorite-pages

# 즐겨찾기 인용구 목록
curl -X GET http://43.200.102.14:5000/api/mypage/user/1/favorite-quotes

# 즐겨찾기 페이지 추가
curl -X POST http://43.200.102.14:5000/api/mypage/user/1/favorite-page \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": 1,
    "pageNumber": 10
  }'

# 즐겨찾기 인용구 추가
curl -X POST http://43.200.102.14:5000/api/mypage/user/1/favorite-quote \
  -H "Content-Type: application/json" \
  -d '{
    "bookId": 1,
    "pageNumber": 10,
    "content": "인상적인 문장입니다."
  }'
```

### **11. ESP32 진단 기능**
```bash
# ESP32 헬스 체크
curl -X GET http://43.200.102.14:5000/api/esp32/health

# ESP32 상태 조회
curl -X GET http://43.200.102.14:5000/api/esp32/status

# ESP32 테스트 메시지 전송
curl -X POST http://43.200.102.14:5000/api/esp32/test?message=Hello

# ESP32 진단 정보
curl -X GET http://43.200.102.14:5000/api/esp32/diagnostics
```

## 🧪 **테스트 시나리오**

### **완전한 독서 플로우 테스트:**
```bash
# 1. 사용자 등록
curl -X POST http://43.200.102.14:5000/api/users/join \
  -H "Content-Type: application/json" \
  -d '{"name":"테스트사용자","username":"testuser","email":"test@example.com","password":"password123"}'

# 2. 책 추가
curl -X POST http://43.200.102.14:5000/api/books \
  -H "Content-Type: application/json" \
  -d '{"title":"테스트책","author":"테스트저자","publisher":"테스트출판사"}'

# 3. 사용자에게 책 추가
curl -X POST http://43.200.102.14:5000/api/user-books \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"bookId":1,"status":"NOW_READ","currentPage":0}'

# 4. 독서 세션 시작
curl -X POST http://43.200.102.14:5000/api/image/session/start \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"bookId":1}'

# 5. 이미지 업로드
curl -X POST http://43.200.102.14:5000/api/image/upload \
  -F "userId=1" \
  -F "bookId=1" \
  -F "image=@test_image.jpg" \
  -F "deviceInfo=Test-Device"

# 6. 독서 세션 종료
curl -X POST http://43.200.102.14:5000/api/image/session/end?userId=1

# 7. 통계 조회
curl -X GET http://43.200.102.14:5000/api/image/stats/1
```

## 📝 **주요 변경사항**

- ❌ `/api/users/register` → ✅ `/api/users/join`
- ❌ `/api/books?userId=1` → ✅ `/api/user-books/user/1`
- ❌ `/api/reading-sessions/start` → ✅ `/api/image/session/start` 또는 `/api/esp32/session/start`
- ❌ `/api/reading-stats` → ✅ `/api/image/stats/{userId}` 또는 `/api/reading-logs/user/{userId}/stats`
