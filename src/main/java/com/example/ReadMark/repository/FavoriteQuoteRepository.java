package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.FavoriteQuote;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface FavoriteQuoteRepository extends JpaRepository<FavoriteQuote, Long>, FavoriteQuoteRepositoryCustom {
    
    @Query("SELECT fq FROM FavoriteQuote fq WHERE fq.user.userId = :userId")
    List<FavoriteQuote> findByUserId(@Param("userId") Long userId);
}
