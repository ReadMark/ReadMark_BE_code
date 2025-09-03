package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.QBook;
import com.example.ReadMark.model.entity.Book;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public class BookRepositoryImpl implements BookRepositoryCustom {
    
    private final JPAQueryFactory queryFactory;
    
    public BookRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }
    
    @Override
    public List<Book> findBooksByKeyword(String keyword) {
        QBook book = QBook.book;
        
        return queryFactory
                .selectFrom(book)
                .where(book.title.containsIgnoreCase(keyword)
                        .or(book.author.containsIgnoreCase(keyword)))
                .orderBy(book.title.asc())
                .fetch();
    }
}
