package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.FavoriteQuote;
import org.springframework.data.jpa.repository.JpaRepository;

public interface FavoriteQuoteRepository extends JpaRepository<FavoriteQuote, Long>, FavoriteQuoteRepositoryCustom {
}
