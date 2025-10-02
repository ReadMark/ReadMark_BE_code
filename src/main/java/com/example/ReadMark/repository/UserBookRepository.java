package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.UserBook;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserBookRepository extends JpaRepository<UserBook, Long>, UserBookRepositoryCustom {
    
    /**
     * 사용자와 책으로 UserBook 관계를 조회합니다.
     */
    Optional<UserBook> findByUser_UserIdAndBook_BookId(Long userId, Long bookId);
    
    /**
     * 사용자의 즐겨찾기 책 목록을 조회합니다.
     */
    List<UserBook> findByUser_UserIdAndFavoriteTrue(Long userId);
    
    /**
     * 사용자의 모든 UserBook 관계를 조회합니다.
     */
    List<UserBook> findByUser_UserId(Long userId);
}
