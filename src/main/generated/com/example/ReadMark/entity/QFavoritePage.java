package com.example.ReadMark.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.example.ReadMark.model.dto.entity.FavoritePage;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QFavoritePage is a Querydsl query type for FavoritePage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QFavoritePage extends EntityPathBase<FavoritePage> {

    private static final long serialVersionUID = -1390066660L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QFavoritePage favoritePage = new QFavoritePage("favoritePage");

    public final QBook book;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> favPageId = createNumber("favPageId", Long.class);

    public final NumberPath<Integer> pageNumber = createNumber("pageNumber", Integer.class);

    public final QUser user;

    public QFavoritePage(String variable) {
        this(FavoritePage.class, forVariable(variable), INITS);
    }

    public QFavoritePage(Path<? extends FavoritePage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QFavoritePage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QFavoritePage(PathMetadata metadata, PathInits inits) {
        this(FavoritePage.class, metadata, inits);
    }

    public QFavoritePage(Class<? extends FavoritePage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.book = inits.isInitialized("book") ? new QBook(forProperty("book")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

