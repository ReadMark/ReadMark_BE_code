# 📚 ReadMark 임베디드 기기 API 가이드

## 🎯 개요
이 문서는 임베디드 기기에서 ReadMark 시스템과 연동하여 책 페이지를 촬영하고 분석하는 방법을 설명합니다.

## 🔌 API 엔드포인트

### 1. 책 페이지 이미지 업로드
**POST** `/api/image/upload`

#### 요청 파라미터
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `userId` | `number` | ✅ | 사용자 ID | `1` |
| `bookId` | `number` | ✅ | 책 ID | `1` |
| `image` | `file` | ✅ | 책 페이지 이미지 파일 | `page_image.jpg` |
| `deviceInfo` | `string` | ❌ | 기기 정보 | `"ESP32-CAM"` |
| `captureTime` | `string` | ❌ | 촬영 시간 (yyyy-MM-dd HH:mm:ss) | `"2024-01-15 14:30:00"` |

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

#### 응답 스키마
| 필드 | 타입 | 설명 |
|------|------|------|
| `success` | `boolean` | 요청 성공 여부 |
| `message` | `string` | 응답 메시지 |
| `page` | `object` | 페이지 정보 |
| `page.pageId` | `number` | 페이지 ID |
| `page.pageNumber` | `number` | 페이지 번호 |
| `page.extractedText` | `string` | 추출된 텍스트 내용 |
| `page.confidence` | `number` | OCR 신뢰도 (0.0 ~ 1.0) |
| `page.language` | `string` | 인식된 언어 코드 |
| `page.wordCount` | `number` | 추출된 단어 수 |
| `page.textQuality` | `number` | 텍스트 품질 점수 |
| `page.qualityLevel` | `string` | 품질 등급 ("우수", "보통", "낮음") |
| `page.confidenceLevel` | `string` | 신뢰도 등급 ("매우 높음", "높음", "보통", "낮음") |
| `page.capturedAt` | `string` | 캡처 시간 (ISO 8601 형식) |
| `deviceInfo` | `string` | 디바이스 정보 |

### 2. 독서 세션 시작
**POST** `/api/image/session/start`

#### 요청 파라미터
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `userId` | `number` | ✅ | 사용자 ID | `1` |
| `bookId` | `number` | ✅ | 책 ID | `1` |

#### 응답 예시
```json
{
  "success": true,
  "message": "독서 세션이 시작되었습니다.",
  "sessionId": 456,
  "startTime": "2024-01-15T14:30:00"
}
```

#### 응답 스키마
| 필드 | 타입 | 설명 |
|------|------|------|
| `success` | `boolean` | 요청 성공 여부 |
| `message` | `string` | 응답 메시지 |
| `sessionId` | `number` | 세션 ID |
| `startTime` | `string` | 세션 시작 시간 (ISO 8601 형식) |

### 3. 독서 세션 종료
**POST** `/api/image/session/end`

#### 요청 파라미터
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `userId` | `number` | ✅ | 사용자 ID | `1` |

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

#### 응답 스키마
| 필드 | 타입 | 설명 |
|------|------|------|
| `success` | `boolean` | 요청 성공 여부 |
| `message` | `string` | 응답 메시지 |
| `totalPagesRead` | `number` | 총 읽은 페이지 수 |
| `totalWordsRead` | `number` | 총 읽은 단어 수 |
| `readingDurationMinutes` | `number` | 독서 시간 (분) |
| `endTime` | `string` | 세션 종료 시간 (ISO 8601 형식) |

### 4. 책 페이지 목록 조회
**GET** `/api/image/pages/{userId}/{bookId}`

#### URL 파라미터
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `userId` | `number` | ✅ | 사용자 ID | `1` |
| `bookId` | `number` | ✅ | 책 ID | `1` |

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

#### 응답 스키마
| 필드 | 타입 | 설명 |
|------|------|------|
| `success` | `boolean` | 요청 성공 여부 |
| `pages` | `array` | 페이지 목록 |
| `pages[].pageId` | `number` | 페이지 ID |
| `pages[].pageNumber` | `number` | 페이지 번호 |
| `pages[].extractedText` | `string` | 추출된 텍스트 |
| `pages[].confidence` | `number` | OCR 신뢰도 (0.0 ~ 1.0) |
| `pages[].textQuality` | `number` | 텍스트 품질 점수 |
| `pages[].qualityLevel` | `string` | 품질 등급 |
| `totalPages` | `number` | 총 페이지 수 |
| `count` | `number` | 반환된 페이지 수 |

### 5. 독서 통계 조회
**GET** `/api/image/stats/{userId}`

#### URL 파라미터
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `userId` | `number` | ✅ | 사용자 ID | `1` |

#### 응답 예시
```json
{
  "success": true,
  "stats": {
    "maxConsecutiveDays": 7,
    "totalReadingDays": 15,
    "currentConsecutiveDays": 3,
    "habitAnalysis": {
      "preferredTime": "저녁",
      "averageReadingTime": 45,
      "mostReadGenre": "소설"
    }
  }
}
```

#### 응답 스키마
| 필드 | 타입 | 설명 |
|------|------|------|
| `success` | `boolean` | 요청 성공 여부 |
| `stats` | `object` | 독서 통계 정보 |
| `stats.maxConsecutiveDays` | `number` | 최대 연속 독서일 |
| `stats.totalReadingDays` | `number` | 총 독서일 |
| `stats.currentConsecutiveDays` | `number` | 현재 연속 독서일 |
| `stats.habitAnalysis` | `object` | 독서 습관 분석 |
| `stats.habitAnalysis.preferredTime` | `string` | 선호하는 독서 시간대 |
| `stats.habitAnalysis.averageReadingTime` | `number` | 평균 독서 시간 (분) |
| `stats.habitAnalysis.mostReadGenre` | `string` | 가장 많이 읽는 장르 |

### 6. 월별 독서 통계 조회
**GET** `/api/image/stats/{userId}/monthly?months=6`

#### URL 파라미터
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `userId` | `number` | ✅ | 사용자 ID | `1` |

#### Query 파라미터
| 파라미터 | 타입 | 필수 | 설명 | 기본값 | 예시 |
|---------|------|------|------|--------|------|
| `months` | `number` | ❌ | 조회할 월 수 | `6` | `12` |

#### 응답 예시
```json
{
  "success": true,
  "monthlyStats": {
    "2024-01": {
      "totalPages": 45,
      "totalWords": 7200,
      "readingDays": 12
    },
    "2024-02": {
      "totalPages": 38,
      "totalWords": 6080,
      "readingDays": 10
    }
  }
}
```

#### 응답 스키마
| 필드 | 타입 | 설명 |
|------|------|------|
| `success` | `boolean` | 요청 성공 여부 |
| `monthlyStats` | `object` | 월별 통계 (키: YYYY-MM 형식) |
| `monthlyStats[YYYY-MM].totalPages` | `number` | 해당 월 총 페이지 수 |
| `monthlyStats[YYYY-MM].totalWords` | `number` | 해당 월 총 단어 수 |
| `monthlyStats[YYYY-MM].readingDays` | `number` | 해당 월 독서일 수 |

### 7. 특정 페이지 조회
**GET** `/api/image/pages/{userId}/{bookId}/{pageNumber}`

#### URL 파라미터
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `userId` | `number` | ✅ | 사용자 ID | `1` |
| `bookId` | `number` | ✅ | 책 ID | `1` |
| `pageNumber` | `number` | ✅ | 페이지 번호 | `45` |

### 8. 페이지 범위 조회
**GET** `/api/image/pages/{userId}/{bookId}/range?startPage=1&endPage=10`

#### URL 파라미터
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `userId` | `number` | ✅ | 사용자 ID | `1` |
| `bookId` | `number` | ✅ | 책 ID | `1` |

#### Query 파라미터
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `startPage` | `number` | ✅ | 시작 페이지 번호 | `1` |
| `endPage` | `number` | ✅ | 끝 페이지 번호 | `10` |

### 9. 최근 페이지 조회
**GET** `/api/image/pages/{userId}/{bookId}/recent?limit=10`

#### URL 파라미터
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `userId` | `number` | ✅ | 사용자 ID | `1` |
| `bookId` | `number` | ✅ | 책 ID | `1` |

#### Query 파라미터
| 파라미터 | 타입 | 필수 | 설명 | 기본값 | 예시 |
|---------|------|------|------|--------|------|
| `limit` | `number` | ❌ | 조회할 페이지 수 | `10` | `20` |

### 10. 페이지 삭제
**DELETE** `/api/image/pages/{userId}/{bookId}/{pageNumber}`

#### URL 파라미터
| 파라미터 | 타입 | 필수 | 설명 | 예시 |
|---------|------|------|------|------|
| `userId` | `number` | ✅ | 사용자 ID | `1` |
| `bookId` | `number` | ✅ | 책 ID | `1` |
| `pageNumber` | `number` | ✅ | 삭제할 페이지 번호 | `45` |

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
    
    def start_reading_session(self):
        """독서 세션을 시작합니다."""
        data = {
            'userId': self.user_id,
            'bookId': self.book_id
        }
        
        response = requests.post(
            f"{self.base_url}/api/image/session/start",
            data=data
        )
        
        if response.status_code == 200:
            result = response.json()
            if result['success']:
                print(f"독서 세션 시작: ID {result['sessionId']}")
                return result
        return None
    
    def end_reading_session(self):
        """독서 세션을 종료합니다."""
        data = {'userId': self.user_id}
        
        response = requests.post(
            f"{self.base_url}/api/image/session/end",
            data=data
        )
        
        if response.status_code == 200:
            result = response.json()
            if result['success']:
                print(f"독서 세션 종료: {result['totalPagesRead']}페이지 읽음")
                return result
        return None
    
    def get_reading_stats(self):
        """독서 통계를 조회합니다."""
        response = requests.get(
            f"{self.base_url}/api/image/stats/{self.user_id}"
        )
        
        if response.status_code == 200:
            return response.json()
        return None

# 사용 예시
client = ReadMarkClient("http://localhost:8080", 1, 1)

# 독서 세션 시작
client.start_reading_session()

# 페이지 촬영 및 업로드
client.capture_and_upload_page()

# 독서 통계 조회
stats = client.get_reading_stats()
print(f"총 독서일: {stats['stats']['totalReadingDays']}일")

# 독서 세션 종료
client.end_reading_session()
```

### Arduino (ESP32-CAM)
```cpp
#include <WiFi.h>
#include <HTTPClient.h>
#include <ArduinoJson.h>
#include "esp_camera.h"

// 🔧 WiFi 설정
const char* ssid = "YOUR_WIFI_SSID";           // WiFi 네트워크 이름
const char* password = "YOUR_WIFI_PASSWORD";   // WiFi 비밀번호
const char* serverUrl = "http://192.168.1.100:5000";  // ReadMark 서버 주소

// 📊 사용자 설정
const int USER_ID = 1;    // 사용자 ID
const int BOOK_ID = 1;    // 책 ID

void setup() {
  Serial.begin(115200);
  Serial.println("🚀 ReadMark ESP32-CAM 시작");
  
  // 📶 WiFi 연결
  WiFi.begin(ssid, password);
  while (WiFi.status() != WL_CONNECTED) {
    delay(1000);
    Serial.println("📶 WiFi 연결 중...");
  }
  Serial.println("✅ WiFi 연결됨");
  Serial.printf("📡 IP 주소: %s\n", WiFi.localIP().toString().c_str());
  
  // 📷 카메라 초기화
  camera_config_t config;
  config.ledc_channel = LEDC_CHANNEL_0;
  config.ledc_timer = LEDC_TIMER_0;
  config.pin_d0 = 5; config.pin_d1 = 18; config.pin_d2 = 19; config.pin_d3 = 21;
  config.pin_d4 = 36; config.pin_d5 = 39; config.pin_d6 = 34; config.pin_d7 = 35;
  config.pin_xclk = 0; config.pin_pclk = 22; config.pin_vsync = 25; config.pin_href = 23;
  config.pin_sscb_sda = 26; config.pin_sscb_scl = 27; config.pin_pwdn = 32; config.pin_reset = -1;
  config.xclk_freq_hz = 20000000;
  config.pixel_format = PIXFORMAT_JPEG;
  config.frame_size = FRAMESIZE_VGA;  // 640x480
  config.jpeg_quality = 12;           // 0-63 (낮을수록 고품질)
  config.fb_count = 1;
  
  esp_err_t err = esp_camera_init(&config);
  if (err != ESP_OK) {
    Serial.printf("❌ 카메라 초기화 실패: 0x%x\n", err);
    return;
  }
  Serial.println("✅ 카메라 초기화 완료");
}

// 📖 독서 세션 시작
void startReadingSession() {
  HTTPClient http;
  http.begin(serverUrl + "/api/image/session/start");
  http.addHeader("Content-Type", "application/x-www-form-urlencoded");
  
  String postData = "userId=" + String(USER_ID) + "&bookId=" + String(BOOK_ID);
  int httpResponseCode = http.POST(postData);
  
  if (httpResponseCode > 0) {
    String response = http.getString();
    Serial.println("📖 독서 세션 시작: " + response);
    
    // JSON 응답 파싱
    DynamicJsonDocument doc(1024);
    deserializeJson(doc, response);
    if (doc["success"] == true) {
      int sessionId = doc["sessionId"];
      Serial.printf("✅ 세션 ID: %d\n", sessionId);
    }
  } else {
    Serial.printf("❌ 세션 시작 실패: HTTP %d\n", httpResponseCode);
  }
  http.end();
}

// 🏁 독서 세션 종료
void endReadingSession() {
  HTTPClient http;
  http.begin(serverUrl + "/api/image/session/end");
  http.addHeader("Content-Type", "application/x-www-form-urlencoded");
  
  String postData = "userId=" + String(USER_ID);
  int httpResponseCode = http.POST(postData);
  
  if (httpResponseCode > 0) {
    String response = http.getString();
    Serial.println("🏁 독서 세션 종료: " + response);
    
    // JSON 응답 파싱
    DynamicJsonDocument doc(1024);
    deserializeJson(doc, response);
    if (doc["success"] == true) {
      int totalPages = doc["totalPagesRead"];
      int totalWords = doc["totalWordsRead"];
      int duration = doc["readingDurationMinutes"];
      Serial.printf("📊 총 %d페이지, %d단어, %d분 읽음\n", totalPages, totalWords, duration);
    }
  } else {
    Serial.printf("❌ 세션 종료 실패: HTTP %d\n", httpResponseCode);
  }
  http.end();
}

// 📸 이미지 촬영 및 업로드
void captureAndUpload() {
  Serial.println("📸 이미지 촬영 중...");
  
  // 이미지 촬영
  camera_fb_t * fb = esp_camera_fb_get();
  if (!fb) {
    Serial.println("❌ 이미지 촬영 실패");
    return;
  }
  Serial.printf("✅ 이미지 촬영 완료 (%d bytes)\n", fb->len);
  
  // HTTP 클라이언트 생성
  HTTPClient http;
  http.begin(serverUrl + "/api/image/upload");
  
  // 멀티파트 폼 데이터 구성
  String boundary = "----WebKitFormBoundary7MA4YWxkTrZu0gW";
  String postData = "--" + boundary + "\r\n";
  postData += "Content-Disposition: form-data; name=\"userId\"\r\n\r\n";
  postData += String(USER_ID) + "\r\n";
  postData += "--" + boundary + "\r\n";
  postData += "Content-Disposition: form-data; name=\"bookId\"\r\n\r\n";
  postData += String(BOOK_ID) + "\r\n";
  postData += "--" + boundary + "\r\n";
  postData += "Content-Disposition: form-data; name=\"deviceInfo\"\r\n\r\n";
  postData += "ESP32-CAM\r\n";
  postData += "--" + boundary + "\r\n";
  postData += "Content-Disposition: form-data; name=\"image\"; filename=\"page.jpg\"\r\n";
  postData += "Content-Type: image/jpeg\r\n\r\n";
  
  // 이미지 데이터 추가
  String endData = "\r\n--" + boundary + "--\r\n";
  
  // 전체 데이터 크기 계산
  size_t totalSize = postData.length() + fb->len + endData.length();
  
  http.addHeader("Content-Type", "multipart/form-data; boundary=" + boundary);
  http.addHeader("Content-Length", String(totalSize));
  
  // 데이터 전송
  http.POST((uint8_t*)postData.c_str(), postData.length());
  http.write(fb->buf, fb->len);
  http.POST((uint8_t*)endData.c_str(), endData.length());
  
  if (http.getResponseCode() > 0) {
    String response = http.getString();
    Serial.println("📤 업로드 응답: " + response);
    
    // JSON 응답 파싱
    DynamicJsonDocument doc(1024);
    deserializeJson(doc, response);
    
    if (doc["success"] == true) {
      int pageNumber = doc["page"]["pageNumber"];
      float confidence = doc["page"]["confidence"];
      float textQuality = doc["page"]["textQuality"];
      Serial.printf("✅ 페이지 %d 저장 완료!\n", pageNumber);
      Serial.printf("📊 신뢰도: %.2f, 품질: %.1f\n", confidence, textQuality);
    } else {
      Serial.println("❌ 업로드 실패: " + doc["message"].as<String>());
    }
  } else {
    Serial.printf("❌ HTTP 오류: %d\n", http.getResponseCode());
  }
  
  http.end();
  esp_camera_fb_return(fb);
}
```

## 🔧 설정 및 환경

### 1. 서버 설정
| 설정 항목 | 값 | 설명 |
|----------|-----|------|
| **서버 주소** | `http://192.168.1.100:5000` | ReadMark 서버 IP와 포트 |
| **사용자 ID** | `1` | 임베디드 기기 사용자 ID |
| **책 ID** | `1` | 읽고 있는 책의 ID |

### 2. WiFi 설정
| 설정 항목 | 값 | 설명 |
|----------|-----|------|
| **SSID** | `YOUR_WIFI_SSID` | WiFi 네트워크 이름 |
| **비밀번호** | `YOUR_WIFI_PASSWORD` | WiFi 비밀번호 |
| **대역** | `2.4GHz` | ESP32는 2.4GHz만 지원 |

### 3. 카메라 설정
| 설정 항목 | 값 | 설명 |
|----------|-----|------|
| **해상도** | `VGA (640x480)` | 권장 해상도 |
| **품질** | `12` | JPEG 품질 (0-63, 낮을수록 고품질) |
| **형식** | `JPEG` | 이미지 형식 |

### 4. 이미지 처리 설정
| 설정 항목 | 값 | 설명 |
|----------|-----|------|
| **최대 크기** | `10MB` | 업로드 가능한 최대 이미지 크기 |
| **지원 형식** | `JPEG, PNG, GIF` | 지원하는 이미지 형식 |
| **품질 임계값** | `50점 이상` | OCR 처리를 위한 최소 품질 |

## 📊 모니터링 및 로그

### 로그 레벨
| 레벨 | 설명 | 예시 |
|------|------|------|
| **INFO** | 일반적인 작업 진행 상황 | `✅ 이미지 촬영 완료 (245KB)` |
| **WARN** | 경고 상황 | `⚠️ 낮은 품질: 45점` |
| **ERROR** | 오류 상황 | `❌ HTTP 오류: 500` |

### 주요 메트릭
| 메트릭 | 설명 | 목표값 |
|--------|------|--------|
| **이미지 업로드 성공률** | 업로드 성공 비율 | `> 95%` |
| **텍스트 추출 정확도** | OCR 신뢰도 | `> 0.8` |
| **페이지 번호 인식 정확도** | 페이지 번호 인식률 | `> 90%` |
| **API 응답 시간** | 서버 응답 시간 | `< 3초` |

## 🚀 성능 최적화 팁

| 최적화 항목 | 권장사항 | 효과 |
|------------|----------|------|
| **이미지 품질** | 명확하고 선명한 이미지 사용 | OCR 정확도 향상 |
| **조명** | 적절한 조명으로 텍스트 가독성 향상 | 텍스트 인식률 증가 |
| **각도** | 책과 수직으로 촬영 | 왜곡 최소화 |
| **해상도** | 최소 VGA (640x480) 권장 | 처리 속도와 품질 균형 |
| **네트워크** | 안정적인 WiFi 연결 사용 | 업로드 성공률 향상 |

## 🔍 문제 해결

### 일반적인 오류
| 오류 유형 | 원인 | 해결 방법 |
|----------|------|----------|
| **텍스트 추출 실패** | 이미지 품질 부족 | 조명 개선, 각도 조정 |
| **페이지 번호 인식 실패** | 페이지 번호 불명확 | 페이지 번호가 명확히 보이도록 촬영 |
| **네트워크 오류** | WiFi 연결 불안정 | 연결 상태 확인, 서버 접근성 점검 |
| **업로드 실패** | 서버 응답 오류 | 서버 상태 확인, 재시도 |

### 디버깅 방법
1. **로그 확인**: 시리얼 모니터에서 상세 로그 확인
2. **이미지 품질 점수**: `textQuality` 값 확인 (50점 이상 권장)
3. **API 응답 분석**: JSON 응답의 `success` 및 `message` 필드 확인
4. **네트워크 상태**: WiFi 신호 강도 및 서버 연결 상태 점검
