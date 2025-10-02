-- 즐겨찾기 문장에 책 제목 컬럼 추가를 위한 데이터베이스 마이그레이션 스크립트
-- 실행 전에 반드시 데이터베이스 백업을 수행하세요!

-- 1. favorite_quotes 테이블에 book_title 컬럼 추가
ALTER TABLE favorite_quotes ADD COLUMN book_title VARCHAR(255);

-- 2. 기존 데이터 업데이트 (book_id가 있는 경우)
-- 주의: 이 쿼리는 book_id 컬럼이 아직 존재하는 경우에만 실행하세요
-- UPDATE favorite_quotes fq 
-- JOIN books b ON b.book_id = fq.book_id 
-- SET fq.book_title = b.title 
-- WHERE fq.book_id IS NOT NULL;

-- 마이그레이션 완료 확인
SELECT 'Migration completed successfully' as status;
