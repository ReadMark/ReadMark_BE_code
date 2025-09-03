package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.FavoritePage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoritePageRepository extends JpaRepository<FavoritePage, Long>, FavoritePageRepositoryCustom {
    
    @Query("SELECT fp FROM FavoritePage fp WHERE fp.user.userId = :userId")
    List<FavoritePage> findByUserId(@Param("userId") Long userId);
}
