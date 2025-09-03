-- QueryDSL에서 사용할 수 있는 복잡한 MySQL 쿼리 예제들
-- 이 쿼리들은 QueryDSL로 변환하여 사용할 수 있습니다.

USE readmark;

-- 1. 사용자별 독서 통계 및 책 상태별 개수 조회
-- QueryDSL: UserRepositoryImpl.findByEmailWithUserBooks()와 유사
SELECT 
    u.user_id,
    u.name,
    u.email,
    COUNT(DISTINCT rl.read_date) as total_reading_days,
    SUM(rl.pages_read) as total_pages_read,
    COUNT(CASE WHEN ub.status = 'NOW_READ' THEN 1 END) as now_reading_count,
    COUNT(CASE WHEN ub.status = 'WANNA_READ' THEN 1 END) as wanna_read_count,
    COUNT(CASE WHEN ub.status = 'READ_DONE' THEN 1 END) as read_done_count
FROM User u
LEFT JOIN Reading_Log rl ON u.user_id = rl.user_id
LEFT JOIN User_Book ub ON u.user_id = ub.user_id
WHERE u.email = 'reader1@example.com'
GROUP BY u.user_id, u.name, u.email;

-- 2. 사용자별 책 목록 조회 (책 정보 포함)
-- QueryDSL: UserBookRepositoryImpl.findUserBooksWithBookInfo()와 유사
SELECT 
    ub.user_book_id,
    ub.status,
    ub.current_page,
    ub.created_at,
    ub.updated_at,
    b.book_id,
    b.title,
    b.author,
    b.publisher,
    b.cover_image_url,
    b.published_at
FROM User_Book ub
LEFT JOIN Book b ON ub.book_id = b.book_id
WHERE ub.user_id = 1
ORDER BY ub.created_at DESC;

-- 3. 상태별 책 목록 조회
-- QueryDSL: UserBookRepositoryImpl.findUserBooksWithBookInfo(userId, status)와 유사
SELECT 
    ub.user_book_id,
    ub.current_page,
    ub.created_at,
    b.title,
    b.author,
    b.publisher
FROM User_Book ub
LEFT JOIN Book b ON ub.book_id = b.book_id
WHERE ub.user_id = 1 AND ub.status = 'NOW_READ'
ORDER BY ub.createdAt DESC;

-- 4. 기간별 독서 기록 조회
-- QueryDSL: ReadingLogRepositoryImpl.findReadingLogsWithUserInfo()와 유사
SELECT 
    rl.log_id,
    rl.read_date,
    rl.pages_read,
    rl.created_at,
    u.user_id,
    u.name
FROM Reading_Log rl
LEFT JOIN User u ON rl.user_id = u.user_id
WHERE rl.user_id = 1 
  AND rl.read_date BETWEEN '2024-01-01' AND '2024-12-31'
ORDER BY rl.read_date DESC;

-- 5. 연속 독서일 계산 (복잡한 쿼리)
-- QueryDSL: ReadingLogRepositoryImpl.getMaxConsecutiveReadingDays()와 유사
WITH RECURSIVE consecutive_dates AS (
    SELECT 
        read_date,
        read_date as start_date,
        1 as consecutive_count
    FROM Reading_Log 
    WHERE user_id = 1
    
    UNION ALL
    
    SELECT 
        rl.read_date,
        cd.start_date,
        cd.consecutive_count + 1
    FROM Reading_Log rl
    JOIN consecutive_dates cd ON rl.read_date = DATE_ADD(cd.read_date, INTERVAL 1 DAY)
    WHERE rl.user_id = 1
)
SELECT MAX(consecutive_count) as max_consecutive_days
FROM consecutive_dates;

-- 6. 책 검색 (제목 또는 저자로 검색)
-- QueryDSL: BookRepositoryImpl.findBooksByKeyword()와 유사
SELECT 
    book_id,
    title,
    author,
    publisher,
    cover_image_url,
    published_at
FROM Book
WHERE title LIKE '%트로피컬%' 
   OR author LIKE '%조예은%'
   OR title LIKE '%1984%'
   OR author LIKE '%오웰%'
ORDER BY title ASC;

-- 7. 즐겨찾기한 페이지와 책 정보 조회
-- QueryDSL: FavoritePageRepositoryImpl.findFavoritePagesWithBookInfo()와 유사
SELECT 
    fp.fav_page_id,
    fp.page_number,
    fp.created_at,
    b.book_id,
    b.title,
    b.author,
    b.publisher,
    b.cover_image_url
FROM Favorite_Page fp
LEFT JOIN Book b ON fp.book_id = b.book_id
WHERE fp.user_id = 1
ORDER BY fp.created_at DESC;

-- 8. 즐겨찾기한 문장과 책 정보 조회
-- QueryDSL: FavoriteQuoteRepositoryImpl.findFavoriteQuotesWithBookInfo()와 유사
SELECT 
    fq.fav_quote_id,
    fq.page_number,
    fq.content,
    fq.created_at,
    b.book_id,
    b.title,
    b.author,
    b.publisher
FROM Favorite_Quote fq
LEFT JOIN Book b ON fq.book_id = b.book_id
WHERE fq.user_id = 1
ORDER BY fq.created_at DESC;

-- 9. 오늘 독서 페이지 수 조회
-- QueryDSL: ReadingLogRepository.sumPagesReadByUserIdAndDate()와 유사
SELECT 
    COALESCE(SUM(pages_read), 0) as today_pages_read
FROM Reading_Log
WHERE user_id = 1 AND read_date = CURDATE();

-- 10. 사용자별 독서 패턴 분석
SELECT 
    u.user_id,
    u.name,
    COUNT(DISTINCT rl.read_date) as total_reading_days,
    COUNT(DISTINCT DATE_FORMAT(rl.read_date, '%Y-%m')) as total_reading_months,
    AVG(rl.pages_read) as avg_pages_per_day,
    MAX(rl.pages_read) as max_pages_in_day,
    MIN(rl.read_date) as first_reading_date,
    MAX(rl.read_date) as last_reading_date,
    DATEDIFF(MAX(rl.read_date), MIN(rl.read_date)) + 1 as reading_period_days
FROM User u
LEFT JOIN Reading_Log rl ON u.user_id = rl.user_id
WHERE u.user_id = 1
GROUP BY u.user_id, u.name;

-- 11. 월별 독서 통계
SELECT 
    DATE_FORMAT(rl.read_date, '%Y-%m') as month,
    COUNT(DISTINCT rl.user_id) as active_readers,
    SUM(rl.pages_read) as total_pages_read,
    AVG(rl.pages_read) as avg_pages_per_reader,
    COUNT(DISTINCT rl.read_date) as reading_days
FROM Reading_Log rl
WHERE rl.read_date >= DATE_SUB(CURDATE(), INTERVAL 12 MONTH)
GROUP BY DATE_FORMAT(rl.read_date, '%Y-%m')
ORDER BY month DESC;

-- 12. 인기 책 조회 (사용자들이 많이 읽는 책)
SELECT 
    b.book_id,
    b.title,
    b.author,
    b.publisher,
    COUNT(DISTINCT ub.user_id) as total_readers,
    COUNT(CASE WHEN ub.status = 'NOW_READ' THEN 1 END) as currently_reading,
    COUNT(CASE WHEN ub.status = 'READ_DONE' THEN 1 END) as completed_readers,
    COUNT(CASE WHEN ub.status = 'WANNA_READ' THEN 1 END) as want_to_read
FROM Book b
LEFT JOIN User_Book ub ON b.book_id = ub.book_id
GROUP BY b.book_id, b.title, b.author, b.publisher
ORDER BY total_readers DESC, completed_readers DESC;

-- 13. 사용자별 독서 시간대 분석 (가정: 독서 시간이 있다면)
-- 실제로는 Reading_Log 테이블에 reading_time 컬럼을 추가해야 함
SELECT 
    u.user_id,
    u.name,
    COUNT(rl.log_id) as total_reading_sessions,
    AVG(rl.pages_read) as avg_pages_per_session
FROM User u
LEFT JOIN Reading_Log rl ON u.user_id = rl.user_id
GROUP BY u.user_id, u.name
ORDER BY total_reading_sessions DESC;

-- 14. 연속 독서일 계산 (더 정확한 방법)
SELECT 
    user_id,
    MAX(consecutive_days) as max_consecutive_days
FROM (
    SELECT 
        user_id,
        read_date,
        @consecutive := IF(
            @prev_user = user_id AND DATEDIFF(read_date, @prev_date) = 1,
            @consecutive + 1,
            1
        ) as consecutive_days,
        @prev_user := user_id,
        @prev_date := read_date
    FROM (
        SELECT user_id, read_date
        FROM Reading_Log
        WHERE user_id = 1
        ORDER BY user_id, read_date
    ) ordered_logs,
    (SELECT @consecutive := 0, @prev_user := NULL, @prev_date := NULL) vars
) consecutive_calc
GROUP BY user_id;

-- 15. 사용자별 독서 목표 달성률 (가정: 월별 목표 페이지가 있다면)
-- 실제로는 User_Goal 테이블을 추가해야 함
SELECT 
    u.user_id,
    u.name,
    COUNT(DISTINCT rl.read_date) as actual_reading_days,
    SUM(rl.pages_read) as actual_pages_read,
    -- 목표 페이지가 있다면: (SUM(rl.pages_read) / goal_pages) * 100 as achievement_rate
    '목표 설정 필요' as achievement_rate
FROM User u
LEFT JOIN Reading_Log rl ON u.user_id = rl.user_id
WHERE rl.read_date >= DATE_FORMAT(CURDATE(), '%Y-%m-01')
GROUP BY u.user_id, u.name;

-- 16. 책별 독서 진행률
SELECT 
    b.title,
    b.author,
    ub.current_page,
    -- 책의 총 페이지 수가 있다면: (ub.current_page / b.total_pages) * 100 as progress_rate
    '총 페이지 수 정보 필요' as progress_rate,
    ub.status,
    u.name as reader_name
FROM User_Book ub
JOIN Book b ON ub.book_id = b.book_id
JOIN User u ON ub.user_id = u.user_id
WHERE ub.status = 'NOW_READ'
ORDER BY ub.current_page DESC;

-- 17. 독서 습관 분석 (요일별, 시간대별)
SELECT 
    DAYNAME(rl.read_date) as day_of_week,
    COUNT(*) as reading_sessions,
    AVG(rl.pages_read) as avg_pages,
    SUM(rl.pages_read) as total_pages
FROM Reading_Log rl
WHERE rl.user_id = 1
  AND rl.read_date >= DATE_SUB(CURDATE(), INTERVAL 30 DAY)
GROUP BY DAYNAME(rl.read_date)
ORDER BY FIELD(DAYNAME(rl.read_date), 'Monday', 'Tuesday', 'Wednesday', 'Thursday', 'Friday', 'Saturday', 'Sunday');

-- 18. 사용자별 독서 랭킹
SELECT 
    u.user_id,
    u.name,
    COUNT(DISTINCT rl.read_date) as total_reading_days,
    SUM(rl.pages_read) as total_pages_read,
    RANK() OVER (ORDER BY SUM(rl.pages_read) DESC) as ranking
FROM User u
LEFT JOIN Reading_Log rl ON u.user_id = rl.user_id
GROUP BY u.user_id, u.name
ORDER BY total_pages_read DESC;

-- 19. 책 상태 변경 이력 (가정: User_Book_History 테이블이 있다면)
-- 실제로는 히스토리 테이블을 추가해야 함
SELECT 
    '히스토리 테이블 필요' as message,
    'User_Book_History 테이블을 생성하여 상태 변경 이력을 추적해야 함' as description;

-- 20. 데이터베이스 성능 모니터링
SELECT 
    TABLE_NAME,
    TABLE_ROWS,
    ROUND(DATA_LENGTH / 1024 / 1024, 2) as data_size_mb,
    ROUND(INDEX_LENGTH / 1024 / 1024, 2) as index_size_mb,
    ROUND((DATA_LENGTH + INDEX_LENGTH) / 1024 / 1024, 2) as total_size_mb
FROM information_schema.TABLES 
WHERE TABLE_SCHEMA = 'readmark'
ORDER BY (DATA_LENGTH + INDEX_LENGTH) DESC;

-- QueryDSL 변환 힌트
SELECT 
    'QueryDSL 변환 시 참고사항:' as info,
    '1. 복잡한 JOIN은 fetchJoin() 사용' as tip1,
    '2. 서브쿼리는 별도 메서드로 분리' as tip2,
    '3. 동적 쿼리는 BooleanBuilder 사용' as tip3,
    '4. 페이징은 offset()과 limit() 사용' as tip4,
    '5. 정렬은 orderBy() 사용' as tip5;

