package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.QUserBook;
import com.example.ReadMark.model.entity.QBook;
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
    public List<UserBook> findUserBooksWithBookInfo(Long userId, Status status) {
        QUserBook userBook = QUserBook.userBook;
        QBook book = QBook.book;
        
        return queryFactory
                .selectFrom(userBook)
                .leftJoin(userBook.book, book).fetchJoin()
                .where(userBook.user.userId.eq(userId)
                        .and(userBook.status.eq(status)))
                .orderBy(userBook.createdAt.desc())
                .fetch();
    }
    
    @Override
    public List<UserBook> findUserBooksWithBookInfo(Long userId) {
        QUserBook userBook = QUserBook.userBook;
        QBook book = QBook.book;
        
        return queryFactory
                .selectFrom(userBook)
                .leftJoin(userBook.book, book).fetchJoin()
                .where(userBook.user.userId.eq(userId))
                .orderBy(userBook.createdAt.desc())
                .fetch();
    }
}
