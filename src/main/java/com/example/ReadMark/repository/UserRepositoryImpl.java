package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.QUser;
import com.example.ReadMark.model.entity.QUserBook;
import com.example.ReadMark.model.entity.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class UserRepositoryImpl implements UserRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    public UserRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }
    
    @Override
    public Optional<User> findByEmailWithUserBooks(String email) {
        QUser user = QUser.user;
        QUserBook userBook = QUserBook.userBook;
        
        User result = queryFactory
                .selectFrom(user)
                .leftJoin(user.userBooks, userBook).fetchJoin()
                .where(user.email.eq(email))
                .fetchOne();
        
        return Optional.ofNullable(result);
    }
    
    @Override
    public Optional<User> findByEmailAndPassword(String email, String password) {
        QUser user = QUser.user;
        
        User result = queryFactory
                .selectFrom(user)
                .where(user.email.eq(email)
                        .and(user.password.eq(password)))
                .fetchOne();
        
        return Optional.ofNullable(result);
    }
    
    @Override
    public Optional<User> findUserWithAllRelations(Long userId) {
        QUser user = QUser.user;
        QUserBook userBook = QUserBook.userBook;
        
        User result = queryFactory
                .selectFrom(user)
                .leftJoin(user.userBooks, userBook).fetchJoin()
                .where(user.userId.eq(userId))
                .fetchOne();
        
        return Optional.ofNullable(result);
    }
}
