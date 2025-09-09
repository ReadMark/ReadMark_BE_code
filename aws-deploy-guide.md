# AWS 무료 배포 가이드 - ReadMark

## 🚀 배포 방법 선택

### 방법 1: AWS Elastic Beanstalk (권장)
- **장점**: 자동 스케일링, 로드 밸런싱, 모니터링
- **무료 티어**: 750시간/월 (t2.micro)
- **적합한 이유**: Spring Boot 애플리케이션에 최적화

### 방법 2: AWS EC2 + Docker
- **장점**: 더 많은 제어권, Docker 컨테이너화
- **무료 티어**: 750시간/월 (t2.micro)

## 📋 사전 준비사항

### 1. AWS 계정 설정
- [ ] AWS 계정 생성 완료
- [ ] AWS CLI 설치 및 설정
- [ ] IAM 사용자 생성 (프로그래밍 방식 접근)

### 2. Google Vision API 설정
- [ ] Google Cloud Console에서 서비스 계정 키 생성
- [ ] JSON 키 파일 다운로드
- [ ] AWS 환경변수로 설정

## 🛠️ 배포 단계

### Elastic Beanstalk 배포 (권장)

#### 1단계: AWS CLI 설정
```bash
aws configure
# AWS Access Key ID: [입력]
# AWS Secret Access Key: [입력]
# Default region name: ap-northeast-2 (서울)
# Default output format: json
```

#### 2단계: 애플리케이션 빌드
```bash
# Windows
gradlew clean build -x test

# Linux/Mac
./gradlew clean build -x test
```

#### 3단계: Elastic Beanstalk CLI 설치
```bash
pip install awsebcli
```

#### 4단계: Elastic Beanstalk 애플리케이션 초기화
```bash
eb init
# Application name: readmark-backend
# Platform: Java
# Platform version: Java 17
# Region: ap-northeast-2
```

#### 5단계: 환경 생성 및 배포
```bash
eb create production
# Environment name: readmark-production
# DNS CNAME prefix: [고유한 이름]
```

#### 6단계: 환경변수 설정
```bash
eb setenv GOOGLE_APPLICATION_CREDENTIALS=/tmp/google-credentials.json
```

### EC2 + Docker 배포

#### 1단계: EC2 인스턴스 생성
- AMI: Amazon Linux 2
- 인스턴스 타입: t2.micro
- 보안 그룹: HTTP(80), HTTPS(443), SSH(22), Custom TCP(8080)

#### 2단계: Docker 설치
```bash
sudo yum update -y
sudo yum install -y docker
sudo systemctl start docker
sudo systemctl enable docker
sudo usermod -a -G docker ec2-user
```

#### 3단계: 애플리케이션 배포
```bash
# Docker 이미지 빌드
docker build -t readmark-backend .

# 컨테이너 실행
docker run -d -p 8080:8080 \
  -e GOOGLE_APPLICATION_CREDENTIALS=/tmp/google-credentials.json \
  --name readmark-app \
  readmark-backend
```

## 🔧 환경변수 설정

### Google Vision API
```bash
# Elastic Beanstalk
eb setenv GOOGLE_APPLICATION_CREDENTIALS=/tmp/google-credentials.json

# EC2
export GOOGLE_APPLICATION_CREDENTIALS=/tmp/google-credentials.json
```

## 📊 모니터링 및 로그

### CloudWatch 로그 확인
```bash
# Elastic Beanstalk
eb logs

# EC2
docker logs readmark-app
```

## 💰 비용 최적화 팁

1. **인스턴스 중지**: 개발 중에는 인스턴스를 중지
2. **모니터링**: CloudWatch로 리소스 사용량 모니터링
3. **알람 설정**: 비용 초과 시 알림 설정
4. **정기 정리**: 사용하지 않는 리소스 정리

## 🔒 보안 설정

1. **보안 그룹**: 필요한 포트만 열기
2. **IAM 역할**: 최소 권한 원칙
3. **환경변수**: 민감한 정보는 환경변수로 관리
4. **HTTPS**: SSL 인증서 설정 (Let's Encrypt 무료)

## 🚨 주의사항

- **무료 티어 한도**: 월 750시간 초과 시 요금 발생
- **데이터 전송**: 월 1GB 초과 시 요금 발생
- **스토리지**: 30GB 초과 시 요금 발생
- **정기 모니터링**: AWS Billing Dashboard 확인

## 📞 문제 해결

### 일반적인 문제들
1. **포트 충돌**: 8080 포트가 이미 사용 중
2. **메모리 부족**: t2.micro는 1GB RAM 제한
3. **Google Vision API**: 인증서 파일 경로 확인
4. **CORS 오류**: ESP32에서 접근 시 CORS 설정 확인

### 로그 확인 방법
```bash
# Elastic Beanstalk
eb logs --all

# EC2 Docker
docker logs -f readmark-app
```

## 🔄 업데이트 배포

### Elastic Beanstalk
```bash
eb deploy
```

### EC2 Docker
```bash
docker stop readmark-app
docker rm readmark-app
docker build -t readmark-backend .
docker run -d -p 8080:8080 --name readmark-app readmark-backend
```
