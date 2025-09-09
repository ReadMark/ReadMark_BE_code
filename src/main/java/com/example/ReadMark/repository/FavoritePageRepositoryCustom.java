package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.FavoritePage;

import java.util.List;

public interface FavoritePageRepositoryCustom {
    List<FavoritePage> findByUserId(Long userId);
    List<FavoritePage> findFavoritePagesWithBookInfo(Long userId);
    List<FavoritePage> findByUserIdAndBookId(Long userId, Long bookId);
    List<FavoritePage> findByUserIdAndPageRange(Long userId, int minPage, int maxPage);
    List<FavoritePage> findFavoritePagesWithPagination(Long userId, int offset, int limit);
}
