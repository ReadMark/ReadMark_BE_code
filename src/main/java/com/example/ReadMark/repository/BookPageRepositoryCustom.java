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
    List<BookPage> findBookPagesByConfidenceRange(Long userId, Long bookId, Double minConfidence, Double maxConfidence);
    List<BookPage> findBookPagesWithPagination(Long userId, Long bookId, int offset, int limit);
    
    // 캘린더용 메서드 추가
    List<BookPage> findByUser_UserIdAndCapturedAtBetween(Long userId, LocalDateTime startDate, LocalDateTime endDate);
    
    // 일별 페이지 수 계산용 메서드 (기존 - 사진 개수)
    int countByUserIdAndDate(Long userId, java.time.LocalDate date);
    
    // 실제 읽은 페이지 수 계산용 메서드 (새로운 - 페이지 번호 차이)
    int calculatePagesReadByUserIdAndDate(Long userId, java.time.LocalDate date);
    
    // 총 읽은 날 수 계산용 메서드 (BookPage 기반)
    Long countDistinctReadingDaysByUserId(Long userId);
    
    // 고유한 독서 날짜 목록 조회 (BookPage 기반)
    List<java.time.LocalDate> findDistinctReadingDatesByUserId(Long userId);
    
    // 도장개수 계산 (20페이지 이상 읽은 날 수) - BookPage 기반
    Long countStampDaysByUserId(Long userId);
}
