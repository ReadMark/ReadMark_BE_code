# ReadMark API 테스트 예제

## 🚀 **이미지 업로드 테스트**

### **1. 기본 이미지 업로드 (ImageUploadController)**
```bash
curl -X POST http://43.200.102.14:5000/api/image/upload \
  -F "userId=1" \
  -F "bookId=1" \
  -F "image=@test_image.jpg" \
  -F "deviceInfo=ESP32-CAM" \
  -F "captureTime=2025-01-14 12:30:00"
```

**예상 응답:**
```json
{
  "success": true,
  "message": "책 페이지가 성공적으로 저장되었습니다.",
  "page": {
    "pageId": 1,
    "pageNumber": 1,
    "confidence": 0.95,
    "language": "ko",
    "numberCount": 150,
    "capturedAt": "2025-01-14T12:30:00"
  },
  "deviceInfo": "ESP32-CAM"
}
```

### **2. ESP32 이미지 업로드 (ESP32UploadController)**
```bash
curl -X POST http://43.200.102.14:5000/api/esp32/upload \
  -F "image=@test_image.jpg"
```

**예상 응답:**
```json
{
  "success": true,
  "message": "이미지 업로드 성공",
  "pageId": 1,
  "pageNumber": 1,
  "confidence": 0.95,
  "deviceInfo": "ESP32-CAM",
  "capturedAt": "2025-01-14T12:30:00",
  "date": "2025-01-14",
  "readingPeriod": "1일",
  "currentConsecutiveDays": 1
}
```

## 📚 **책 관리 API 테스트**

### **3. 책 목록 조회**
```bash
curl -X GET "http://43.200.102.14:5000/api/books?userId=1"
```

**예상 응답:**
```json
{
  "success": true,
  "books": [
    {
      "bookId": 1,
      "title": "테스트 책",
      "author": "테스트 저자",
      "publisher": "테스트 출판사",
      "coverImageUrl": null,
      "publishedAt": null,
      "createdAt": "2025-01-14T12:00:00"
    }
  ]
}
```

### **4. 새 책 추가**
```bash
curl -X POST http://43.200.102.14:5000/api/books \
  -H "Content-Type: application/json" \
  -d '{
    "title": "새로운 책",
    "author": "새로운 저자",
    "publisher": "새로운 출판사",
    "coverImageUrl": "https://example.com/cover.jpg"
  }'
```

**예상 응답:**
```json
{
  "success": true,
  "message": "책이 성공적으로 추가되었습니다.",
  "book": {
    "bookId": 2,
    "title": "새로운 책",
    "author": "새로운 저자",
    "publisher": "새로운 출판사",
    "coverImageUrl": "https://example.com/cover.jpg",
    "createdAt": "2025-01-14T12:30:00"
  }
}
```

## 👤 **사용자 관리 API 테스트**

### **5. 사용자 등록 (JOIN)**
```bash
curl -X POST http://43.200.102.14:5000/api/users/join \
  -H "Content-Type: application/json" \
  -d '{
    "name": "홍길동",
    "username": "hong123",
    "email": "hong@example.com",
    "password": "password123"
  }'
```

**예상 응답:**
```json
{
  "success": true,
  "message": "사용자 등록 성공",
  "data": {
    "userId": 2,
    "name": "홍길동",
    "username": "hong123",
    "email": "hong@example.com",
    "createdAt": "2025-01-14T12:30:00"
  }
}
```

### **6. 사용자 로그인**
```bash
curl -X POST http://43.200.102.14:5000/api/users/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "testuser",
    "password": "password123"
  }'
```

## 📖 **독서 세션 API 테스트**

### **7. 독서 세션 시작**
```bash
curl -X POST http://43.200.102.14:5000/api/reading-sessions/start \
  -H "Content-Type: application/json" \
  -d '{
    "userId": 1,
    "bookId": 1
  }'
```

**예상 응답:**
```json
{
  "success": true,
  "message": "독서 세션이 시작되었습니다.",
  "session": {
    "sessionId": 1,
    "userId": 1,
    "bookId": 1,
    "startTime": "2025-01-14T12:30:00",
    "totalPagesRead": 0,
    "totalNumbersRead": 0
  }
}
```

### **8. 독서 세션 종료**
```bash
curl -X POST http://43.200.102.14:5000/api/reading-sessions/1/end \
  -H "Content-Type: application/json" \
  -d '{
    "sessionNotes": "재미있는 책이었습니다."
  }'
```

## 📄 **책 페이지 API 테스트**

### **9. 책 페이지 목록 조회**
```bash
curl -X GET "http://43.200.102.14:5000/api/books/1/pages?userId=1"
```

**예상 응답:**
```json
{
  "success": true,
  "pages": [
    {
      "pageId": 1,
      "pageNumber": 1,
      "imageUrl": null,
      "capturedAt": "2025-01-14T12:30:00",
      "confidence": 0.95,
      "deviceInfo": "ESP32-CAM",
      "language": "ko",
      "numberCount": 150
    }
  ]
}
```

### **10. 특정 페이지 조회**
```bash
curl -X GET "http://43.200.102.14:5000/api/books/1/pages/1?userId=1"
```

## 📊 **독서 통계 API 테스트**

### **11. 독서 통계 조회**
```bash
curl -X GET "http://43.200.102.14:5000/api/reading-stats?userId=1"
```

**예상 응답:**
```json
{
  "success": true,
  "stats": {
    "totalPagesRead": 150,
    "totalBooksRead": 1,
    "totalReadingTime": 120,
    "currentStreak": 1,
    "longestStreak": 1
  }
}
```

## 🔧 **테스트용 이미지 파일 생성**

### **Windows (PowerShell):**
```powershell
# 테스트용 이미지 파일 생성 (1KB 더미 이미지)
$bytes = New-Object byte[] 1024
[System.IO.File]::WriteAllBytes("test_image.jpg", $bytes)
```

### **Linux/Mac:**
```bash
# 테스트용 이미지 파일 생성
dd if=/dev/zero of=test_image.jpg bs=1024 count=1
```

## 📝 **테스트 시나리오**

### **완전한 테스트 플로우:**
1. **사용자 등록** → 사용자 ID 획득
2. **책 추가** → 책 ID 획득
3. **독서 세션 시작** → 세션 ID 획득
4. **이미지 업로드** → 페이지 데이터 저장
5. **독서 세션 종료** → 세션 완료
6. **통계 조회** → 결과 확인

### **오류 테스트:**
```bash
# 잘못된 사용자 ID로 이미지 업로드
curl -X POST http://43.200.102.14:5000/api/image/upload \
  -F "userId=999" \
  -F "bookId=1" \
  -F "image=@test_image.jpg"

# 빈 이미지 업로드
curl -X POST http://43.200.102.14:5000/api/image/upload \
  -F "userId=1" \
  -F "bookId=1" \
  -F "image="
```

## 🌐 **API 엔드포인트 목록**

| 메서드 | 엔드포인트 | 설명 |
|--------|------------|------|
| POST | `/api/image/upload` | 이미지 업로드 |
| POST | `/api/esp32/upload` | ESP32 이미지 업로드 |
| GET | `/api/books` | 책 목록 조회 |
| POST | `/api/books` | 새 책 추가 |
| GET | `/api/books/{bookId}/pages` | 책 페이지 목록 |
| POST | `/api/users/register` | 사용자 등록 |
| POST | `/api/users/login` | 사용자 로그인 |
| POST | `/api/reading-sessions/start` | 독서 세션 시작 |
| POST | `/api/reading-sessions/{sessionId}/end` | 독서 세션 종료 |
| GET | `/api/reading-stats` | 독서 통계 조회 |

## 🔍 **디버깅 팁**

1. **로그 확인**: 애플리케이션 로그에서 오류 메시지 확인
2. **데이터베이스 확인**: MySQL에서 테이블 데이터 직접 확인
3. **네트워크 확인**: 방화벽 및 포트 설정 확인
4. **CORS 확인**: 브라우저에서 CORS 오류 확인
