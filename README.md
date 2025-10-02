# 📚 ReadMark Backend

## 🎯 프로젝트 개요
ReadMark는 책 페이지를 촬영하여 OCR로 텍스트를 추출하고 독서 기록을 관리하는 시스템입니다.

## 🚀 주요 기능
- **이미지 업로드 및 OCR 처리**: Google Vision API를 통한 텍스트 추출
- **독서 세션 관리**: 독서 시작/종료 및 통계 관리
- **사용자 관리**: 회원가입, 로그인, 사용자 정보 관리
- **책 관리**: 책 등록, 검색, 상세 정보 관리
- **독서 기록**: 일별 독서 기록 및 통계
- **캘린더**: 월별 독서 현황 시각화
- **마이페이지**: 즐겨찾기 페이지/문장 관리
- **웹소켓**: 실시간 통신 지원

## 📋 API 문서
- **[완전한 API 명세서](COMPLETE_API_SPECIFICATION.md)**: 모든 API 엔드포인트 (35개)
- **[프론트엔드 API 가이드](FRONTEND_API_SPECIFICATION.md)**: 프론트엔드 개발용

## 🛠️ 기술 스택
- **Backend**: Spring Boot, Java 17
- **Database**: MySQL
- **OCR**: Google Vision API
- **WebSocket**: Spring WebSocket
- **Build Tool**: Gradle

## 🚀 빠른 시작

### 1. 환경 설정
```bash
# Google Vision API 설정
cp src/main/resources/application.properties.example src/main/resources/application.properties
# Google Vision API 키 설정 필요
```

### 2. 데이터베이스 설정
```bash
# MySQL 데이터베이스 생성
mysql -u root -p
CREATE DATABASE readmark;
```

### 3. 애플리케이션 실행
```bash
./gradlew bootRun
```

### 4. API 테스트
```bash
# 헬스체크
curl http://localhost:5000/upload/health

# 이미지 업로드 테스트
curl -X POST http://localhost:5000/upload/ \
  -F "image=@test_image.jpg"
```

## 📁 프로젝트 구조
```
src/main/java/com/example/ReadMark/
├── config/          # 설정 클래스
├── controller/      # REST API 컨트롤러
├── service/         # 비즈니스 로직
├── repository/      # 데이터 접근 계층
├── model/           # 엔티티 및 DTO
├── handler/         # 웹소켓 핸들러
└── constant/        # 상수 정의
```

## 🔧 주요 API 엔드포인트

### 이미지 업로드
- `POST /upload/` - 임베디드용 단순한 OCR API
- `POST /api/image/upload` - 프론트엔드용 완전한 독서 관리 API

### 사용자 관리
- `POST /api/users/join` - 회원가입
- `POST /api/users/login` - 로그인
- `GET /api/users` - 사용자 목록

### 책 관리
- `POST /api/books` - 책 등록
- `GET /api/books/search` - 책 검색
- `GET /api/books/{bookId}` - 책 상세 조회

### 독서 세션
- `POST /api/image/session/start` - 독서 세션 시작
- `POST /api/image/session/end` - 독서 세션 종료
- `GET /api/image/stats/{userId}` - 독서 통계

## 📚 추가 문서
- **[AWS 배포 가이드](aws-deploy-guide.md)**
- **[Docker 가이드](DOCKER_GUIDE.md)**
- **[Google Vision 설정](GOOGLE_VISION_SETUP.md)**
- **[웹소켓 트러블슈팅](WEBSOCKET_TROUBLESHOOTING.md)**

## 🤝 팀별 사용 가이드

### 임베디드 팀 (ESP32)
- **단순한 OCR API**: `POST /upload/`
- **상태 확인**: `GET /upload/health`, `GET /upload/status`
- **테스트**: `POST /upload/test`

### 프론트엔드 팀
- **완전한 독서 관리**: `POST /api/image/upload`
- **사용자 관리**: `/api/users/*`
- **책 관리**: `/api/books/*`
- **통계 및 캘린더**: `/api/image/stats/*`, `/api/calendar/*`

## 📞 지원
문제가 발생하면 다음을 확인하세요:
1. [완전한 API 명세서](COMPLETE_API_SPECIFICATION.md) 참조
2. [웹소켓 트러블슈팅](WEBSOCKET_TROUBLESHOOTING.md) 확인
3. 서버 로그 확인

---
**버전**: 1.0  
**최종 업데이트**: 2025-01-14