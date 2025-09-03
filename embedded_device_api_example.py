#!/usr/bin/env python3
"""
ReadMark 임베디드 기기용 API 예제
이 스크립트는 임베디드 기기에서 책 페이지 이미지를 촬영하고 서버로 전송하는 예제입니다.
"""

import requests
import json
import time
import os
from datetime import datetime
import cv2
import numpy as np

class ReadMarkAPI:
    def __init__(self, base_url="http://localhost:8080"):
        self.base_url = base_url
        self.session = requests.Session()
        
    def start_reading_session(self, user_id, book_id):
        """독서 세션을 시작합니다."""
        url = f"{self.base_url}/api/image/session/start"
        data = {
            'userId': user_id,
            'bookId': book_id
        }
        
        try:
            response = self.session.post(url, data=data)
            response.raise_for_status()
            result = response.json()
            
            if result.get('success'):
                print(f"독서 세션 시작: {result.get('message')}")
                return result.get('sessionId')
            else:
                print(f"세션 시작 실패: {result.get('message')}")
                return None
                
        except Exception as e:
            print(f"세션 시작 중 오류: {e}")
            return None
    
    def end_reading_session(self, user_id):
        """독서 세션을 종료합니다."""
        url = f"{self.base_url}/api/image/session/end"
        data = {'userId': user_id}
        
        try:
            response = self.session.post(url, data=data)
            response.raise_for_status()
            result = response.json()
            
            if result.get('success'):
                print(f"독서 세션 종료: {result.get('message')}")
                print(f"총 읽은 페이지: {result.get('totalPagesRead')}")
                print(f"총 읽은 단어: {result.get('totalWordsRead')}")
                print(f"독서 시간: {result.get('readingDurationMinutes')}분")
                return result
            else:
                print(f"세션 종료 실패: {result.get('message')}")
                return None
                
        except Exception as e:
            print(f"세션 종료 중 오류: {e}")
            return None
    
    def upload_book_image(self, user_id, book_id, image_path, device_info="RaspberryPi", capture_time=None):
        """책 페이지 이미지를 업로드하고 분석합니다."""
        url = f"{self.base_url}/api/image/upload"
        
        if capture_time is None:
            capture_time = datetime.now().isoformat()
        
        try:
            with open(image_path, 'rb') as image_file:
                files = {'image': image_file}
                data = {
                    'userId': user_id,
                    'bookId': book_id,
                    'deviceInfo': device_info,
                    'captureTime': capture_time
                }
                
                response = self.session.post(url, files=files, data=data)
                response.raise_for_status()
                result = response.json()
                
                if result.get('success'):
                    analysis = result.get('analysis', {})
                    print(f"이미지 분석 완료: {result.get('message')}")
                    print(f"추출된 텍스트 길이: {len(analysis.get('extractedText', ''))}")
                    print(f"추출된 단어 수: {analysis.get('wordCount', 0)}")
                    print(f"텍스트 품질: {analysis.get('textQuality', 0):.1f}%")
                    print(f"추정 페이지 번호: {analysis.get('estimatedPageNumber', 'N/A')}")
                    return result
                else:
                    print(f"이미지 분석 실패: {result.get('message')}")
                    return None
                    
        except Exception as e:
            print(f"이미지 업로드 중 오류: {e}")
            return None
    
    def get_reading_stats(self, user_id):
        """독서 통계를 조회합니다."""
        url = f"{self.base_url}/api/image/stats/{user_id}"
        
        try:
            response = self.session.get(url)
            response.raise_for_status()
            result = response.json()
            
            if result.get('success'):
                stats = result.get('stats', {})
                habit_analysis = stats.get('habitAnalysis', {})
                
                print("=== 독서 통계 ===")
                print(f"최대 연속 독서일: {stats.get('maxConsecutiveDays', 0)}일")
                print(f"총 독서일: {stats.get('totalReadingDays', 0)}일")
                print(f"현재 연속 독서일: {stats.get('currentConsecutiveDays', 0)}일")
                print(f"평균 페이지/일: {habit_analysis.get('avgPagesPerDay', 0)}")
                print(f"선호 요일: {habit_analysis.get('favoriteDayOfWeek', '없음')}")
                print(f"독서 빈도: {habit_analysis.get('readingFrequency', '0%')}")
                
                return result
            else:
                print(f"통계 조회 실패: {result.get('message')}")
                return None
                
        except Exception as e:
            print(f"통계 조회 중 오류: {e}")
            return None

class CameraController:
    """카메라 제어 클래스 (Raspberry Pi Camera 또는 USB 카메라용)"""
    
    def __init__(self, camera_index=0):
        self.camera_index = camera_index
        self.camera = None
        
    def initialize_camera(self):
        """카메라를 초기화합니다."""
        try:
            self.camera = cv2.VideoCapture(self.camera_index)
            if not self.camera.isOpened():
                print("카메라를 열 수 없습니다.")
                return False
            
            # 카메라 설정
            self.camera.set(cv2.CAP_PROP_FRAME_WIDTH, 1920)
            self.camera.set(cv2.CAP_PROP_FRAME_HEIGHT, 1080)
            self.camera.set(cv2.CAP_PROP_AUTOFOCUS, 1)
            
            print("카메라 초기화 완료")
            return True
            
        except Exception as e:
            print(f"카메라 초기화 실패: {e}")
            return False
    
    def capture_image(self, save_path):
        """이미지를 촬영하고 저장합니다."""
        if self.camera is None:
            print("카메라가 초기화되지 않았습니다.")
            return False
        
        try:
            ret, frame = self.camera.read()
            if not ret:
                print("이미지 촬영 실패")
                return False
            
            # 이미지 품질 개선
            frame = cv2.GaussianBlur(frame, (5, 5), 0)
            
            # 이미지 저장
            cv2.imwrite(save_path, frame, [cv2.IMWRITE_JPEG_QUALITY, 95])
            print(f"이미지 저장 완료: {save_path}")
            return True
            
        except Exception as e:
            print(f"이미지 촬영 중 오류: {e}")
            return False
    
    def release_camera(self):
        """카메라를 해제합니다."""
        if self.camera is not None:
            self.camera.release()
            print("카메라 해제 완료")

def main():
    """메인 실행 함수"""
    # API 클라이언트 초기화
    api = ReadMarkAPI("http://localhost:8080")
    
    # 카메라 초기화
    camera = CameraController()
    if not camera.initialize_camera():
        print("카메라 초기화 실패. 프로그램을 종료합니다.")
        return
    
    # 사용자 정보 (실제 사용 시에는 설정에서 읽어와야 함)
    USER_ID = 1
    BOOK_ID = 1
    DEVICE_INFO = "RaspberryPi_BookReader_v1.0"
    
    try:
        # 독서 세션 시작
        session_id = api.start_reading_session(USER_ID, BOOK_ID)
        if session_id is None:
            print("독서 세션을 시작할 수 없습니다.")
            return
        
        print("독서를 시작합니다. 'q'를 누르면 종료됩니다.")
        
        page_count = 0
        while True:
            # 사용자 입력 확인 (실제 임베디드에서는 버튼 입력 등으로 대체)
            user_input = input("페이지를 촬영하려면 Enter를, 종료하려면 'q'를 누르세요: ")
            
            if user_input.lower() == 'q':
                break
            
            # 이미지 촬영
            image_path = f"book_page_{page_count:03d}.jpg"
            if camera.capture_image(image_path):
                # 이미지 업로드 및 분석
                result = api.upload_book_image(USER_ID, BOOK_ID, image_path, DEVICE_INFO)
                
                if result and result.get('success'):
                    page_count += 1
                    print(f"페이지 {page_count} 처리 완료")
                else:
                    print("이미지 분석에 실패했습니다. 다시 촬영해주세요.")
            else:
                print("이미지 촬영에 실패했습니다.")
            
            # 잠시 대기
            time.sleep(1)
        
        # 독서 세션 종료
        api.end_reading_session(USER_ID)
        
        # 독서 통계 조회
        api.get_reading_stats(USER_ID)
        
    except KeyboardInterrupt:
        print("\n프로그램이 중단되었습니다.")
        api.end_reading_session(USER_ID)
    
    finally:
        # 카메라 해제
        camera.release_camera()
        
        # 임시 이미지 파일 정리
        for i in range(page_count):
            temp_file = f"book_page_{i:03d}.jpg"
            if os.path.exists(temp_file):
                os.remove(temp_file)

if __name__ == "__main__":
    main()
