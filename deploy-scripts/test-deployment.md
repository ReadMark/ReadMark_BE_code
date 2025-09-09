# 배포 테스트 및 검증 가이드

## 🚀 배포 후 테스트 체크리스트

### 1. 기본 연결 테스트
```bash
# 애플리케이션 상태 확인
curl http://your-eb-url.elasticbeanstalk.com/actuator/health

# 예상 응답:
# {"status":"UP"}
```

### 2. API 엔드포인트 테스트
```bash
# 사용자 목록 조회
curl http://your-eb-url.elasticbeanstalk.com/api/users

# 책 목록 조회
curl http://your-eb-url.elasticbeanstalk.com/api/books

# 읽기 세션 목록 조회
curl http://your-eb-url.elasticbeanstalk.com/api/sessions
```

### 3. 이미지 업로드 테스트
```bash
# 테스트 이미지 업로드
curl -X POST \
  http://your-eb-url.elasticbeanstalk.com/api/upload \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@test-image.jpg' \
  -F 'userId=1' \
  -F 'bookId=1'

# 예상 응답:
# {"success":true,"message":"이미지가 성공적으로 업로드되었습니다","data":{...}}
```

### 4. ESP32 전용 엔드포인트 테스트
```bash
# ESP32 세션 시작
curl -X POST \
  http://your-eb-url.elasticbeanstalk.com/api/esp32/session/start \
  -H 'Content-Type: application/json' \
  -d '{"userId":1,"bookId":1,"deviceInfo":"ESP32-CAM"}'

# ESP32 이미지 업로드
curl -X POST \
  http://your-eb-url.elasticbeanstalk.com/api/esp32/upload \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@esp32-test-image.jpg' \
  -F 'sessionId=1'
```

## 📱 ESP32 코드 업데이트

### ESP32 main.c 수정
```c
// 기존 URL을 AWS URL로 변경
const char* serverUrl = "http://your-eb-url.elasticbeanstalk.com";

// 또는 환경변수로 설정
const char* serverUrl = "http://readmark-production.ap-northeast-2.elasticbeanstalk.com";
```

### ESP32 연결 테스트 코드
```c
void testServerConnection() {
    HTTPClient http;
    http.begin(serverUrl + "/actuator/health");
    
    int httpResponseCode = http.GET();
    
    if (httpResponseCode > 0) {
        String response = http.getString();
        Serial.println("서버 연결 성공: " + response);
    } else {
        Serial.println("서버 연결 실패: " + String(httpResponseCode));
    }
    
    http.end();
}
```

## 🔍 문제 해결 가이드

### 1. 연결 실패 문제
**증상**: ESP32에서 서버에 연결할 수 없음
**해결방법**:
- AWS 보안 그룹에서 HTTP(80) 포트 열기
- Elastic Beanstalk 환경 상태 확인
- 로드 밸런서 상태 확인

### 2. CORS 오류
**증상**: 브라우저에서 CORS 오류 발생
**해결방법**:
```properties
# application-aws.properties에서 CORS 설정 확인
spring.web.cors.allowed-origins=*
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE
spring.web.cors.allowed-headers=*
```

### 3. Google Vision API 오류
**증상**: OCR 기능이 작동하지 않음
**해결방법**:
```bash
# 환경변수 확인
eb printenv

# Google Vision API 키 재설정
eb setenv GOOGLE_APPLICATION_CREDENTIALS_JSON="[Base64 인코딩된 JSON]"
```

### 4. 메모리 부족 오류
**증상**: t2.micro에서 OutOfMemoryError 발생
**해결방법**:
```bash
# JVM 힙 크기 조정
eb setenv JAVA_OPTS="-Xmx512m -Xms256m"
```

## 📊 성능 모니터링

### 1. AWS CloudWatch 메트릭
- **CPU 사용률**: 80% 이하 유지
- **메모리 사용률**: 80% 이하 유지
- **네트워크 I/O**: 정상 범위 확인
- **디스크 I/O**: 정상 범위 확인

### 2. 애플리케이션 로그 모니터링
```bash
# 실시간 로그 확인
eb logs --all

# 특정 시간대 로그 확인
eb logs --all --start-time "2024-01-01 00:00:00"
```

### 3. 응답 시간 측정
```bash
# API 응답 시간 테스트
time curl http://your-eb-url.elasticbeanstalk.com/api/users

# 이미지 업로드 응답 시간 테스트
time curl -X POST \
  http://your-eb-url.elasticbeanstalk.com/api/upload \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@test-image.jpg' \
  -F 'userId=1' \
  -F 'bookId=1'
```

## 🔄 업데이트 배포

### 1. 코드 변경 후 재배포
```bash
# 코드 수정 후
./gradlew clean build -x test

# Elastic Beanstalk에 배포
eb deploy
```

### 2. 환경변수 변경
```bash
# 환경변수 업데이트
eb setenv NEW_VARIABLE=value

# 환경 재시작
eb restart
```

### 3. 롤백 (문제 발생 시)
```bash
# 이전 버전으로 롤백
eb deploy --version previous
```

## 💰 비용 모니터링

### 1. AWS Billing Dashboard 확인
- 월별 사용량 확인
- 무료 티어 한도 모니터링
- 예상 비용 확인

### 2. 비용 알림 설정
```bash
# CloudWatch 알람 생성
aws cloudwatch put-metric-alarm \
    --alarm-name "ReadMark-High-CPU" \
    --alarm-description "CPU 사용률이 80% 초과" \
    --metric-name CPUUtilization \
    --namespace AWS/EC2 \
    --statistic Average \
    --period 300 \
    --threshold 80 \
    --comparison-operator GreaterThanThreshold
```

### 3. 리소스 최적화
- 사용하지 않는 인스턴스 중지
- 불필요한 스토리지 정리
- 로그 파일 크기 제한

## 🎯 성공 기준

### 배포 성공 기준:
- [ ] 애플리케이션이 정상적으로 시작됨
- [ ] 모든 API 엔드포인트가 응답함
- [ ] ESP32에서 서버에 연결 가능
- [ ] 이미지 업로드 및 OCR 기능 작동
- [ ] Google Vision API 정상 작동
- [ ] 데이터베이스 연결 정상
- [ ] 로그에 오류 없음

### 성능 기준:
- [ ] API 응답 시간 < 2초
- [ ] 이미지 업로드 시간 < 10초
- [ ] CPU 사용률 < 80%
- [ ] 메모리 사용률 < 80%
- [ ] 가용성 > 99%

## 📞 지원 및 도움

### 문제 발생 시 확인사항:
1. AWS 콘솔에서 리소스 상태 확인
2. CloudWatch 로그 확인
3. 보안 그룹 설정 확인
4. 환경변수 설정 확인
5. Google Vision API 할당량 확인

### 추가 도움:
- AWS 문서: https://docs.aws.amazon.com/
- Elastic Beanstalk 가이드: https://docs.aws.amazon.com/elasticbeanstalk/
- Google Vision API 문서: https://cloud.google.com/vision/docs
