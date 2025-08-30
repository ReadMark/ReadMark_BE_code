package com.example.ReadMark.repository;

import com.example.ReadMark.entity.FavoriteQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface FavoriteQuoteRepository extends JpaRepository<FavoriteQuote, Long> {
    List<FavoriteQuote> findByUser_UserId(Long userId);
}
