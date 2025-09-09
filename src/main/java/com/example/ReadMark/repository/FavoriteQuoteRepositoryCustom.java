package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.FavoriteQuote;

import java.util.List;

public interface FavoriteQuoteRepositoryCustom {
    List<FavoriteQuote> findByUserId(Long userId);
    List<FavoriteQuote> findFavoriteQuotesWithBookInfo(Long userId);
    List<FavoriteQuote> findByUserIdAndBookId(Long userId, Long bookId);
    List<FavoriteQuote> findByUserIdAndPageRange(Long userId, int minPage, int maxPage);
    List<FavoriteQuote> findByUserIdAndContentContaining(Long userId, String content);
    List<FavoriteQuote> findFavoriteQuotesWithPagination(Long userId, int offset, int limit);
}
