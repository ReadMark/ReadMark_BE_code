# 🚀 ESP32-CAM과 ReadMark 백엔드 통합 가이드

## 📋 개요
이 문서는 ESP32-CAM 임베디드 기기와 ReadMark 백엔드 서버 간의 통합 방법을 설명합니다.

## 🔌 ESP32 전용 API 엔드포인트

### 1. 이미지 업로드
**POST** `/upload/`
- ESP32가 기존에 사용하던 `/upload/` 엔드포인트 지원
- 자동으로 `userId=1`, `bookId=1` 설정
- `image` 파라미터만 전송하면 됨

#### 요청 예시
```bash
curl -X POST "http://localhost:5000/upload/" \
  -F "image=@page_image.jpg"
```

#### 응답 예시
```json
{
  "success": true,
  "message": "이미지 업로드 성공",
  "pageId": 123,
  "pageNumber": 45,
  "extractedText": "추출된 텍스트...",
  "confidence": 0.95,
  "textQuality": 85.5,
  "deviceInfo": "ESP32-CAM",
  "capturedAt": "2024-01-15T14:30:00",
  "date": 3,
  "readingPeriod": "2024.01.13 - 2024.01.15",
  "currentConsecutiveDays": 3
}
```

#### 새로운 응답 필드 설명
- `date`: ESP32에서 사용할 간단한 값 (연속 독서일 수)
- `readingPeriod`: 독서 기간 문자열 (시작일 - 종료일)
- `currentConsecutiveDays`: 현재 연속 독서일 수

### 2. 독서 세션 시작
**POST** `/esp32/session/start`
- 자동으로 `userId=1`, `bookId=1` 설정
- 응답에 독서 기간 정보 포함

#### 응답 예시
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

### 3. 독서 세션 종료
**POST** `/esp32/session/end`
- 자동으로 `userId=1` 설정
- 응답에 독서 기간 정보 포함

#### 응답 예시
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

### 4. 통계 조회
**GET** `/esp32/stats`
- 자동으로 `userId=1` 설정

## 🚀 실행 방법

### 통합 모드로 실행 (포트 5000)
```bash
# 통합 모드로 실행 (ESP32 + 프론트엔드 모두 지원)
java -jar ReadMark.jar

# 또는 IDE에서 실행
```

### ESP32 전용 모드로 실행 (포트 5000)
```bash
# ESP32 전용 모드로 실행 (디버깅용)
java -jar ReadMark.jar esp32

# 또는 IDE에서 실행 시 VM 옵션에 추가
-Dspring.profiles.active=esp32
```

## ⚙️ ESP32 코드 수정 필요사항

### 1. 서버 URL 수정
```c
// 기존 (잘못된 URL)
char url[128] = "http://192.168.1.51:5000/upload/";

// 수정 (올바른 URL)
char url[128] = "http://192.168.1.51:5000/upload/";
```

### 2. 요청 파라미터 수정
```c
// 기존 (image만 전송)
"Content-Disposition: form-data; name=\"image\"; filename=\"esp32-cam.jpg\""

// 수정 (image만 전송하면 됨 - 서버에서 자동으로 userId, bookId 설정)
"Content-Disposition: form-data; name=\"image\"; filename=\"esp32-cam.jpg\""
```

### 3. 응답 파싱 수정
```c
// 기존 (잘못된 키)
cJSON *msg = cJSON_GetObjectItemCaseSensitive(root, "test_value");

// 수정 (올바른 키들)
cJSON *success = cJSON_GetObjectItemCaseSensitive(root, "success");
cJSON *message = cJSON_GetObjectItemCaseSensitive(root, "message");
cJSON *pageId = cJSON_GetObjectItemCaseSensitive(root, "pageId");
cJSON *pageNumber = cJSON_GetObjectItemCaseSensitive(root, "pageNumber");
cJSON *extractedText = cJSON_GetObjectItemCaseSensitive(root, "extractedText");
cJSON *confidence = cJSON_GetObjectItemCaseSensitive(root, "confidence");
cJSON *capturedAt = cJSON_GetObjectItemCaseSensitive(root, "capturedAt");

// 새로 추가된 날짜 정보
cJSON *date = cJSON_GetObjectItemCaseSensitive(root, "date");
cJSON *readingPeriod = cJSON_GetObjectItemCaseSensitive(root, "readingPeriod");
cJSON *consecutiveDays = cJSON_GetObjectItemCaseSensitive(root, "currentConsecutiveDays");

// ESP32에서 사용할 간단한 값
if (cJSON_IsNumber(date)) {
    int readingDays = date->valueint;
    // LED 깜빡임, 디스플레이 출력 등에 사용
    ESP_LOGI("ReadMark", "연속 독서일: %d일", readingDays);
}
```

## 🔧 설정 파일

### application-esp32.properties
```properties
# ESP32 전용 설정
server.port=5000
esp32.default.user-id=1
esp32.default.book-id=1
esp32.default.device-info=ESP32-CAM
```

## 📊 호환성 매트릭스

| 기능 | ESP32 코드 | ReadMark 서버 | 상태 |
|------|------------|---------------|------|
| 이미지 업로드 | `/upload/` | ✅ `/upload/` | 🟢 완벽 호환 |
| 세션 시작 | ❌ 없음 | ✅ `/esp32/session/start` | 🟡 ESP32 코드 추가 필요 |
| 세션 종료 | ❌ 없음 | ✅ `/esp32/session/end` | 🟡 ESP32 코드 추가 필요 |
| 통계 조회 | ❌ 없음 | ✅ `/esp32/stats` | 🟡 ESP32 코드 추가 필요 |

## 🎯 다음 단계

1. **ESP32 코드 수정**: 위의 수정사항 적용
2. **서버 실행**: `java -jar ReadMark.jar esp32` 명령으로 ESP32 모드 실행
3. **테스트**: ESP32에서 이미지 업로드 테스트
4. **기능 확장**: 세션 관리, 통계 조회 기능 추가

## 🚨 주의사항

- **통합 모드**: 포트 5000에서 ESP32와 프론트엔드 모두 지원
- **ESP32 전용 모드**: 포트 5000에서 ESP32만 지원 (디버깅용)
- ESP32는 기본적으로 `userId=1`, `bookId=1`을 사용합니다
- 프로덕션 환경에서는 보안 설정을 강화해야 합니다
- **중요**: 프론트엔드도 포트 5000으로 접속해야 합니다


