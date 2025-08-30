package com.example.ReadMark.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFavoriteQuote is a Querydsl query type for FavoriteQuote
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFavoriteQuote extends EntityPathBase<FavoriteQuote> {

    private static final long serialVersionUID = -140865905L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFavoriteQuote favoriteQuote = new QFavoriteQuote("favoriteQuote");

    public final QBook book;

    public final StringPath content = createString("content");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> favQuoteId = createNumber("favQuoteId", Long.class);

    public final NumberPath<Integer> pageNumber = createNumber("pageNumber", Integer.class);

    public final QUser user;

    public QFavoriteQuote(String variable) {
        this(FavoriteQuote.class, forVariable(variable), INITS);
    }

    public QFavoriteQuote(Path<? extends FavoriteQuote> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFavoriteQuote(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFavoriteQuote(PathMetadata metadata, PathInits inits) {
        this(FavoriteQuote.class, metadata, inits);
    }

    public QFavoriteQuote(Class<? extends FavoriteQuote> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.book = inits.isInitialized("book") ? new QBook(forProperty("book")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

