# ReadMark MySQL 데이터베이스 설정 가이드

## 📋 개요
이 가이드는 ReadMark 프로젝트를 위한 MySQL 데이터베이스를 설정하는 방법을 설명합니다.

## 🗄️ 데이터베이스 생성

### 1. MySQL 서버 설치 및 실행
```bash
# Windows (MySQL Installer 사용 권장)
# https://dev.mysql.com/downloads/installer/ 에서 다운로드

# 또는 Docker 사용
docker run --name readmark-mysql -e MYSQL_ROOT_PASSWORD=your_password -e MYSQL_DATABASE=readmark -e MYSQL_USER=readmark -e MYSQL_PASSWORD=readmark_password -p 3306:3306 -d mysql:8.0
```

### 2. 데이터베이스 스키마 생성
```bash
# MySQL 클라이언트로 접속
mysql -u root -p

# 또는 Docker 컨테이너에 접속
docker exec -it readmark-mysql mysql -u root -p
```

```sql
-- 스키마 파일 실행
source mysql_database_schema.sql;

-- 또는 직접 실행
mysql -u root -p < mysql_database_schema.sql
```

## ⚙️ 애플리케이션 설정

### 1. application-mysql.properties 파일 생성
```properties
# MySQL 데이터베이스 설정
server.port=5000

# MySQL 데이터베이스 연결 설정
spring.datasource.url=jdbc:mysql://localhost:3306/readmark?useSSL=false&serverTimezone=Asia/Seoul&allowPublicKeyRetrieval=true&characterEncoding=UTF-8
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.datasource.username=readmark
spring.datasource.password=readmark_password

# JPA 설정
spring.jpa.database-platform=org.hibernate.dialect.MySQL8Dialect
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.use_sql_comments=true

# 파일 업로드 설정
spring.servlet.multipart.max-file-size=10MB
spring.servlet.multipart.max-request-size=10MB

# 로깅 설정
logging.level.com.example.ReadMark=INFO
logging.level.org.springframework.web=INFO
logging.level.org.hibernate.SQL=DEBUG
logging.level.org.hibernate.type.descriptor.sql.BasicBinder=TRACE

# CORS 설정
spring.web.cors.allowed-origins=*
spring.web.cors.allowed-methods=GET,POST,PUT,DELETE,OPTIONS
spring.web.cors.allowed-headers=*
spring.web.cors.allow-credentials=true
```

### 2. build.gradle에 MySQL 의존성 추가
```gradle
dependencies {
    // 기존 의존성들...
    
    // MySQL 드라이버
    runtimeOnly 'mysql:mysql-connector-java:8.0.33'
    
    // 또는 최신 버전
    runtimeOnly 'com.mysql:mysql-connector-j:8.2.0'
}
```

### 3. 애플리케이션 실행
```bash
# MySQL 프로파일로 실행
./gradlew bootRun --args='--spring.profiles.active=mysql'

# 또는 IDE에서 실행 시 VM 옵션
-Dspring.profiles.active=mysql
```

## 🔧 환경별 설정

### 개발 환경
```properties
# application-dev-mysql.properties
spring.datasource.url=jdbc:mysql://localhost:3306/readmark_dev
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
```

### 운영 환경
```properties
# application-prod-mysql.properties
spring.datasource.url=jdbc:mysql://your-production-server:3306/readmark
spring.jpa.hibernate.ddl-auto=validate
spring.jpa.show-sql=false
```

## 🐳 Docker Compose 설정

### docker-compose.yml
```yaml
version: '3.8'

services:
  mysql:
    image: mysql:8.0
    container_name: readmark-mysql
    environment:
      MYSQL_ROOT_PASSWORD: root_password
      MYSQL_DATABASE: readmark
      MYSQL_USER: readmark
      MYSQL_PASSWORD: readmark_password
    ports:
      - "3306:3306"
    volumes:
      - mysql_data:/var/lib/mysql
      - ./mysql_database_schema.sql:/docker-entrypoint-initdb.d/init.sql
    command: --default-authentication-plugin=mysql_native_password

  app:
    build: .
    container_name: readmark-app
    ports:
      - "5000:5000"
    environment:
      SPRING_PROFILES_ACTIVE: mysql
      DB_HOST: mysql
      DB_PORT: 3306
      DB_NAME: readmark
      DB_USERNAME: readmark
      DB_PASSWORD: readmark_password
    depends_on:
      - mysql

volumes:
  mysql_data:
```

## 📊 데이터베이스 관리

### 백업
```bash
# 전체 데이터베이스 백업
mysqldump -u root -p readmark > readmark_backup.sql

# 특정 테이블만 백업
mysqldump -u root -p readmark users books > users_books_backup.sql
```

### 복원
```bash
# 백업에서 복원
mysql -u root -p readmark < readmark_backup.sql
```

### 성능 모니터링
```sql
-- 활성 연결 확인
SHOW PROCESSLIST;

-- 테이블 상태 확인
SHOW TABLE STATUS;

-- 인덱스 사용률 확인
SHOW INDEX FROM users;
```

## 🔒 보안 설정

### 1. 사용자 권한 설정
```sql
-- 전용 사용자 생성
CREATE USER 'readmark'@'localhost' IDENTIFIED BY 'strong_password';
CREATE USER 'readmark'@'%' IDENTIFIED BY 'strong_password';

-- 권한 부여
GRANT SELECT, INSERT, UPDATE, DELETE ON readmark.* TO 'readmark'@'localhost';
GRANT SELECT, INSERT, UPDATE, DELETE ON readmark.* TO 'readmark'@'%';

-- 권한 적용
FLUSH PRIVILEGES;
```

### 2. SSL 연결 설정
```properties
# SSL 연결 강제
spring.datasource.url=jdbc:mysql://localhost:3306/readmark?useSSL=true&requireSSL=true&serverTimezone=Asia/Seoul
```

## 🚨 문제 해결

### 일반적인 오류들

1. **연결 거부 오류**
   ```bash
   # MySQL 서비스 상태 확인
   net start mysql
   # 또는
   systemctl status mysql
   ```

2. **인증 오류**
   ```sql
   -- 사용자 인증 방식 변경
   ALTER USER 'readmark'@'localhost' IDENTIFIED WITH mysql_native_password BY 'password';
   ```

3. **문자 인코딩 문제**
   ```sql
   -- 데이터베이스 문자셋 확인
   SHOW VARIABLES LIKE 'character_set%';
   
   -- 테이블 문자셋 변경
   ALTER TABLE users CONVERT TO CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
   ```

## 📈 성능 최적화

### 1. 인덱스 최적화
```sql
-- 자주 사용되는 쿼리에 대한 인덱스 추가
CREATE INDEX idx_user_books_status ON user_books(user_id, status);
CREATE INDEX idx_reading_sessions_user_time ON reading_sessions(user_id, start_time);
```

### 2. 연결 풀 설정
```properties
# HikariCP 설정
spring.datasource.hikari.maximum-pool-size=10
spring.datasource.hikari.minimum-idle=5
spring.datasource.hikari.connection-timeout=20000
spring.datasource.hikari.idle-timeout=300000
spring.datasource.hikari.max-lifetime=1200000
```

이제 ReadMark 프로젝트가 MySQL 데이터베이스와 완벽하게 호환됩니다! 🎉

