package com.example.ReadMark.repository;

import com.example.ReadMark.entity.FavoritePage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FavoritePageRepository extends JpaRepository<FavoritePage, Long> {
    List<FavoritePage> findByUser_UserId(Long userId);
}
