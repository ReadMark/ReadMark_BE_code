-- BookPage 테이블 정리 스크립트
-- pageNumber만 사용하도록 불필요한 컬럼들 제거

-- 1. 불필요한 컬럼들 제거
ALTER TABLE book_pages 
DROP COLUMN IF EXISTS confidence,
DROP COLUMN IF EXISTS device_info,
DROP COLUMN IF EXISTS language,
DROP COLUMN IF EXISTS number_count,
DROP COLUMN IF EXISTS image_url,
DROP COLUMN IF EXISTS image_data;

-- 2. book_page_detected_numbers 테이블 제거 (더 이상 사용하지 않음)
DROP TABLE IF EXISTS book_page_detected_numbers;

-- 3. 테이블 구조 확인
DESCRIBE book_pages;

-- 4. 최종 테이블 구조
-- page_id (PK)
-- book_id (FK to books)
-- user_id (FK to users) 
-- page_number (INTEGER, NOT NULL) - OCR로 추출된 페이지 번호
-- captured_at (DATETIME, NOT NULL) - 촬영 시간
-- created_at (DATETIME, NOT NULL) - 생성 시간
