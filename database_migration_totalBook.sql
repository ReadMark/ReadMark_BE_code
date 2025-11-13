-- ============================================
-- total_pages를 total_book으로 변경하는 마이그레이션
-- ============================================

-- 1. books 테이블의 total_pages 컬럼을 total_book으로 변경
ALTER TABLE books 
CHANGE COLUMN total_pages total_book INT;

-- 2. 변경 확인 쿼리
SELECT 
    book_id,
    title,
    author,
    total_book,
    created_at
FROM books
LIMIT 10;

-- 3. 데이터 확인 (유저 1의 책 16)
SELECT 
    ub.user_id,
    ub.book_id,
    ub.current_page,
    b.total_book,
    b.title,
    b.author
FROM user_books ub
INNER JOIN books b ON ub.book_id = b.book_id
WHERE ub.user_id = 1 AND ub.book_id = 16;

-- 4. 업데이트 예시 (유저 1의 책 16)
UPDATE books 
SET total_book = 312 
WHERE book_id = 16;

UPDATE user_books 
SET current_page = 132 
WHERE user_id = 1 AND book_id = 16;

