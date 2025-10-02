package com.example.ReadMark.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QStamp is a Querydsl query type for Stamp
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QStamp extends EntityPathBase<Stamp> {

    private static final long serialVersionUID = -1420955081L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QStamp stamp = new QStamp("stamp");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final DatePath<java.time.LocalDate> earnedDate = createDate("earnedDate", java.time.LocalDate.class);

    public final NumberPath<Integer> pagesRead = createNumber("pagesRead", Integer.class);

    public final NumberPath<Long> stampId = createNumber("stampId", Long.class);

    public final QUser user;

    public QStamp(String variable) {
        this(Stamp.class, forVariable(variable), INITS);
    }

    public QStamp(Path<? extends Stamp> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QStamp(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QStamp(PathMetadata metadata, PathInits inits) {
        this(Stamp.class, metadata, inits);
    }

    public QStamp(Class<? extends Stamp> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

