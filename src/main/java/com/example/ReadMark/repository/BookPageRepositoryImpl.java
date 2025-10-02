package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.QBookPage;
import com.example.ReadMark.model.entity.QBook;
import com.example.ReadMark.model.entity.QUser;
import com.example.ReadMark.model.entity.BookPage;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
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
    public Long countStampDaysByUserId(Long userId) {
        QBookPage bookPage = QBookPage.bookPage;
        
        // MySQL only_full_group_by 호환을 위해 더 간단한 방식으로 수정
        DateTemplate<LocalDate> dateTemplate = Expressions.dateTemplate(LocalDate.class, "DATE({0})", bookPage.capturedAt);
        
        // 먼저 모든 날짜별 페이지 수를 조회
        List<LocalDate> allDates = queryFactory
                .select(dateTemplate)
                .from(bookPage)
                .where(bookPage.user.userId.eq(userId))
                .groupBy(dateTemplate)
                .fetch();
        
        // 각 날짜별로 페이지 수를 확인하여 20페이지 이상인 날짜만 카운트
        long stampDays = 0;
        for (LocalDate date : allDates) {
            long pageCount = queryFactory
                    .select(bookPage.count())
                    .from(bookPage)
                    .where(bookPage.user.userId.eq(userId)
                            .and(dateTemplate.eq(date)))
                    .fetchOne();
            
            if (pageCount >= 20) {
                stampDays++;
            }
        }
        
        return stampDays;
    }
}
