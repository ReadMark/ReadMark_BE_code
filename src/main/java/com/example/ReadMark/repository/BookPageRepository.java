package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.BookPage;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BookPageRepository extends JpaRepository<BookPage, Long>, BookPageRepositoryCustom {
}
