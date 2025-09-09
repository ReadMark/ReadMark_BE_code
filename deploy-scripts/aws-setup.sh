#!/bin/bash

echo "========================================"
echo "ReadMark AWS 배포 스크립트"
echo "========================================"

# AWS CLI 설치 확인
echo "1. AWS CLI 설치 확인..."
if ! command -v aws &> /dev/null; then
    echo "AWS CLI가 설치되지 않았습니다."
    echo "https://aws.amazon.com/cli/ 에서 다운로드하세요."
    exit 1
fi

aws --version

# AWS CLI 설정
echo "2. AWS CLI 설정..."
aws configure
if [ $? -ne 0 ]; then
    echo "AWS CLI 설정에 실패했습니다."
    exit 1
fi

# Elastic Beanstalk CLI 설치
echo "3. Elastic Beanstalk CLI 설치..."
pip install awsebcli
if [ $? -ne 0 ]; then
    echo "pip이 설치되지 않았습니다. Python을 먼저 설치하세요."
    exit 1
fi

# 프로젝트 빌드
echo "4. 프로젝트 빌드..."
./gradlew clean build -x test
if [ $? -ne 0 ]; then
    echo "빌드에 실패했습니다."
    exit 1
fi

# Elastic Beanstalk 초기화
echo "5. Elastic Beanstalk 초기화..."
eb init
if [ $? -ne 0 ]; then
    echo "EB 초기화에 실패했습니다."
    exit 1
fi

# 환경 생성
echo "6. 환경 생성..."
eb create production
if [ $? -ne 0 ]; then
    echo "환경 생성에 실패했습니다."
    exit 1
fi

echo "========================================"
echo "배포가 완료되었습니다!"
echo "========================================"
echo ""
echo "다음 단계:"
echo "1. Google Vision API 키를 환경변수로 설정"
echo "2. ESP32에서 새로운 URL로 연결 테스트"
echo ""
