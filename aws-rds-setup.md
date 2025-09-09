# AWS RDS MySQL 설정 가이드 (선택사항)

## 🗄️ RDS MySQL 무료 티어 설정

### 무료 티어 제한사항
- **인스턴스**: db.t2.micro
- **스토리지**: 20GB
- **백업**: 20GB
- **시간**: 750시간/월
- **데이터 전송**: 월 1GB

### 1단계: RDS 인스턴스 생성

#### AWS 콘솔에서:
1. RDS 서비스로 이동
2. "데이터베이스 생성" 클릭
3. 설정:
   - **엔진 유형**: MySQL
   - **버전**: MySQL 8.0
   - **템플릿**: 프리 티어
   - **DB 인스턴스 식별자**: readmark-db
   - **마스터 사용자 이름**: readmark
   - **마스터 암호**: [안전한 비밀번호]
   - **DB 이름**: readmark

#### 보안 그룹 설정:
- **포트**: 3306
- **소스**: 애플리케이션 서버의 보안 그룹

### 2단계: 애플리케이션 설정

#### 환경변수 설정:
```bash
# Elastic Beanstalk
eb setenv \
  DB_HOST=readmark-db.xxxxxxxxx.ap-northeast-2.rds.amazonaws.com \
  DB_PORT=3306 \
  DB_NAME=readmark \
  DB_USERNAME=readmark \
  DB_PASSWORD=your_password \
  SPRING_PROFILES_ACTIVE=aws-rds

# EC2 Docker
docker run -d -p 8080:8080 \
  -e DB_HOST=readmark-db.xxxxxxxxx.ap-northeast-2.rds.amazonaws.com \
  -e DB_PORT=3306 \
  -e DB_NAME=readmark \
  -e DB_USERNAME=readmark \
  -e DB_PASSWORD=your_password \
  -e SPRING_PROFILES_ACTIVE=aws-rds \
  --name readmark-app \
  readmark-backend
```

### 3단계: 데이터베이스 초기화

#### SQL 스크립트 실행:
```sql
-- 사용자 테이블
CREATE TABLE IF NOT EXISTS users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- 책 테이블
CREATE TABLE IF NOT EXISTS books (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(200) NOT NULL,
    author VARCHAR(100),
    isbn VARCHAR(20),
    user_id BIGINT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(id)
);

-- 페이지 테이블
CREATE TABLE IF NOT EXISTS book_pages (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    page_number INT NOT NULL,
    image_url VARCHAR(500),
    ocr_text TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (book_id) REFERENCES books(id)
);

-- 읽기 세션 테이블
CREATE TABLE IF NOT EXISTS reading_sessions (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    start_time TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    total_pages INT DEFAULT 0,
    FOREIGN KEY (user_id) REFERENCES users(id),
    FOREIGN KEY (book_id) REFERENCES books(id)
);
```

## 💡 H2 vs RDS 선택 가이드

### H2 인메모리 DB (현재 설정)
**장점:**
- ✅ 완전 무료
- ✅ 설정 간단
- ✅ 빠른 개발/테스트

**단점:**
- ❌ 서버 재시작 시 데이터 손실
- ❌ 확장성 제한
- ❌ 동시 사용자 제한

### RDS MySQL (업그레이드 옵션)
**장점:**
- ✅ 데이터 영구 보존
- ✅ 높은 가용성
- ✅ 자동 백업
- ✅ 확장 가능

**단점:**
- ❌ 무료 티어 한도 존재
- ❌ 설정 복잡
- ❌ 월 비용 발생 가능

## 🔄 마이그레이션 방법

### H2에서 RDS로 마이그레이션:
1. RDS 인스턴스 생성
2. `application-aws-rds.properties` 사용
3. 환경변수 설정
4. 애플리케이션 재배포

### 데이터 이전:
```bash
# H2 데이터 덤프 (개발 환경에서)
java -cp h2-*.jar org.h2.tools.Script -url jdbc:h2:mem:readmark -user sa -script dump.sql

# MySQL로 임포트
mysql -h your-rds-endpoint -u readmark -p readmark < dump.sql
```

## 📊 비용 모니터링

### RDS 비용 체크리스트:
- [ ] 월 750시간 초과 여부
- [ ] 20GB 스토리지 초과 여부
- [ ] 백업 스토리지 사용량
- [ ] 데이터 전송량 (월 1GB)

### 비용 절약 팁:
1. **개발 중**: RDS 인스턴스 중지
2. **백업**: 불필요한 백업 삭제
3. **모니터링**: CloudWatch 알람 설정
4. **정기 검토**: 월별 사용량 확인
