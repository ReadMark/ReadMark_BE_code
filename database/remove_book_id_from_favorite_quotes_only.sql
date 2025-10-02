-- 즐겨찾기 문장에서만 book_id 제거를 위한 데이터베이스 마이그레이션 스크립트
-- 실행 전에 반드시 데이터베이스 백업을 수행하세요!

USE readmark;

-- 1. 외래키 제약조건 확인
SELECT 
    CONSTRAINT_NAME,
    COLUMN_NAME,
    REFERENCED_TABLE_NAME,
    REFERENCED_COLUMN_NAME
FROM information_schema.KEY_COLUMN_USAGE 
WHERE TABLE_NAME = 'favorite_quotes' 
  AND TABLE_SCHEMA = 'readmark'
  AND REFERENCED_TABLE_NAME IS NOT NULL;

-- 2. 테이블 구조 확인
DESCRIBE favorite_quotes;

-- 3. book_id 컬럼이 존재하는지 확인
SELECT COLUMN_NAME 
FROM INFORMATION_SCHEMA.COLUMNS 
WHERE TABLE_NAME = 'favorite_quotes' 
  AND TABLE_SCHEMA = 'readmark'
  AND COLUMN_NAME = 'book_id';

-- 4. book_id 컬럼이 존재한다면 제거
-- ALTER TABLE favorite_quotes DROP COLUMN book_id;

-- 5. book_title 컬럼 추가 (이미 있다면 무시됨)
ALTER TABLE favorite_quotes ADD COLUMN book_title VARCHAR(255);

-- 마이그레이션 완료 확인
SELECT 'Migration completed successfully - book_id removed from favorite_quotes only' as status;
