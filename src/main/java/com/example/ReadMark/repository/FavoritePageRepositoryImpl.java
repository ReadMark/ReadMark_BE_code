package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.QFavoritePage;
import com.example.ReadMark.model.entity.QBook;
import com.example.ReadMark.model.entity.QUser;
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
    public List<FavoritePage> findByUserId(Long userId) {
        QFavoritePage favoritePage = QFavoritePage.favoritePage;
        
        return queryFactory
                .selectFrom(favoritePage)
                .where(favoritePage.user.userId.eq(userId))
                .orderBy(favoritePage.createdAt.desc())
                .fetch();
    }
    
    @Override
    public List<FavoritePage> findFavoritePagesWithBookInfo(Long userId) {
        QFavoritePage favoritePage = QFavoritePage.favoritePage;
        QBook book = QBook.book;
        QUser user = QUser.user;
        
        return queryFactory
                .selectFrom(favoritePage)
                .join(favoritePage.book, book).fetchJoin()
                .join(favoritePage.user, user).fetchJoin()
                .where(favoritePage.user.userId.eq(userId))
                .orderBy(favoritePage.createdAt.desc())
                .fetch();
    }
    
    @Override
    public List<FavoritePage> findByUserIdAndBookId(Long userId, Long bookId) {
        QFavoritePage favoritePage = QFavoritePage.favoritePage;
        
        return queryFactory
                .selectFrom(favoritePage)
                .where(favoritePage.user.userId.eq(userId)
                        .and(favoritePage.book.bookId.eq(bookId)))
                .orderBy(favoritePage.pageNumber.asc())
                .fetch();
    }
    
    @Override
    public List<FavoritePage> findByUserIdAndPageRange(Long userId, int minPage, int maxPage) {
        QFavoritePage favoritePage = QFavoritePage.favoritePage;
        
        return queryFactory
                .selectFrom(favoritePage)
                .where(favoritePage.user.userId.eq(userId)
                        .and(favoritePage.pageNumber.between(minPage, maxPage)))
                .orderBy(favoritePage.pageNumber.asc())
                .fetch();
    }
    
    @Override
    public List<FavoritePage> findFavoritePagesWithPagination(Long userId, int offset, int limit) {
        QFavoritePage favoritePage = QFavoritePage.favoritePage;
        
        return queryFactory
                .selectFrom(favoritePage)
                .where(favoritePage.user.userId.eq(userId))
                .orderBy(favoritePage.createdAt.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }
}
