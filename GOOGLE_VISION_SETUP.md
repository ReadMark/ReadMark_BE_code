# Google Vision API 설정 가이드

## 1. Google Cloud Platform 프로젝트 설정

### 1.1 Google Cloud Console 접속
1. [Google Cloud Console](https://console.cloud.google.com/)에 접속
2. Google 계정으로 로그인

### 1.2 새 프로젝트 생성
1. 상단의 프로젝트 선택 드롭다운 클릭
2. "새 프로젝트" 클릭
3. 프로젝트 이름 입력 (예: `readmark-vision-api`)
4. "만들기" 클릭

### 1.3 Vision API 활성화
1. 왼쪽 메뉴에서 "API 및 서비스" > "라이브러리" 클릭
2. 검색창에 "Vision API" 입력
3. "Cloud Vision API" 클릭
4. "사용" 버튼 클릭

## 2. 서비스 계정 키 생성

### 2.1 서비스 계정 생성
1. 왼쪽 메뉴에서 "API 및 서비스" > "사용자 인증 정보" 클릭
2. "사용자 인증 정보 만들기" > "서비스 계정" 클릭
3. 서비스 계정 이름 입력 (예: `readmark-vision-service`)
4. "만들고 계속하기" 클릭
5. 역할 선택: "Cloud Vision API 사용자"
6. "완료" 클릭

### 2.2 키 파일 다운로드
1. 생성된 서비스 계정 클릭
2. "키" 탭 클릭
3. "키 추가" > "새 키 만들기" 클릭
4. "JSON" 선택 후 "만들기" 클릭
5. JSON 키 파일이 자동으로 다운로드됨

## 3. 환경 변수 설정

### 3.1 Windows 환경
```cmd
set GOOGLE_APPLICATION_CREDENTIALS=C:\path\to\your\service-account-key.json
```

### 3.2 Linux/Mac 환경
```bash
export GOOGLE_APPLICATION_CREDENTIALS="/path/to/your/service-account-key.json"
```

### 3.3 영구 설정 (Linux/Mac)
```bash
echo 'export GOOGLE_APPLICATION_CREDENTIALS="/path/to/your/service-account-key.json"' >> ~/.bashrc
source ~/.bashrc
```

## 4. 애플리케이션 설정

### 4.1 application.properties 설정
```properties
# Google Vision API 설정
google.vision.api.key=your-api-key-here
google.vision.project.id=your-project-id-here
```

### 4.2 키 파일 위치 권장사항
- 프로젝트 루트에 `config/` 폴더 생성
- JSON 키 파일을 `config/google-vision-key.json`에 저장
- `.gitignore`에 키 파일 경로 추가

## 5. 테스트

### 5.1 간단한 테스트
```bash
# 환경 변수 확인
echo $GOOGLE_APPLICATION_CREDENTIALS

# 애플리케이션 실행
./gradlew bootRun
```

### 5.2 API 테스트
```bash
# 이미지 업로드 테스트
curl -X POST \
  http://localhost:8080/api/image/upload \
  -F "userId=1" \
  -F "bookId=1" \
  -F "image=@test-image.jpg"
```

## 6. 임베디드 기기 설정

### 6.1 Python 환경 설정
```bash
# 필요한 패키지 설치
pip install requests opencv-python numpy

# API 테스트
python embedded_device_api_example.py
```

### 6.2 Raspberry Pi 설정
```bash
# 카메라 활성화
sudo raspi-config
# Interface Options > Camera > Enable

# 필요한 패키지 설치
sudo apt-get update
sudo apt-get install python3-pip python3-opencv
pip3 install requests numpy
```

## 7. 문제 해결

### 7.1 인증 오류
```
Error: Could not create client: Application Default Credentials not found
```
**해결방법:**
- `GOOGLE_APPLICATION_CREDENTIALS` 환경 변수 확인
- JSON 키 파일 경로가 올바른지 확인
- 키 파일에 읽기 권한이 있는지 확인

### 7.2 API 할당량 초과
```
Error: Quota exceeded for quota group 'default'
```
**해결방법:**
- Google Cloud Console에서 할당량 확인
- 필요시 할당량 증가 요청
- 무료 티어: 월 1,000회 요청

### 7.3 이미지 형식 오류
```
Error: Invalid image format
```
**해결방법:**
- 지원 형식: JPEG, PNG, GIF, BMP, WEBP
- 이미지 크기: 최대 10MB
- 이미지 해상도: 최대 20MP

## 8. 보안 고려사항

### 8.1 키 파일 보안
- JSON 키 파일을 절대 Git에 커밋하지 않음
- 프로덕션 환경에서는 환경 변수 사용
- 정기적으로 키 파일 교체

### 8.2 API 사용량 모니터링
- Google Cloud Console에서 사용량 확인
- 비용 알림 설정
- 무료 할당량 초과 시 과금 주의

## 9. 성능 최적화

### 9.1 이미지 최적화
- 이미지 크기: 1920x1080 권장
- 압축률: JPEG 85-95%
- 텍스트가 명확하게 보이도록 조명 확보

### 9.2 API 호출 최적화
- 배치 처리로 여러 이미지 동시 분석
- 캐싱을 통한 중복 요청 방지
- 에러 재시도 로직 구현

## 10. 추가 리소스

- [Google Vision API 문서](https://cloud.google.com/vision/docs)
- [Java 클라이언트 라이브러리](https://googleapis.dev/java/google-cloud-vision/latest/)
- [API 할당량 및 가격](https://cloud.google.com/vision/pricing)
- [모범 사례](https://cloud.google.com/vision/docs/best-practices)
