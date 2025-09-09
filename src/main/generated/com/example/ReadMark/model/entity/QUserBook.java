package com.example.ReadMark.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUserBook is a Querydsl query type for UserBook
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUserBook extends EntityPathBase<UserBook> {

    private static final long serialVersionUID = -2054124960L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QUserBook userBook = new QUserBook("userBook");

    public final QBook book;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Integer> currentPage = createNumber("currentPage", Integer.class);

    public final EnumPath<UserBook.Status> status = createEnum("status", UserBook.Status.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final QUser user;

    public final NumberPath<Long> userBookId = createNumber("userBookId", Long.class);

    public QUserBook(String variable) {
        this(UserBook.class, forVariable(variable), INITS);
    }

    public QUserBook(Path<? extends UserBook> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QUserBook(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QUserBook(PathMetadata metadata, PathInits inits) {
        this(UserBook.class, metadata, inits);
    }

    public QUserBook(Class<? extends UserBook> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.book = inits.isInitialized("book") ? new QBook(forProperty("book")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

