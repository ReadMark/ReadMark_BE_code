# Multi-stage build for ReadMark Backend
FROM openjdk:21-jdk-slim as builder

# 작업 디렉토리 설정
WORKDIR /app

# Gradle wrapper와 build.gradle 복사
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .

# 소스 코드 복사
COPY src src

# Gradle 빌드 실행
RUN chmod +x gradlew
RUN ./gradlew clean build -x test

# Runtime stage
FROM openjdk:21-jre-slim

# 보안을 위한 non-root 사용자 생성
RUN groupadd -r readmark && useradd -r -g readmark readmark

# 작업 디렉토리 설정
WORKDIR /app

# 빌드된 JAR 파일 복사
COPY --from=builder /app/build/libs/*.jar app.jar

# 파일 소유권 변경
RUN chown -R readmark:readmark /app

# non-root 사용자로 전환
USER readmark

# 포트 노출 (application.properties에서 5000으로 설정됨)
EXPOSE 5000

# 환경변수 설정
ENV SPRING_PROFILES_ACTIVE=docker
ENV SERVER_PORT=5000

# 애플리케이션 실행
ENTRYPOINT ["java", "-jar", "app.jar"]
