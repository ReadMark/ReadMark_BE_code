-- cover_image_url 컬럼 문제 해결 스크립트
-- 실행 전에 반드시 데이터베이스 백업을 수행하세요!

USE readmark;

-- 1. 현재 테이블 구조 확인
SELECT '=== Current favorite_quotes table structure ===' as info;
DESCRIBE favorite_quotes;

-- 2. cover_image_url 컬럼 존재 여부 확인
SELECT '=== cover_image_url column check ===' as info;
SELECT COLUMN_NAME, DATA_TYPE, CHARACTER_MAXIMUM_LENGTH
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'favorite_quotes' 
  AND TABLE_SCHEMA = 'readmark'
  AND COLUMN_NAME = 'cover_image_url';

-- 3. cover_image_url 컬럼 추가 (존재하지 않는 경우)
-- ALTER TABLE favorite_quotes ADD COLUMN cover_image_url VARCHAR(1000);

-- 4. cover_image_url 컬럼 크기 수정 (존재하는 경우)
-- ALTER TABLE favorite_quotes MODIFY COLUMN cover_image_url VARCHAR(1000);

-- 5. 최종 테이블 구조 확인
SELECT '=== Final favorite_quotes table structure ===' as info;
DESCRIBE favorite_quotes;

-- 마이그레이션 완료 확인
SELECT 'Migration completed successfully' as status;
