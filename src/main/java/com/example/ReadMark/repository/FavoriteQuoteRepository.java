package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.FavoriteQuote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface FavoriteQuoteRepository extends JpaRepository<FavoriteQuote, Long>, FavoriteQuoteRepositoryCustom {
    
    /**
     * 사용자, 책, 페이지 번호, 내용으로 즐겨찾기 문장 중복 확인
     */
    Optional<FavoriteQuote> findByUser_UserIdAndBook_BookIdAndPageNumberAndContent(Long userId, Long bookId, Integer pageNumber, String content);
}
