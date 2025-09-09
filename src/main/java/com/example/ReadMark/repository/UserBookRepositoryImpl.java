package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.QUserBook;
import com.example.ReadMark.model.entity.QBook;
import com.example.ReadMark.model.entity.QUser;
import com.example.ReadMark.model.entity.UserBook;
import com.example.ReadMark.model.entity.UserBook.Status;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class UserBookRepositoryImpl implements UserBookRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    public UserBookRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }
    
    @Override
    public List<UserBook> findByUser_UserIdAndStatus(Long userId, Status status) {
        QUserBook userBook = QUserBook.userBook;
        
        return queryFactory
                .selectFrom(userBook)
                .where(userBook.user.userId.eq(userId)
                        .and(userBook.status.eq(status)))
                .orderBy(userBook.createdAt.desc())
                .fetch();
    }
    
    @Override
    public List<UserBook> findByUserId(Long userId) {
        QUserBook userBook = QUserBook.userBook;
        
        return queryFactory
                .selectFrom(userBook)
                .where(userBook.user.userId.eq(userId))
                .orderBy(userBook.createdAt.desc())
                .fetch();
    }
    
    @Override
    public long countByUserIdAndStatus(Long userId, Status status) {
        QUserBook userBook = QUserBook.userBook;
        
        return queryFactory
                .selectFrom(userBook)
                .where(userBook.user.userId.eq(userId)
                        .and(userBook.status.eq(status)))
                .fetchCount();
    }
    
    @Override
    public List<UserBook> findUserBooksWithBookInfo(Long userId) {
        QUserBook userBook = QUserBook.userBook;
        QBook book = QBook.book;
        QUser user = QUser.user;
        
        return queryFactory
                .selectFrom(userBook)
                .join(userBook.book, book).fetchJoin()
                .join(userBook.user, user).fetchJoin()
                .where(userBook.user.userId.eq(userId))
                .orderBy(userBook.createdAt.desc())
                .fetch();
    }
    
    @Override
    public List<UserBook> findUserBooksWithBookInfo(Long userId, Status status) {
        QUserBook userBook = QUserBook.userBook;
        QBook book = QBook.book;
        QUser user = QUser.user;
        
        return queryFactory
                .selectFrom(userBook)
                .join(userBook.book, book).fetchJoin()
                .join(userBook.user, user).fetchJoin()
                .where(userBook.user.userId.eq(userId)
                        .and(userBook.status.eq(status)))
                .orderBy(userBook.createdAt.desc())
                .fetch();
    }
    
    @Override
    public List<UserBook> findUserBooksByCurrentPageRange(Long userId, int minPage, int maxPage) {
        QUserBook userBook = QUserBook.userBook;
        
        return queryFactory
                .selectFrom(userBook)
                .where(userBook.user.userId.eq(userId)
                        .and(userBook.currentPage.between(minPage, maxPage)))
                .orderBy(userBook.currentPage.asc())
                .fetch();
    }
    
    @Override
    public List<UserBook> findUserBooksByStatusAndPageRange(Long userId, Status status, int minPage, int maxPage) {
        QUserBook userBook = QUserBook.userBook;
        
        return queryFactory
                .selectFrom(userBook)
                .where(userBook.user.userId.eq(userId)
                        .and(userBook.status.eq(status))
                        .and(userBook.currentPage.between(minPage, maxPage)))
                .orderBy(userBook.currentPage.asc())
                .fetch();
    }
    
    @Override
    public List<UserBook> findUserBooksWithPagination(Long userId, int offset, int limit) {
        QUserBook userBook = QUserBook.userBook;
        
        return queryFactory
                .selectFrom(userBook)
                .where(userBook.user.userId.eq(userId))
                .orderBy(userBook.createdAt.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }
}
