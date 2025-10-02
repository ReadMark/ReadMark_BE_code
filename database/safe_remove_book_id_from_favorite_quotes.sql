-- 즐겨찾기 문장에서 book_id 안전하게 제거하는 스크립트
-- 실행 전에 반드시 데이터베이스 백업을 수행하세요!

USE readmark;

-- 1. 현재 상태 확인
SELECT '=== Current favorite_quotes table structure ===' as info;
DESCRIBE favorite_quotes;

-- 2. 외래키 제약조건 확인
SELECT '=== Foreign key constraints ===' as info;
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'favorite_quotes' 
  AND TABLE_SCHEMA = 'readmark'
  AND REFERENCED_TABLE_NAME IS NOT NULL;

-- 3. book_id 컬럼 존재 여부 확인
SELECT '=== book_id column check ===' as info;
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'favorite_quotes' 
  AND TABLE_SCHEMA = 'readmark'
  AND COLUMN_NAME = 'book_id';

-- 4. book_title 컬럼 존재 여부 확인
SELECT '=== book_title column check ===' as info;
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'favorite_quotes' 
  AND TABLE_SCHEMA = 'readmark'
  AND COLUMN_NAME = 'book_title';

-- 5. book_title 컬럼 추가 (존재하지 않는 경우에만)
-- ALTER TABLE favorite_quotes ADD COLUMN book_title VARCHAR(255);

-- 6. book_id 컬럼 제거 (존재하는 경우에만)
-- ALTER TABLE favorite_quotes DROP COLUMN book_id;

-- 마이그레이션 완료 확인
SELECT '=== Migration completed ===' as status;
