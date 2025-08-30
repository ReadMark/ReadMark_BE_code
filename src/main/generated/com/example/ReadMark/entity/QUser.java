package com.example.ReadMark.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QUser is a Querydsl query type for User
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QUser extends EntityPathBase<User> {

    private static final long serialVersionUID = 1534992028L;

    public static final QUser user = new QUser("user");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath email = createString("email");

    public final ListPath<FavoritePage, QFavoritePage> favoritePages = this.<FavoritePage, QFavoritePage>createList("favoritePages", FavoritePage.class, QFavoritePage.class, PathInits.DIRECT2);

    public final ListPath<FavoriteQuote, QFavoriteQuote> favoriteQuotes = this.<FavoriteQuote, QFavoriteQuote>createList("favoriteQuotes", FavoriteQuote.class, QFavoriteQuote.class, PathInits.DIRECT2);

    public final StringPath name = createString("name");

    public final StringPath password = createString("password");

    public final ListPath<ReadingLog, QReadingLog> readingLogs = this.<ReadingLog, QReadingLog>createList("readingLogs", ReadingLog.class, QReadingLog.class, PathInits.DIRECT2);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final ListPath<UserBook, QUserBook> userBooks = this.<UserBook, QUserBook>createList("userBooks", UserBook.class, QUserBook.class, PathInits.DIRECT2);

    public final NumberPath<Long> userId = createNumber("userId", Long.class);

    public QUser(String variable) {
        super(User.class, forVariable(variable));
    }

    public QUser(Path<? extends User> path) {
        super(path.getType(), path.getMetadata());
    }

    public QUser(PathMetadata metadata) {
        super(User.class, metadata);
    }

}

