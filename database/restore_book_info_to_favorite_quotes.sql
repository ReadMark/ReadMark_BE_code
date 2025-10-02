-- 즐겨찾기 문장에 책 제목과 표지 정보 복구를 위한 데이터베이스 마이그레이션 스크립트
-- 실행 전에 반드시 데이터베이스 백업을 수행하세요!

USE readmark;

-- 1. favorite_quotes 테이블에 book_title 컬럼 추가
ALTER TABLE favorite_quotes ADD COLUMN book_title VARCHAR(255);

-- 2. 테이블 구조 확인
DESCRIBE favorite_quotes;

-- 마이그레이션 완료 확인
SELECT 'Migration completed successfully - book_title column added' as status;
