package com.example.ReadMark.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReadingSession is a Querydsl query type for ReadingSession
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReadingSession extends EntityPathBase<ReadingSession> {

    private static final long serialVersionUID = 956033654L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReadingSession readingSession = new QReadingSession("readingSession");

    public final QBook book;

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> endTime = createDateTime("endTime", java.time.LocalDateTime.class);

    public final NumberPath<Long> sessionId = createNumber("sessionId", Long.class);

    public final StringPath sessionNotes = createString("sessionNotes");

    public final DateTimePath<java.time.LocalDateTime> startTime = createDateTime("startTime", java.time.LocalDateTime.class);

    public final NumberPath<Integer> totalNumbersRead = createNumber("totalNumbersRead", Integer.class);

    public final NumberPath<Integer> totalPagesRead = createNumber("totalPagesRead", Integer.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final QUser user;

    public QReadingSession(String variable) {
        this(ReadingSession.class, forVariable(variable), INITS);
    }

    public QReadingSession(Path<? extends ReadingSession> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReadingSession(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReadingSession(PathMetadata metadata, PathInits inits) {
        this(ReadingSession.class, metadata, inits);
    }

    public QReadingSession(Class<? extends ReadingSession> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.book = inits.isInitialized("book") ? new QBook(forProperty("book")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

