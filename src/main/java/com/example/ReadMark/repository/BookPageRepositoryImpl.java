package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.QBookPage;
import com.example.ReadMark.model.entity.QBook;
import com.example.ReadMark.model.entity.QUser;
import com.example.ReadMark.model.entity.BookPage;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Repository
@Slf4j
public class BookPageRepositoryImpl implements BookPageRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    public BookPageRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }
    
    @Override
    public List<BookPage> findByUser_UserIdAndBook_BookIdOrderByPageNumberAsc(Long userId, Long bookId) {
        QBookPage bookPage = QBookPage.bookPage;
        
        return queryFactory
                .selectFrom(bookPage)
                .where(bookPage.user.userId.eq(userId)
                        .and(bookPage.book.bookId.eq(bookId)))
                .orderBy(bookPage.pageNumber.asc())
                .fetch();
    }
    
    @Override
    public Optional<BookPage> findByUser_UserIdAndBook_BookIdAndPageNumber(Long userId, Long bookId, Integer pageNumber) {
        QBookPage bookPage = QBookPage.bookPage;
        
        BookPage result = queryFactory
                .selectFrom(bookPage)
                .where(bookPage.user.userId.eq(userId)
                        .and(bookPage.book.bookId.eq(bookId))
                        .and(bookPage.pageNumber.eq(pageNumber)))
                .fetchOne();
        
        return Optional.ofNullable(result);
    }
    
    @Override
    public Long countByUserIdAndBookId(Long userId, Long bookId) {
        QBookPage bookPage = QBookPage.bookPage;
        
        return queryFactory
                .selectFrom(bookPage)
                .where(bookPage.user.userId.eq(userId)
                        .and(bookPage.book.bookId.eq(bookId)))
                .fetchCount();
    }
    
    @Override
    public List<BookPage> findByUser_UserIdAndBook_BookIdAndCapturedAtBetweenOrderByCapturedAtDesc(
            Long userId, Long bookId, LocalDateTime startDate, LocalDateTime endDate) {
        QBookPage bookPage = QBookPage.bookPage;
        
        return queryFactory
                .selectFrom(bookPage)
                .where(bookPage.user.userId.eq(userId)
                        .and(bookPage.book.bookId.eq(bookId))
                        .and(bookPage.capturedAt.between(startDate, endDate)))
                .orderBy(bookPage.capturedAt.desc())
                .fetch();
    }
    
    @Override
    public List<BookPage> findRecentPagesByUserIdAndBookId(Long userId, Long bookId) {
        QBookPage bookPage = QBookPage.bookPage;
        
        return queryFactory
                .selectFrom(bookPage)
                .where(bookPage.user.userId.eq(userId)
                        .and(bookPage.book.bookId.eq(bookId)))
                .orderBy(bookPage.capturedAt.desc())
                .fetch();
    }
    
    @Override
    public List<BookPage> findByPageRange(Long userId, Long bookId, Integer startPage, Integer endPage) {
        QBookPage bookPage = QBookPage.bookPage;
        
        return queryFactory
                .selectFrom(bookPage)
                .where(bookPage.user.userId.eq(userId)
                        .and(bookPage.book.bookId.eq(bookId))
                        .and(bookPage.pageNumber.between(startPage, endPage)))
                .orderBy(bookPage.pageNumber.asc())
                .fetch();
    }
    
    @Override
    public List<BookPage> findBookPagesWithUserAndBookInfo(Long userId, Long bookId) {
        QBookPage bookPage = QBookPage.bookPage;
        QBook book = QBook.book;
        QUser user = QUser.user;
        
        return queryFactory
                .selectFrom(bookPage)
                .join(bookPage.book, book).fetchJoin()
                .join(bookPage.user, user).fetchJoin()
                .where(bookPage.user.userId.eq(userId)
                        .and(bookPage.book.bookId.eq(bookId)))
                .orderBy(bookPage.pageNumber.asc())
                .fetch();
    }
    
    
    @Override
    public List<BookPage> findBookPagesByConfidenceRange(Long userId, Long bookId, Double minConfidence, Double maxConfidence) {
        QBookPage bookPage = QBookPage.bookPage;
        
        return queryFactory
                .selectFrom(bookPage)
                .where(bookPage.user.userId.eq(userId)
                        .and(bookPage.book.bookId.eq(bookId)))
                .orderBy(bookPage.capturedAt.desc())
                .fetch();
    }
    
    @Override
    public List<BookPage> findBookPagesWithPagination(Long userId, Long bookId, int offset, int limit) {
        QBookPage bookPage = QBookPage.bookPage;
        
        return queryFactory
                .selectFrom(bookPage)
                .where(bookPage.user.userId.eq(userId)
                        .and(bookPage.book.bookId.eq(bookId)))
                .orderBy(bookPage.capturedAt.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }
    
    @Override
    public List<BookPage> findByUser_UserIdAndCapturedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate) {
        QBookPage bookPage = QBookPage.bookPage;
        
        return queryFactory
                .selectFrom(bookPage)
                .where(bookPage.user.userId.eq(userId)
                        .and(bookPage.capturedAt.between(startDate, endDate)))
                .orderBy(bookPage.capturedAt.asc())
                .fetch();
    }
    
    @Override
    public int countByUserIdAndDate(Long userId, java.time.LocalDate date) {
        QBookPage bookPage = QBookPage.bookPage;
        
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        return Math.toIntExact(queryFactory
                .selectFrom(bookPage)
                .where(bookPage.user.userId.eq(userId)
                        .and(bookPage.capturedAt.between(startOfDay, endOfDay)))
                .fetchCount());
    }
    
    @Override
    public Long countDistinctReadingDaysByUserId(Long userId) {
        QBookPage bookPage = QBookPage.bookPage;
        
        DateTemplate<LocalDate> dateTemplate = Expressions.dateTemplate(LocalDate.class, "DATE({0})", bookPage.capturedAt);
        
        return queryFactory
                .select(dateTemplate.countDistinct())
                .from(bookPage)
                .where(bookPage.user.userId.eq(userId))
                .fetchOne();
    }
    
    @Override
    public List<java.time.LocalDate> findDistinctReadingDatesByUserId(Long userId) {
        QBookPage bookPage = QBookPage.bookPage;
        
        DateTemplate<LocalDate> dateTemplate = Expressions.dateTemplate(LocalDate.class, "DATE({0})", bookPage.capturedAt);
        
        return queryFactory
                .select(dateTemplate)
                .from(bookPage)
                .where(bookPage.user.userId.eq(userId))
                .distinct()
                .orderBy(dateTemplate.asc())
                .fetch();
    }
    
    @Override
    public int calculatePagesReadByUserIdAndDate(Long userId, java.time.LocalDate date) {
        QBookPage bookPage = QBookPage.bookPage;
        
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);
        
        // 해당 날짜의 모든 BookPage 조회 (책별로 그룹화)
        List<BookPage> pages = queryFactory
                .selectFrom(bookPage)
                .where(bookPage.user.userId.eq(userId)
                        .and(bookPage.capturedAt.between(startOfDay, endOfDay)))
                .orderBy(bookPage.book.bookId.asc(), bookPage.capturedAt.asc())
                .fetch();
        
        int totalPagesRead = 0;
        
        // 책별로 그룹화하여 계산
        Map<Long, List<BookPage>> pagesByBook = pages.stream()
                .collect(Collectors.groupingBy(page -> page.getBook().getBookId()));
        
        for (Map.Entry<Long, List<BookPage>> entry : pagesByBook.entrySet()) {
            List<BookPage> bookPages = entry.getValue();
            if (bookPages.size() >= 2) {
                // 시간순으로 정렬
                bookPages.sort((a, b) -> a.getCapturedAt().compareTo(b.getCapturedAt()));
                
                // 연속된 독서 구간별로 계산
                int bookPagesRead = 0;
                for (int i = 1; i < bookPages.size(); i++) {
                    int prevPage = bookPages.get(i - 1).getPageNumber();
                    int currentPage = bookPages.get(i).getPageNumber();
                    
                    // 연속된 페이지인 경우에만 계산 (역행 방지)
                    if (currentPage > prevPage) {
                        bookPagesRead += (currentPage - prevPage);
                    }
                }
                
                totalPagesRead += bookPagesRead;
            }
        }
        
        return totalPagesRead;
    }
    
    @Override
    public Long countStampDaysByUserId(Long userId) {
        QBookPage bookPage = QBookPage.bookPage;
        
        try {
            // 타입 변환 문제를 피하기 위해 다른 방식 사용
            // 모든 BookPage를 조회하여 Java에서 날짜별로 그룹화
            List<BookPage> allPages = queryFactory
                    .selectFrom(bookPage)
                    .where(bookPage.user.userId.eq(userId))
                    .orderBy(bookPage.capturedAt.asc())
                    .fetch();
            
            // 날짜별로 페이지 수 계산
            Map<LocalDate, Long> pagesByDate = allPages.stream()
                    .collect(Collectors.groupingBy(
                            page -> page.getCapturedAt().toLocalDate(),
                            Collectors.counting()
                    ));
            
            // 20페이지 이상인 날짜만 카운트
            long stampDays = pagesByDate.values().stream()
                    .mapToLong(count -> count >= 20 ? 1 : 0)
                    .sum();
            
            return stampDays;
        } catch (Exception e) {
            // 타입 변환 오류 발생 시 기본값 반환
            log.warn("BookPage 도장개수 조회 중 오류 발생: 사용자 {}", userId, e);
            return 0L;
        }
    }
}
