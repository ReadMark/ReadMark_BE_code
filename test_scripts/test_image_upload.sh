#!/bin/bash

echo "========================================"
echo "ReadMark 이미지 업로드 테스트"
echo "========================================"

# 테스트용 이미지 파일 생성 (1KB 더미 이미지)
echo "테스트용 이미지 파일 생성 중..."
dd if=/dev/zero of=test_image.jpg bs=1024 count=1 2>/dev/null

echo
echo "1. 기본 이미지 업로드 테스트..."
curl -X POST http://43.200.102.14:5000/api/image/upload \
  -F "userId=1" \
  -F "bookId=1" \
  -F "image=@test_image.jpg" \
  -F "deviceInfo=Test-Device" \
  -F "captureTime=2025-01-14 12:30:00"

echo
echo
echo "2. ESP32 이미지 업로드 테스트..."
curl -X POST http://43.200.102.14:5000/api/esp32/upload \
  -F "image=@test_image.jpg"

echo
echo
echo "3. 책 목록 조회 테스트..."
curl -X GET "http://43.200.102.14:5000/api/books?userId=1"

echo
echo
echo "4. 독서 세션 시작 테스트..."
curl -X POST http://43.200.102.14:5000/api/reading-sessions/start \
  -H "Content-Type: application/json" \
  -d '{"userId":1,"bookId":1}'

echo
echo
echo "5. 독서 통계 조회 테스트..."
curl -X GET "http://43.200.102.14:5000/api/reading-stats?userId=1"

echo
echo "========================================"
echo "테스트 완료!"
echo "========================================"
