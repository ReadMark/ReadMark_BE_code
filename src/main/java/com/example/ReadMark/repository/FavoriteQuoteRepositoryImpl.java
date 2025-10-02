package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.QFavoriteQuote;
import com.example.ReadMark.model.entity.QBook;
import com.example.ReadMark.model.entity.QUser;
import com.example.ReadMark.model.entity.FavoriteQuote;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FavoriteQuoteRepositoryImpl implements FavoriteQuoteRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    public FavoriteQuoteRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }
    
    @Override
    public List<FavoriteQuote> findByUserId(Long userId) {
        QFavoriteQuote favoriteQuote = QFavoriteQuote.favoriteQuote;
        
        return queryFactory
                .selectFrom(favoriteQuote)
                .where(favoriteQuote.user.userId.eq(userId))
                .orderBy(favoriteQuote.createdAt.desc())
                .fetch();
    }
    
    @Override
    public List<FavoriteQuote> findFavoriteQuotesWithBookInfo(Long userId) {
        QFavoriteQuote favoriteQuote = QFavoriteQuote.favoriteQuote;
        QUser user = QUser.user;
        
        return queryFactory
                .selectFrom(favoriteQuote)
                .join(favoriteQuote.user, user).fetchJoin()
                .where(favoriteQuote.user.userId.eq(userId))
                .orderBy(favoriteQuote.createdAt.desc())
                .fetch();
    }
    
    
    @Override
    public List<FavoriteQuote> findByUserIdAndPageRange(Long userId, int minPage, int maxPage) {
        QFavoriteQuote favoriteQuote = QFavoriteQuote.favoriteQuote;
        
        return queryFactory
                .selectFrom(favoriteQuote)
                .where(favoriteQuote.user.userId.eq(userId)
                        .and(favoriteQuote.pageNumber.between(minPage, maxPage)))
                .orderBy(favoriteQuote.pageNumber.asc())
                .fetch();
    }
    
    @Override
    public List<FavoriteQuote> findByUserIdAndContentContaining(Long userId, String content) {
        QFavoriteQuote favoriteQuote = QFavoriteQuote.favoriteQuote;
        
        return queryFactory
                .selectFrom(favoriteQuote)
                .where(favoriteQuote.user.userId.eq(userId)
                        .and(favoriteQuote.content.containsIgnoreCase(content)))
                .orderBy(favoriteQuote.createdAt.desc())
                .fetch();
    }
    
    @Override
    public List<FavoriteQuote> findFavoriteQuotesWithPagination(Long userId, int offset, int limit) {
        QFavoriteQuote favoriteQuote = QFavoriteQuote.favoriteQuote;
        
        return queryFactory
                .selectFrom(favoriteQuote)
                .where(favoriteQuote.user.userId.eq(userId))
                .orderBy(favoriteQuote.createdAt.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }
}
