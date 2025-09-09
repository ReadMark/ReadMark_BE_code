package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.UserBook;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserBookRepository extends JpaRepository<UserBook, Long>, UserBookRepositoryCustom {
}
