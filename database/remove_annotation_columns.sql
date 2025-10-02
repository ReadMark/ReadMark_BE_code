-- 어노테이션 칸 제거를 위한 데이터베이스 마이그레이션 스크립트
-- 실행 전에 반드시 데이터베이스 백업을 수행하세요!

-- 1. favorite_quotes 테이블에서 annotation 컬럼 제거
ALTER TABLE favorite_quotes DROP COLUMN annotation;

-- 2. favorite_pages 테이블에서 annotation 컬럼 제거  
ALTER TABLE favorite_pages DROP COLUMN annotation;

-- 마이그레이션 완료 확인
SELECT 'Migration completed successfully' as status;
