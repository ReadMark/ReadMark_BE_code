# Google Vision API 설정 가이드

## 🔑 Google Cloud Console 설정

### 1단계: Google Cloud 프로젝트 생성
1. [Google Cloud Console](https://console.cloud.google.com/) 접속
2. 새 프로젝트 생성 또는 기존 프로젝트 선택
3. 프로젝트 이름: `readmark-ocr`

### 2단계: Vision API 활성화
1. **API 및 서비스** > **라이브러리** 이동
2. "Cloud Vision API" 검색
3. **사용 설정** 클릭

### 3단계: 서비스 계정 생성
1. **IAM 및 관리** > **서비스 계정** 이동
2. **서비스 계정 만들기** 클릭
3. 설정:
   - **서비스 계정 이름**: `readmark-vision-service`
   - **서비스 계정 ID**: `readmark-vision-service`
   - **설명**: `ReadMark OCR 서비스용 계정`

### 4단계: 역할 부여
1. **역할** 선택: `Cloud Vision API 사용자`
2. **완료** 클릭

### 5단계: 키 생성
1. 생성된 서비스 계정 클릭
2. **키** 탭 이동
3. **키 추가** > **새 키 만들기**
4. **JSON** 선택 후 **만들기**
5. JSON 파일 다운로드 (안전하게 보관)

## 🔧 AWS 환경변수 설정

### Elastic Beanstalk 설정
```bash
# JSON 키 파일을 Base64로 인코딩
base64 -i google-credentials.json

# 환경변수로 설정
eb setenv GOOGLE_APPLICATION_CREDENTIALS_JSON="[Base64 인코딩된 JSON]"
```

### EC2 Docker 설정
```bash
# JSON 파일을 컨테이너에 복사
docker cp google-credentials.json readmark-app:/tmp/google-credentials.json

# 또는 환경변수로 설정
docker run -d -p 8080:8080 \
  -e GOOGLE_APPLICATION_CREDENTIALS=/tmp/google-credentials.json \
  -v /path/to/google-credentials.json:/tmp/google-credentials.json:ro \
  --name readmark-app \
  readmark-backend
```

## 💰 Google Vision API 비용

### 무료 할당량 (월)
- **텍스트 감지**: 1,000 요청/월
- **이미지 라벨링**: 1,000 요청/월
- **안전 검색**: 1,000 요청/월

### 유료 요금 (할당량 초과 시)
- **텍스트 감지**: $1.50/1,000 요청
- **이미지 라벨링**: $1.50/1,000 요청

## 🔒 보안 모범 사례

### 1. 키 파일 보안
- ✅ JSON 키 파일을 버전 관리에 포함하지 않기
- ✅ 환경변수로 관리
- ✅ 정기적인 키 로테이션

### 2. AWS IAM 정책
```json
{
    "Version": "2012-10-17",
    "Statement": [
        {
            "Effect": "Allow",
            "Action": [
                "secretsmanager:GetSecretValue"
            ],
            "Resource": "arn:aws:secretsmanager:region:account:secret:google-vision-credentials"
        }
    ]
}
```

### 3. AWS Secrets Manager 사용 (권장)
```bash
# JSON 키를 Secrets Manager에 저장
aws secretsmanager create-secret \
    --name "google-vision-credentials" \
    --description "Google Vision API credentials" \
    --secret-string file://google-credentials.json

# 애플리케이션에서 사용
eb setenv GOOGLE_APPLICATION_CREDENTIALS_SECRET="google-vision-credentials"
```

## 🧪 테스트 방법

### 1. 로컬 테스트
```bash
# 환경변수 설정
export GOOGLE_APPLICATION_CREDENTIALS="path/to/google-credentials.json"

# 애플리케이션 실행
./gradlew bootRun --args='--spring.profiles.active=aws'
```

### 2. API 테스트
```bash
# 이미지 업로드 테스트
curl -X POST \
  http://localhost:8080/api/upload \
  -H 'Content-Type: multipart/form-data' \
  -F 'file=@test-image.jpg' \
  -F 'userId=1' \
  -F 'bookId=1'
```

### 3. ESP32 연결 테스트
```cpp
// ESP32 코드에서 URL 변경
const char* serverUrl = "http://your-eb-url.elasticbeanstalk.com";
```

## 🚨 문제 해결

### 일반적인 오류들

#### 1. 인증 오류
```
Error: Could not load the default credentials
```
**해결방법:**
- JSON 키 파일 경로 확인
- 환경변수 설정 확인
- 서비스 계정 권한 확인

#### 2. API 할당량 초과
```
Error: Quota exceeded
```
**해결방법:**
- Google Cloud Console에서 할당량 확인
- 요청 빈도 조절
- 유료 계정으로 업그레이드

#### 3. 네트워크 오류
```
Error: Network error
```
**해결방법:**
- AWS 보안 그룹 설정 확인
- 방화벽 설정 확인
- VPC 설정 확인

## 📊 모니터링

### Google Cloud Console 모니터링
1. **API 및 서비스** > **할당량** 이동
2. **Cloud Vision API** 선택
3. 사용량 및 할당량 확인

### AWS CloudWatch 모니터링
```bash
# 로그 확인
eb logs

# 메트릭 확인
aws cloudwatch get-metric-statistics \
    --namespace AWS/ApplicationELB \
    --metric-name RequestCount \
    --dimensions Name=LoadBalancer,Value=your-load-balancer \
    --start-time 2024-01-01T00:00:00Z \
    --end-time 2024-01-02T00:00:00Z \
    --period 3600 \
    --statistics Sum
```
