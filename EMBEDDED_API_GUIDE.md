# 📚 ReadMark 임베디드 기기 API 가이드

## 🎯 개요
이 문서는 임베디드 기기에서 ReadMark 시스템과 연동하여 책 페이지를 촬영하고 분석하는 방법을 설명합니다.

## 🔌 API 엔드포인트

### 1. 책 페이지 이미지 업로드
**POST** `/api/image/upload`

#### 요청 파라미터
- `userId` (필수): 사용자 ID
- `bookId` (필수): 책 ID  
- `image` (필수): 책 페이지 이미지 파일
- `deviceInfo` (선택): 기기 정보 (예: "RaspberryPi4", "ESP32-CAM")
- `captureTime` (선택): 촬영 시간 (yyyy-MM-dd HH:mm:ss 형식)

#### 요청 예시 (cURL)
```bash
curl -X POST "http://localhost:8080/api/image/upload" \
  -F "userId=1" \
  -F "bookId=1" \
  -F "image=@page_image.jpg" \
  -F "deviceInfo=RaspberryPi4" \
  -F "captureTime=2024-01-15 14:30:00"
```

#### 응답 예시
```json
{
  "success": true,
  "message": "책 페이지가 성공적으로 저장되었습니다.",
  "page": {
    "pageId": 123,
    "pageNumber": 45,
    "extractedText": "추출된 텍스트 내용...",
    "confidence": 0.95,
    "language": "ko",
    "wordCount": 156,
    "textQuality": 85.5,
    "qualityLevel": "우수",
    "confidenceLevel": "매우 높음",
    "capturedAt": "2024-01-15T14:30:00"
  },
  "deviceInfo": "RaspberryPi4"
}
```

### 2. 독서 세션 시작
**POST** `/api/image/session/start`

#### 요청 파라미터
- `userId` (필수): 사용자 ID
- `bookId` (필수): 책 ID

#### 응답 예시
```json
{
  "success": true,
  "message": "독서 세션이 시작되었습니다.",
  "sessionId": 456,
  "startTime": "2024-01-15T14:30:00"
}
```

### 3. 독서 세션 종료
**POST** `/api/image/session/end`

#### 요청 파라미터
- `userId` (필수): 사용자 ID

#### 응답 예시
```json
{
  "success": true,
  "message": "독서 세션이 종료되었습니다.",
  "totalPagesRead": 15,
  "totalWordsRead": 2340,
  "readingDurationMinutes": 45,
  "endTime": "2024-01-15T15:15:00"
}
```

### 4. 책 페이지 목록 조회
**GET** `/api/image/pages/{userId}/{bookId}`

#### 응답 예시
```json
{
  "success": true,
  "pages": [
    {
      "pageId": 123,
      "pageNumber": 45,
      "extractedText": "텍스트 내용...",
      "confidence": 0.95,
      "textQuality": 85.5,
      "qualityLevel": "우수"
    }
  ],
  "totalPages": 15,
  "count": 1
}
```

### 5. 특정 페이지 조회
**GET** `/api/image/pages/{userId}/{bookId}/{pageNumber}`

### 6. 페이지 범위 조회
**GET** `/api/image/pages/{userId}/{bookId}/range?startPage=1&endPage=10`

### 7. 최근 페이지 조회
**GET** `/api/image/pages/{userId}/{bookId}/recent?limit=10`

### 8. 페이지 삭제
**DELETE** `/api/image/pages/{userId}/{bookId}/{pageNumber}`

## 📱 임베디드 기기 구현 예시

### Python (Raspberry Pi)
```python
import requests
import time
from datetime import datetime
from picamera import PiCamera

class ReadMarkClient:
    def __init__(self, base_url, user_id, book_id):
        self.base_url = base_url
        self.user_id = user_id
        self.book_id = book_id
        self.camera = PiCamera()
        
    def capture_and_upload_page(self):
        # 이미지 촬영
        timestamp = datetime.now().strftime("%Y-%m-%d %H:%M:%S")
        image_path = f"/tmp/page_{timestamp}.jpg"
        
        self.camera.capture(image_path)
        
        # 서버로 업로드
        with open(image_path, 'rb') as image_file:
            files = {'image': image_file}
            data = {
                'userId': self.user_id,
                'bookId': self.book_id,
                'deviceInfo': 'RaspberryPi4',
                'captureTime': timestamp
            }
            
            response = requests.post(
                f"{self.base_url}/api/image/upload",
                files=files,
                data=data
            )
            
            if response.status_code == 200:
                result = response.json()
                if result['success']:
                    print(f"페이지 {result['page']['pageNumber']} 저장 완료!")
                    return result['page']
                else:
                    print(f"저장 실패: {result['message']}")
            else:
                print(f"HTTP 오류: {response.status_code}")
                
        return None

# 사용 예시
client = ReadMarkClient("http://localhost:8080", 1, 1)
client.capture_and_upload_page()
```

### Arduino (ESP32-CAM)
```cpp
#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include "esp_camera.h"

const char* ssid = "YOUR_WIFI_SSID";
const char* password = "YOUR_WIFI_PASSWORD";
const char* serverUrl = "http://localhost:8080";

void setup() {
  Serial.begin(115200);
  
  // WiFi 연결
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("WiFi 연결 중...");
  }
  Serial.println("WiFi 연결됨");
  
  // 카메라 초기화
  camera_config_t config;
  // 카메라 설정...
  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("카메라 초기화 실패: 0x%x", err);
    return;
  }
}

void captureAndUpload() {
  // 이미지 촬영
  camera_fb_t * fb = esp_camera_fb_get();
  if (!fb) {
    Serial.println("이미지 촬영 실패");
    return;
  }
  
  // HTTP 클라이언트 생성
  HTTPClient http;
  http.begin(serverUrl + "/api/image/upload");
  http.addHeader("Content-Type", "multipart/form-data");
  
  // 폼 데이터 구성
  String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
  String postData = "--" + boundary + "\r\n";
  postData += "Content-Disposition: form-data; name=\"userId\"\r\n\r\n";
  postData += "1\r\n";
  postData += "--" + boundary + "\r\n";
  postData += "Content-Disposition: form-data; name=\"bookId\"\r\n\r\n";
  postData += "1\r\n";
  postData += "--" + boundary + "\r\n";
  postData += "Content-Disposition: form-data; name=\"image\"; filename=\"page.jpg\"\r\n";
  postData += "Content-Type: image/jpeg\r\n\r\n";
  
  // 이미지 데이터 추가
  String endData = "\r\n--" + boundary + "--\r\n";
  
  int httpResponseCode = http.POST((uint8_t*)postData.c_str(), 
                                  postData.length() + fb->len + endData.length());
  
  if (httpResponseCode > 0) {
    String response = http.getString();
    Serial.println("응답: " + response);
  } else {
    Serial.printf("HTTP 오류: %d\n", httpResponseCode);
  }
  
  http.end();
  esp_camera_fb_return(fb);
}
```

## 🔧 설정 및 환경

### 1. Google Vision API 설정
- `GOOGLE_APPLICATION_CREDENTIALS` 환경변수 설정
- 서비스 계정 키 파일 경로 지정

### 2. 데이터베이스 설정
- MySQL/PostgreSQL 연결 설정
- 테이블 자동 생성 설정

### 3. 이미지 처리 설정
- 최대 이미지 크기: 10MB
- 지원 형식: JPEG, PNG, GIF
- 이미지 품질 임계값: 50점 이상

## 📊 모니터링 및 로그

### 로그 레벨
- INFO: 일반적인 작업 진행 상황
- WARN: 경고 상황 (중복 페이지, 낮은 품질 등)
- ERROR: 오류 상황

### 주요 메트릭
- 이미지 업로드 성공률
- 텍스트 추출 정확도
- 페이지 번호 인식 정확도
- API 응답 시간

## 🚀 성능 최적화 팁

1. **이미지 품질**: 명확하고 선명한 이미지 사용
2. **조명**: 적절한 조명으로 텍스트 가독성 향상
3. **각도**: 책과 수직으로 촬영
4. **해상도**: 최소 1280x720 권장
5. **네트워크**: 안정적인 WiFi 연결 사용

## 🔍 문제 해결

### 일반적인 오류
- **텍스트 추출 실패**: 이미지 품질 확인, 조명 개선
- **페이지 번호 인식 실패**: 페이지 번호가 명확히 보이는지 확인
- **네트워크 오류**: WiFi 연결 상태 및 서버 접근성 확인

### 디버깅 방법
1. 로그 확인
2. 이미지 품질 점수 확인
3. API 응답 상세 내용 분석
4. 네트워크 연결 상태 점검
