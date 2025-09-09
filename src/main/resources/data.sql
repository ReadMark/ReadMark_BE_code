-- 초기 테스트 데이터 삽입

-- 사용자 데이터
INSERT INTO users (name, email, password, created_at) VALUES 
('테스트 사용자', 'test@example.com', 'password123', CURRENT_TIMESTAMP),
('관리자', 'admin@example.com', 'admin123', CURRENT_TIMESTAMP);

-- 책 데이터
INSERT INTO books (title, author, publisher, created_at) VALUES 
('테스트 책 1', '작가 1', '출판사 1', CURRENT_TIMESTAMP),
('테스트 책 2', '작가 2', '출판사 2', CURRENT_TIMESTAMP);

-- 사용자-책 관계 데이터
INSERT INTO user_books (user_id, book_id, status, current_page, created_at) VALUES 
(1, 1, 'NOW_READ', 0, CURRENT_TIMESTAMP),
(1, 2, 'NOW_READ', 0, CURRENT_TIMESTAMP);
