package com.example.ReadMark.repository;

import com.example.ReadMark.entity.UserBook;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;

@Repository
public interface UserBookRepository extends JpaRepository<UserBook, Long> {
    long countByUser_UserIdAndStatus(Long userId, UserBook.Status status);
    List<UserBook> findByUser_UserIdAndStatus(Long userId, UserBook.Status status);
}
