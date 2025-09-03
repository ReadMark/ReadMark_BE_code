package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.UserBook;
import com.example.ReadMark.model.entity.UserBook.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface UserBookRepository extends JpaRepository<UserBook, Long>, UserBookRepositoryCustom {
    
    List<UserBook> findByUserIdAndStatus(Long userId, Status status);
    
    @Query("SELECT ub FROM UserBook ub WHERE ub.user.userId = :userId")
    List<UserBook> findByUserId(@Param("userId") Long userId);
    
    @Query("SELECT COUNT(ub) FROM UserBook ub WHERE ub.user.userId = :userId AND ub.status = :status")
    long countByUserIdAndStatus(@Param("userId") Long userId, @Param("status") Status status);
}
