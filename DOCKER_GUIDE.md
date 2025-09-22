# 🐳 Docker 사용 가이드 - ReadMark

## 📋 개요

이 가이드는 ReadMark 백엔드 애플리케이션을 Docker로 실행하는 방법을 설명합니다.

## 🚀 빠른 시작

### 1. Docker 이미지 빌드
```bash
# 프로젝트 루트 디렉토리에서
docker build -t readmark-backend .
```

### 2. 단일 컨테이너 실행
```bash
# MySQL이 별도로 실행 중인 경우
docker run -d -p 5000:5000 \
  -e DATABASE_URL=jdbc:mysql://host.docker.internal:3306/readmark \
  -e DATABASE_USERNAME=readmarkuser \
  -e DATABASE_PASSWORD=!@12QWqw \
  --name readmark-app \
  readmark-backend
```

### 3. Docker Compose로 전체 스택 실행 (권장)
```bash
# MySQL + ReadMark Backend를 함께 실행
docker-compose up -d
```

## 🔧 환경 설정

### 환경변수 설정

| 변수명 | 설명 | 기본값 |
|--------|------|--------|
| `SPRING_PROFILES_ACTIVE` | Spring 프로파일 | `docker` |
| `DATABASE_URL` | 데이터베이스 연결 URL | `jdbc:mysql://mysql:3306/readmark` |
| `DATABASE_USERNAME` | DB 사용자명 | `readmarkuser` |
| `DATABASE_PASSWORD` | DB 비밀번호 | `!@12QWqw` |
| `GOOGLE_APPLICATION_CREDENTIALS` | Google Vision API 키 파일 경로 | `/app/google-credentials.json` |

### Google Vision API 설정

1. Google Cloud Console에서 서비스 계정 키 생성
2. JSON 키 파일을 `google-credentials.json`으로 저장
3. Docker 컨테이너에 마운트:

```bash
docker run -d -p 5000:5000 \
  -v /path/to/google-credentials.json:/app/google-credentials.json:ro \
  readmark-backend
```

## 📊 모니터링

### 로그 확인
```bash
# 컨테이너 로그 실시간 확인
docker logs -f readmark-backend

# Docker Compose 사용 시
docker-compose logs -f readmark-backend
```

### 컨테이너 상태 확인
```bash
# 실행 중인 컨테이너 목록
docker ps

# 컨테이너 상세 정보
docker inspect readmark-backend
```

## 🔄 업데이트 및 재배포

### 코드 변경 후 재빌드
```bash
# 이미지 재빌드
docker build -t readmark-backend .

# 기존 컨테이너 중지 및 제거
docker stop readmark-backend
docker rm readmark-backend

# 새 컨테이너 실행
docker run -d -p 5000:5000 --name readmark-backend readmark-backend
```

### Docker Compose 사용 시
```bash
# 서비스 재시작
docker-compose restart readmark-backend

# 전체 스택 재시작
docker-compose down
docker-compose up -d
```

## 🛠️ 개발 환경 설정

### 로컬 개발용 Docker Compose
```yaml
# docker-compose.dev.yml
version: '3.8'
services:
  readmark-backend:
    build: .
    ports:
      - "5000:5000"
    environment:
      - SPRING_PROFILES_ACTIVE=dev
    volumes:
      - ./src:/app/src  # 소스 코드 실시간 반영
```

```bash
# 개발 환경 실행
docker-compose -f docker-compose.dev.yml up
```

## 🐛 문제 해결

### 일반적인 문제들

1. **포트 충돌**
   ```bash
   # 5000 포트 사용 중인 프로세스 확인
   netstat -ano | findstr :5000
   
   # 다른 포트로 실행
   docker run -p 8080:5000 readmark-backend
   ```

2. **데이터베이스 연결 실패**
   ```bash
   # MySQL 컨테이너 상태 확인
   docker logs readmark-mysql
   
   # 네트워크 확인
   docker network ls
   ```

3. **Google Vision API 오류**
   ```bash
   # 인증서 파일 경로 확인
   docker exec readmark-backend ls -la /app/google-credentials.json
   ```

### 로그 레벨 조정
```bash
# 디버그 모드로 실행
docker run -e LOGGING_LEVEL_COM_EXAMPLE_READMARK=DEBUG readmark-backend
```

## 📈 성능 최적화

### 메모리 제한 설정
```bash
docker run -d -p 5000:5000 \
  --memory=1g \
  --memory-swap=1g \
  readmark-backend
```

### CPU 제한 설정
```bash
docker run -d -p 5000:5000 \
  --cpus="1.0" \
  readmark-backend
```

## 🔒 보안 설정

### 비밀번호 환경변수 사용
```bash
# .env 파일 생성
echo "DATABASE_PASSWORD=your_secure_password" > .env

# 환경변수 파일 사용
docker run --env-file .env readmark-backend
```

### 네트워크 격리
```bash
# 사용자 정의 네트워크 생성
docker network create readmark-network

# 컨테이너를 격리된 네트워크에서 실행
docker run --network readmark-network readmark-backend
```

## 📝 유용한 명령어

```bash
# 컨테이너 내부 접속
docker exec -it readmark-backend /bin/bash

# 컨테이너 리소스 사용량 확인
docker stats readmark-backend

# 이미지 정리
docker image prune -f

# 컨테이너 정리
docker container prune -f
```








