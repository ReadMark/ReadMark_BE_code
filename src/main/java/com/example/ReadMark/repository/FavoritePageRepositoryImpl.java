package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.QFavoritePage;
import com.example.ReadMark.model.entity.QBook;
import com.example.ReadMark.model.entity.FavoritePage;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class FavoritePageRepositoryImpl implements FavoritePageRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    public FavoritePageRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }
    
    @Override
    public List<FavoritePage> findFavoritePagesWithBookInfo(Long userId) {
        QFavoritePage favoritePage = QFavoritePage.favoritePage;
        QBook book = QBook.book;
        
        return queryFactory
                .selectFrom(favoritePage)
                .leftJoin(favoritePage.book, book).fetchJoin()
                .where(favoritePage.user.userId.eq(userId))
                .orderBy(favoritePage.createdAt.desc())
                .fetch();
    }
}
