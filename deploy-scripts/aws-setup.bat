@echo off
echo ========================================
echo ReadMark AWS 배포 스크립트
echo ========================================

echo 1. AWS CLI 설치 확인...
aws --version
if %errorlevel% neq 0 (
    echo AWS CLI가 설치되지 않았습니다.
    echo https://aws.amazon.com/cli/ 에서 다운로드하세요.
    pause
    exit /b 1
)

echo 2. AWS CLI 설정...
aws configure
if %errorlevel% neq 0 (
    echo AWS CLI 설정에 실패했습니다.
    pause
    exit /b 1
)

echo 3. Elastic Beanstalk CLI 설치...
pip install awsebcli
if %errorlevel% neq 0 (
    echo pip이 설치되지 않았습니다. Python을 먼저 설치하세요.
    pause
    exit /b 1
)

echo 4. 프로젝트 빌드...
call gradlew clean build -x test
if %errorlevel% neq 0 (
    echo 빌드에 실패했습니다.
    pause
    exit /b 1
)

echo 5. Elastic Beanstalk 초기화...
eb init
if %errorlevel% neq 0 (
    echo EB 초기화에 실패했습니다.
    pause
    exit /b 1
)

echo 6. 환경 생성...
eb create production
if %errorlevel% neq 0 (
    echo 환경 생성에 실패했습니다.
    pause
    exit /b 1
)

echo ========================================
echo 배포가 완료되었습니다!
echo ========================================
echo.
echo 다음 단계:
echo 1. Google Vision API 키를 환경변수로 설정
echo 2. ESP32에서 새로운 URL로 연결 테스트
echo.
pause
