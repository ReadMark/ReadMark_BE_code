package com.example.ReadMark.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.example.ReadMark.model.dto.entity.ReadingLog;
import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QReadingLog is a Querydsl query type for ReadingLog
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QReadingLog extends EntityPathBase<ReadingLog> {

    private static final long serialVersionUID = -1192209623L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QReadingLog readingLog = new QReadingLog("readingLog");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> logId = createNumber("logId", Long.class);

    public final NumberPath<Integer> pagesRead = createNumber("pagesRead", Integer.class);

    public final DatePath<java.time.LocalDate> readDate = createDate("readDate", java.time.LocalDate.class);

    public final QUser user;

    public QReadingLog(String variable) {
        this(ReadingLog.class, forVariable(variable), INITS);
    }

    public QReadingLog(Path<? extends ReadingLog> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QReadingLog(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QReadingLog(PathMetadata metadata, PathInits inits) {
        this(ReadingLog.class, metadata, inits);
    }

    public QReadingLog(Class<? extends ReadingLog> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

