# 웹소켓 연결 문제 해결 가이드

## 1. 서버 구성 확인

### Spring Boot 웹소켓 설정
- ✅ `spring-boot-starter-websocket` 의존성 포함됨
- ✅ `@EnableWebSocket` 어노테이션으로 웹소켓 활성화
- ✅ `WebSocketConfig` 클래스에서 핸들러 등록
- ✅ SockJS 지원 추가 (폴백 옵션)

### 포트 설정
- **개발 환경**: `server.port=5000`
- **AWS 환경**: `server.port=${PORT:5000}`
- **Docker 환경**: `server.port=5000`
- **서버 주소**: `server.address=0.0.0.0` (모든 인터페이스에서 접근 가능)

## 2. 웹소켓 엔드포인트

### ⚠️ 중요: 프로토콜 사용법
- **웹소켓 연결**: 반드시 `ws://` 또는 `wss://` 사용
- **HTTP API**: `http://` 또는 `https://` 사용
- **SockJS**: `http://` 또는 `https://` 사용 (웹소켓 폴백)

### 기본 웹소켓 연결
```
ws://43.200.102.14:5000/ws
```

### SockJS 연결 (폴백)
```
http://43.200.102.14:5000/ws/info
```

### 진단 엔드포인트
- 상태 확인: `GET /api/esp32/ws/status`
- 진단 정보: `GET /api/esp32/ws/diagnostics`
- 테스트 메시지: `POST /api/esp32/ws/test`

### 웹소켓 테스트 페이지
- 브라우저에서 테스트: `http://43.200.102.14:5000/websocket-test.html`
- 실시간 연결 테스트 및 메시지 송수신 가능
- 탄력적 IP로 고정된 주소 사용

## 3. 방화벽 및 보안 설정

### AWS 보안 그룹
다음 포트들이 열려있는지 확인:
- **HTTP**: 80, 5000
- **HTTPS**: 443
- **웹소켓**: 5000 (HTTP와 동일한 포트 사용)

### Ubuntu 방화벽 (UFW)
```bash
# UFW 상태 확인
sudo ufw status

# 웹소켓 포트 열기 (필요시)
sudo ufw allow 5000
```

## 4. 네트워크 연결 테스트

### 서버 연결 확인
```bash
# 서버 포트 확인
telnet your-server-ip 5000

# 웹소켓 엔드포인트 확인
curl -i http://43.200.102.14:5000/api/esp32/ws/status
curl -i http://43.200.102.14:5000/api/esp32/ws/diagnostics
```

### ESP32에서 연결 테스트
```cpp
// ESP32 코드에서 웹소켓 연결 테스트
WebSocketsClient webSocket;

// 올바른 웹소켓 URL 사용 (ws:// 프로토콜)
webSocket.begin("43.200.102.14", 5000, "/ws");
webSocket.onEvent(webSocketEvent);

// 또는 전체 URL로 연결
// webSocket.begin("ws://43.200.102.14:5000/ws");
```

## 5. 로그 확인

### Spring Boot 로그
웹소켓 관련 로그 레벨이 DEBUG로 설정되어 있어야 함:
```properties
logging.level.org.springframework.web.socket=DEBUG
logging.level.com.example.ReadMark.handler.ESP32WebSocketHandler=DEBUG
logging.level.com.example.ReadMark.config.WebSocketConfig=DEBUG
```

### 주요 로그 메시지
- `WebSocket 핸들러 등록 시작: /ws`
- `ESP32 WebSocket 연결됨: {sessionId}`
- `WebSocket 전송 오류` (오류 발생시)

## 6. 일반적인 문제 및 해결방법

### 문제 1: 연결 거부 (Connection Refused)
**원인**: 서버가 실행되지 않았거나 포트가 열려있지 않음
**해결**: 
- 서버 실행 상태 확인
- 포트 설정 확인
- 방화벽 설정 확인

### 문제 2: CORS 오류
**원인**: 브라우저의 CORS 정책에 의해 차단
**해결**: 
- `CorsConfig` 클래스에서 모든 origin 허용 설정 확인
- 웹소켓은 CORS 제한이 덜하지만, SockJS 사용시 필요

### 문제 3: 프로토콜 오류
**원인**: HTTP와 웹소켓 프로토콜 혼동
**해결**: 
- **웹소켓 연결**: 반드시 `ws://` 또는 `wss://` 프로토콜 사용
  - `ws://43.200.102.14:5000/ws` (HTTP 기반)
  - `wss://43.200.102.14:5000/ws` (HTTPS 기반)
- **SockJS 연결**: `http://` 또는 `https://` 프로토콜 사용
  - `http://43.200.102.14:5000/ws/info` (SockJS 정보)
  - `http://43.200.102.14:5000/ws/websocket` (SockJS 웹소켓)

### 문제 4: 핸드셰이크 실패
**원인**: 웹소켓 업그레이드 요청 실패
**해결**: 
- `WebSocketConfig`에서 핸들러 설정 확인
- Tomcat 업그레이드 전략 사용 확인

## 7. ESP32 연결 예제

### 기본 웹소켓 연결
```cpp
#include <WebSocketsClient.h>

WebSocketsClient webSocket;

void setup() {
    // WiFi 연결
    WiFi.begin("SSID", "PASSWORD");
    
    // 웹소켓 연결 (ws:// 프로토콜 사용)
    webSocket.begin("43.200.102.14", 5000, "/ws");
    webSocket.onEvent(webSocketEvent);

    // 또는 전체 URL로 연결
    // webSocket.begin("ws://43.200.102.14:5000/ws");
}

void webSocketEvent(WStype_t type, uint8_t * payload, size_t length) {
    switch(type) {
        case WStype_DISCONNECTED:
            Serial.println("웹소켓 연결 끊어짐");
            break;
        case WStype_CONNECTED:
            Serial.println("웹소켓 연결됨");
            break;
        case WStype_TEXT:
            Serial.println("메시지 수신: " + String((char*)payload));
            break;
    }
}
```

### SockJS 연결 (대안)
```cpp
// SockJS는 HTTP 폴링을 사용하므로 일반 HTTP 클라이언트로도 가능
HTTPClient http;
http.begin("http://your-server:5000/ws/info");
int httpCode = http.GET();
```

## 8. 모니터링 및 디버깅

### 실시간 연결 상태 확인
```bash
# 연결된 클라이언트 수 확인
curl http://your-server:5000/api/esp32/ws/status

# 진단 정보 확인
curl http://your-server:5000/api/esp32/ws/diagnostics
```

### 로그 모니터링
```bash
# Spring Boot 로그 실시간 확인
tail -f logs/application.log | grep -i websocket
```

## 9. 성능 최적화

### 웹소켓 설정 최적화
- `maxTextMessageBufferSize`: 8192
- `maxBinaryMessageBufferSize`: 8192
- `maxSessionIdleTimeout`: 300000ms (5분)

### 연결 풀 관리
- 연결된 세션 수 모니터링
- 비활성 세션 정리
- 메모리 사용량 확인

## 10. 문제 보고

문제가 지속될 경우 다음 정보를 포함하여 보고:
1. 서버 로그 (웹소켓 관련)
2. ESP32 로그
3. 네트워크 연결 상태
4. 방화벽 설정
5. 사용 중인 프로토콜 (ws:// 또는 http://)
