package com.example.ReadMark.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QBookPage is a Querydsl query type for BookPage
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QBookPage extends EntityPathBase<BookPage> {

    private static final long serialVersionUID = 217399844L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QBookPage bookPage = new QBookPage("bookPage");

    public final QBook book;

    public final DateTimePath<java.time.LocalDateTime> capturedAt = createDateTime("capturedAt", java.time.LocalDateTime.class);

    public final NumberPath<Double> confidence = createNumber("confidence", Double.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath deviceInfo = createString("deviceInfo");

    public final StringPath extractedText = createString("extractedText");

    public final ArrayPath<byte[], Byte> imageData = createArray("imageData", byte[].class);

    public final StringPath imageUrl = createString("imageUrl");

    public final StringPath language = createString("language");

    public final NumberPath<Integer> numberCount = createNumber("numberCount", Integer.class);

    public final NumberPath<Long> pageId = createNumber("pageId", Long.class);

    public final NumberPath<Integer> pageNumber = createNumber("pageNumber", Integer.class);

    public final NumberPath<Double> textQuality = createNumber("textQuality", Double.class);

    public final QUser user;

    public QBookPage(String variable) {
        this(BookPage.class, forVariable(variable), INITS);
    }

    public QBookPage(Path<? extends BookPage> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QBookPage(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QBookPage(PathMetadata metadata, PathInits inits) {
        this(BookPage.class, metadata, inits);
    }

    public QBookPage(Class<? extends BookPage> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.book = inits.isInitialized("book") ? new QBook(forProperty("book")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

