package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.QFavoriteQuote;
import com.example.ReadMark.model.entity.QBook;
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
    public List<FavoriteQuote> findFavoriteQuotesWithBookInfo(Long userId) {
        QFavoriteQuote favoriteQuote = QFavoriteQuote.favoriteQuote;
        QBook book = QBook.book;
        
        return queryFactory
                .selectFrom(favoriteQuote)
                .leftJoin(favoriteQuote.book, book).fetchJoin()
                .where(favoriteQuote.user.userId.eq(userId))
                .orderBy(favoriteQuote.createdAt.desc())
                .fetch();
    }
}
