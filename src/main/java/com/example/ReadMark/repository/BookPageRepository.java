package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.BookPage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface BookPageRepository extends JpaRepository<BookPage, Long> {
    
    // 사용자와 책별로 페이지 조회
    List<BookPage> findByUserIdAndBookIdOrderByPageNumberAsc(Long userId, Long bookId);
    
    // 특정 페이지 번호 조회
    Optional<BookPage> findByUserIdAndBookIdAndPageNumber(Long userId, Long bookId, Integer pageNumber);
    
    // 사용자별 총 페이지 수
    @Query("SELECT COUNT(bp) FROM BookPage bp WHERE bp.user.id = :userId AND bp.book.id = :bookId")
    Long countByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);
    
    // 날짜 범위별 페이지 조회
    List<BookPage> findByUserIdAndBookIdAndCapturedAtBetweenOrderByCapturedAtDesc(
            Long userId, Long bookId, LocalDateTime startDate, LocalDateTime endDate);
    
    // 최근 페이지 조회
    @Query("SELECT bp FROM BookPage bp WHERE bp.user.id = :userId AND bp.book.id = :bookId " +
           "ORDER BY bp.capturedAt DESC")
    List<BookPage> findRecentPagesByUserIdAndBookId(@Param("userId") Long userId, @Param("bookId") Long bookId);
    
    // 페이지 번호 범위로 조회
    @Query("SELECT bp FROM BookPage bp WHERE bp.user.id = :userId AND bp.book.id = :bookId " +
           "AND bp.pageNumber BETWEEN :startPage AND :endPage ORDER BY bp.pageNumber")
    List<BookPage> findByPageRange(@Param("userId") Long userId, @Param("bookId") Long bookId, 
                                   @Param("startPage") Integer startPage, @Param("endPage") Integer endPage);
}
