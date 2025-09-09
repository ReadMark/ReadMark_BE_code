package com.example.ReadMark.repository;

import com.example.ReadMark.model.entity.QBook;
import com.example.ReadMark.model.entity.Book;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
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
    
    @Override
    public List<Book> findBooksByAuthor(String author) {
        QBook book = QBook.book;
        
        return queryFactory
                .selectFrom(book)
                .where(book.author.containsIgnoreCase(author))
                .orderBy(book.title.asc())
                .fetch();
    }
    
    @Override
    public List<Book> findBooksByPublisher(String publisher) {
        QBook book = QBook.book;
        
        return queryFactory
                .selectFrom(book)
                .where(book.publisher.containsIgnoreCase(publisher))
                .orderBy(book.title.asc())
                .fetch();
    }
    
    @Override
    public List<Book> findBooksByPublishedDateRange(LocalDate startDate, LocalDate endDate) {
        QBook book = QBook.book;
        
        return queryFactory
                .selectFrom(book)
                .where(book.publishedAt.between(startDate, endDate))
                .orderBy(book.publishedAt.desc())
                .fetch();
    }
    
    @Override
    public List<Book> findBooksByTitleContaining(String title) {
        QBook book = QBook.book;
        
        return queryFactory
                .selectFrom(book)
                .where(book.title.containsIgnoreCase(title))
                .orderBy(book.title.asc())
                .fetch();
    }
    
    @Override
    public List<Book> findBooksWithPagination(int offset, int limit) {
        QBook book = QBook.book;
        
        return queryFactory
                .selectFrom(book)
                .orderBy(book.createdAt.desc())
                .offset(offset)
                .limit(limit)
                .fetch();
    }
}
