package com.example.ReadMark.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMissionCompletion is a Querydsl query type for MissionCompletion
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMissionCompletion extends EntityPathBase<MissionCompletion> {

    private static final long serialVersionUID = 1656742652L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMissionCompletion missionCompletion = new QMissionCompletion("missionCompletion");

    public final DateTimePath<java.time.LocalDateTime> completedAt = createDateTime("completedAt", java.time.LocalDateTime.class);

    public final DatePath<java.time.LocalDate> completedDate = createDate("completedDate", java.time.LocalDate.class);

    public final NumberPath<Long> completionId = createNumber("completionId", Long.class);

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final QMission mission;

    public final QUser user;

    public QMissionCompletion(String variable) {
        this(MissionCompletion.class, forVariable(variable), INITS);
    }

    public QMissionCompletion(Path<? extends MissionCompletion> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMissionCompletion(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMissionCompletion(PathMetadata metadata, PathInits inits) {
        this(MissionCompletion.class, metadata, inits);
    }

    public QMissionCompletion(Class<? extends MissionCompletion> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.mission = inits.isInitialized("mission") ? new QMission(forProperty("mission")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

