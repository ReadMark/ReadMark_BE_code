package com.example.ReadMark.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.example.ReadMark.model.dto.entity.Book;
import com.example.ReadMark.model.dto.entity.FavoritePage;
import com.example.ReadMark.model.dto.entity.FavoriteQuote;
import com.example.ReadMark.model.dto.entity.UserBook;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBook is a Querydsl query type for Book
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBook extends EntityPathBase<Book> {

    private static final long serialVersionUID = 1534422458L;

    public static final QBook book = new QBook("book");

    public final StringPath author = createString("author");

    public final NumberPath<Long> bookId = createNumber("bookId", Long.class);

    public final StringPath coverImageUrl = createString("coverImageUrl");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final ListPath<FavoritePage, QFavoritePage> favoritePages = this.<FavoritePage, QFavoritePage>createList("favoritePages", FavoritePage.class, QFavoritePage.class, PathInits.DIRECT2);

    public final ListPath<FavoriteQuote, QFavoriteQuote> favoriteQuotes = this.<FavoriteQuote, QFavoriteQuote>createList("favoriteQuotes", FavoriteQuote.class, QFavoriteQuote.class, PathInits.DIRECT2);

    public final DatePath<java.time.LocalDate> publishedAt = createDate("publishedAt", java.time.LocalDate.class);

    public final StringPath publisher = createString("publisher");

    public final StringPath title = createString("title");

    public final ListPath<UserBook, QUserBook> userBooks = this.<UserBook, QUserBook>createList("userBooks", UserBook.class, QUserBook.class, PathInits.DIRECT2);

    public QBook(String variable) {
        super(Book.class, forVariable(variable));
    }

    public QBook(Path<? extends Book> path) {
        super(path.getType(), path.getMetadata());
    }

    public QBook(PathMetadata metadata) {
        super(Book.class, metadata);
    }

}

