package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.QBookPage;
import com.example.ReadMark.model.entity.QBook;
import com.example.ReadMark.model.entity.QUser;
import com.example.ReadMark.model.entity.BookPage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

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
                        .and(bookPage.book.bookId.eq(bookId))
                        .and(bookPage.confidence.between(minConfidence, maxConfidence)))
                .orderBy(bookPage.confidence.desc())
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
}
