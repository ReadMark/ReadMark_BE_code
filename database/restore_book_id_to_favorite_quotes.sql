-- 즐겨찾기 문장에 book_id 컬럼 복구를 위한 데이터베이스 마이그레이션 스크립트
-- 실행 전에 반드시 데이터베이스 백업을 수행하세요!

USE readmark;

-- 1. favorite_quotes 테이블에 book_id 컬럼 추가
ALTER TABLE favorite_quotes ADD COLUMN book_id BIGINT;

-- 2. book_id 컬럼을 NOT NULL로 설정 (기본값 1로 설정)
UPDATE favorite_quotes SET book_id = 1 WHERE book_id IS NULL;
ALTER TABLE favorite_quotes MODIFY COLUMN book_id BIGINT NOT NULL;

-- 3. 외래키 제약조건 추가
ALTER TABLE favorite_quotes ADD CONSTRAINT fk_favorite_quotes_book 
    FOREIGN KEY (book_id) REFERENCES books(book_id);

-- 4. 테이블 구조 확인
DESCRIBE favorite_quotes;

-- 마이그레이션 완료 확인
SELECT 'Migration completed successfully - book_id column restored' as status;
