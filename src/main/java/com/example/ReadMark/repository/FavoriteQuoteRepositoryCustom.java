package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.FavoriteQuote;

import java.util.List;

public interface FavoriteQuoteRepositoryCustom {
    List<FavoriteQuote> findFavoriteQuotesWithBookInfo(Long userId);
}
