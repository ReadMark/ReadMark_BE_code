package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.QUser;
import com.example.ReadMark.model.entity.QUserBook;
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
}
