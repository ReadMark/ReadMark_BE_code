package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.FavoritePage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoritePageRepository extends JpaRepository<FavoritePage, Long>, FavoritePageRepositoryCustom {
    
    /**
     * 사용자, 책, 페이지 번호로 즐겨찾기 페이지 중복 확인
     */
    Optional<FavoritePage> findByUser_UserIdAndBook_BookIdAndPageNumber(Long userId, Long bookId, int pageNumber);
}
