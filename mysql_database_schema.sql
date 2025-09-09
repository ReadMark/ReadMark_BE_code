-- ReadMark 프로젝트 MySQL 데이터베이스 스키마
-- 이 스크립트는 ReadMark 백엔드 애플리케이션과 호환되는 MySQL 데이터베이스를 생성합니다.

-- 데이터베이스 생성
CREATE DATABASE IF NOT EXISTS readmark 
CHARACTER SET utf8mb4 
COLLATE utf8mb4_unicode_ci;

USE readmark;

-- 1. 사용자 테이블 (users)
CREATE TABLE IF NOT EXISTS users (
    user_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    INDEX idx_email (email),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. 책 테이블 (books)
CREATE TABLE IF NOT EXISTS books (
    book_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(500) NOT NULL,
    author VARCHAR(255),
    publisher VARCHAR(255),
    cover_image_url VARCHAR(1000),
    published_at DATE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    INDEX idx_title (title),
    INDEX idx_author (author),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. 사용자-책 관계 테이블 (user_books)
CREATE TABLE IF NOT EXISTS user_books (
    user_book_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    status ENUM('NOW_READ', 'WANNA_READ', 'READ_DONE') DEFAULT 'WANNA_READ',
    current_page INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_book (user_id, book_id),
    INDEX idx_user_status (user_id, status),
    INDEX idx_book_status (book_id, status)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 4. 책 페이지 테이블 (book_pages)
CREATE TABLE IF NOT EXISTS book_pages (
    page_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    book_id BIGINT NOT NULL,
    user_id BIGINT NOT NULL,
    page_number INT NOT NULL,
    extracted_text TEXT,
    image_url VARCHAR(500),
    image_data LONGBLOB,
    captured_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    confidence DOUBLE,
    device_info VARCHAR(100),
    language VARCHAR(20),
    number_count INT,
    text_quality DOUBLE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    INDEX idx_book_page (book_id, page_number),
    INDEX idx_user_book_page (user_id, book_id, page_number),
    INDEX idx_captured_at (captured_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 5. 독서 세션 테이블 (reading_sessions)
CREATE TABLE IF NOT EXISTS reading_sessions (
    session_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    start_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    end_time TIMESTAMP NULL,
    total_pages_read INT DEFAULT 0,
    total_numbers_read INT DEFAULT 0,
    session_notes TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    INDEX idx_user_session (user_id, start_time),
    INDEX idx_book_session (book_id, start_time),
    INDEX idx_start_time (start_time)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 6. 독서 로그 테이블 (reading_logs)
CREATE TABLE IF NOT EXISTS reading_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    read_date DATE NOT NULL,
    pages_read INT DEFAULT 0,
    reading_duration_minutes INT DEFAULT 0,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_date (user_id, read_date),
    INDEX idx_user_date (user_id, read_date),
    INDEX idx_read_date (read_date)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 7. 즐겨찾기 페이지 테이블 (favorite_pages)
CREATE TABLE IF NOT EXISTS favorite_pages (
    fav_page_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    page_number INT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    UNIQUE KEY unique_user_book_page (user_id, book_id, page_number),
    INDEX idx_user_favorites (user_id),
    INDEX idx_book_favorites (book_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 8. 즐겨찾기 인용구 테이블 (favorite_quotes)
CREATE TABLE IF NOT EXISTS favorite_quotes (
    fav_quote_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    page_number INT,
    content TEXT NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (user_id) REFERENCES users(user_id) ON DELETE CASCADE,
    FOREIGN KEY (book_id) REFERENCES books(book_id) ON DELETE CASCADE,
    INDEX idx_user_quotes (user_id),
    INDEX idx_book_quotes (book_id),
    INDEX idx_created_at (created_at)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 초기 데이터 삽입 (선택사항)
-- 테스트용 사용자 데이터
INSERT IGNORE INTO users (name, email, password) VALUES 
('테스트 사용자', 'test@readmark.com', '$2a$10$example_hashed_password'),
('관리자', 'admin@readmark.com', '$2a$10$example_hashed_password');

-- 테스트용 책 데이터
INSERT IGNORE INTO books (title, author, publisher, published_at) VALUES 
('테스트 책 1', '작가 1', '출판사 1', '2023-01-01'),
('테스트 책 2', '작가 2', '출판사 2', '2023-02-01');

-- 권한 설정 (필요시)
-- GRANT ALL PRIVILEGES ON readmark.* TO 'readmark'@'%';
-- FLUSH PRIVILEGES;

-- 데이터베이스 생성 완료 메시지
SELECT 'ReadMark 데이터베이스가 성공적으로 생성되었습니다!' as message;

