package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.FavoritePage;

import java.util.List;

public interface FavoritePageRepositoryCustom {
    List<FavoritePage> findFavoritePagesWithBookInfo(Long userId);
}
