package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.BookPage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookPageRepository extends JpaRepository<BookPage, Long>, BookPageRepositoryCustom {
    
    /**
     * 사용자의 모든 책 페이지를 조회합니다.
     */
    List<BookPage> findByUser_UserId(Long userId);
    
    /**
     * 사용자와 책별로 페이지를 촬영 시간 내림차순으로 조회합니다.
     */
    List<BookPage> findByUser_UserIdAndBook_BookIdOrderByCapturedAtDesc(Long userId, Long bookId);
}
