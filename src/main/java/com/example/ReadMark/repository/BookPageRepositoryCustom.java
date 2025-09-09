package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.BookPage;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

public interface BookPageRepositoryCustom {
    List<BookPage> findByUser_UserIdAndBook_BookIdOrderByPageNumberAsc(Long userId, Long bookId);
    Optional<BookPage> findByUser_UserIdAndBook_BookIdAndPageNumber(Long userId, Long bookId, Integer pageNumber);
    Long countByUserIdAndBookId(Long userId, Long bookId);
    List<BookPage> findByUser_UserIdAndBook_BookIdAndCapturedAtBetweenOrderByCapturedAtDesc(
            Long userId, Long bookId, LocalDateTime startDate, LocalDateTime endDate);
    List<BookPage> findRecentPagesByUserIdAndBookId(Long userId, Long bookId);
    List<BookPage> findByPageRange(Long userId, Long bookId, Integer startPage, Integer endPage);
    List<BookPage> findBookPagesWithUserAndBookInfo(Long userId, Long bookId);
    List<BookPage> findBookPagesByQualityRange(Long userId, Long bookId, Double minQuality, Double maxQuality);
    List<BookPage> findBookPagesByConfidenceRange(Long userId, Long bookId, Double minConfidence, Double maxConfidence);
    List<BookPage> findBookPagesWithPagination(Long userId, Long bookId, int offset, int limit);
    
    // 캘린더용 메서드 추가
    List<BookPage> findByUser_UserIdAndCapturedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
}
