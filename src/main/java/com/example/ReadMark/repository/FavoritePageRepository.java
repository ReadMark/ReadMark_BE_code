package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.FavoritePage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoritePageRepository extends JpaRepository<FavoritePage, Long>, FavoritePageRepositoryCustom {
}
