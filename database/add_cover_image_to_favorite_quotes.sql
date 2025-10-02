-- 즐겨찾기 문장의 cover_image_url 컬럼 크기 수정
-- 실행 전에 반드시 데이터베이스 백업을 수행하세요!

USE readmark;

-- 1. 현재 테이블 구조 확인
SELECT '=== Current favorite_quotes table structure ===' as info;
DESCRIBE favorite_quotes;

-- 2. cover_image_url 컬럼 정보 확인
SELECT '=== cover_image_url column info ===' as info;
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH, IS_NULLABLE
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'favorite_quotes' 
  AND TABLE_SCHEMA = 'readmark'
  AND COLUMN_NAME = 'cover_image_url';

-- 3. cover_image_url 컬럼 크기 수정 (이미 존재하는 경우)
ALTER TABLE favorite_quotes MODIFY COLUMN cover_image_url VARCHAR(1000);

-- 4. 테이블 구조 확인
SELECT '=== Updated favorite_quotes table structure ===' as info;
DESCRIBE favorite_quotes;

-- 마이그레이션 완료 확인
SELECT 'Migration completed successfully - cover_image_url column size updated to VARCHAR(1000)' as status;
