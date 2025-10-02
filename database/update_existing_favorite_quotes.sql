-- 기존 즐겨찾기 문장에 책 제목 추가하는 스크립트
-- 실행 전에 반드시 데이터베이스 백업을 수행하세요!

USE readmark;

-- 1. 현재 상태 확인
SELECT '=== Current favorite_quotes data ===' as info;
SELECT fav_quote_id, user_id, page_number, content, book_title, created_at 
FROM favorite_quotes 
WHERE user_id = 4 
ORDER BY created_at DESC;

-- 2. book_title이 NULL인 데이터 확인
SELECT '=== Records with NULL book_title ===' as info;
SELECT COUNT(*) as null_count 
FROM favorite_quotes 
WHERE book_title IS NULL OR book_title = '';

-- 3. 기존 데이터에 기본 책 제목 추가
UPDATE favorite_quotes 
SET book_title = '테스트 책' 
WHERE (book_title IS NULL OR book_title = '') AND user_id = 4;

-- 4. 업데이트 후 상태 확인
SELECT '=== Updated favorite_quotes data ===' as info;
SELECT fav_quote_id, user_id, page_number, content, book_title, created_at 
FROM favorite_quotes 
WHERE user_id = 4 
ORDER BY created_at DESC;

-- 마이그레이션 완료 확인
SELECT 'Migration completed successfully - book_title added to existing records' as status;
