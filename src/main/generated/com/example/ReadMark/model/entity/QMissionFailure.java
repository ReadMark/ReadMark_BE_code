package com.example.ReadMark.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QMissionFailure is a Querydsl query type for MissionFailure
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMissionFailure extends EntityPathBase<MissionFailure> {

    private static final long serialVersionUID = -2115008918L;

    private static final PathInits INITS = PathInits.DIRECT2;

    public static final QMissionFailure missionFailure = new QMissionFailure("missionFailure");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final DateTimePath<java.time.LocalDateTime> failedAt = createDateTime("failedAt", java.time.LocalDateTime.class);

    public final DatePath<java.time.LocalDate> failureDate = createDate("failureDate", java.time.LocalDate.class);

    public final NumberPath<Long> failureId = createNumber("failureId", Long.class);

    public final StringPath failureReason = createString("failureReason");

    public final QMission mission;

    public final QUser user;

    public QMissionFailure(String variable) {
        this(MissionFailure.class, forVariable(variable), INITS);
    }

    public QMissionFailure(Path<? extends MissionFailure> path) {
        this(path.getType(), path.getMetadata(), PathInits.getFor(path.getMetadata(), INITS));
    }

    public QMissionFailure(PathMetadata metadata) {
        this(metadata, PathInits.getFor(metadata, INITS));
    }

    public QMissionFailure(PathMetadata metadata, PathInits inits) {
        this(MissionFailure.class, metadata, inits);
    }

    public QMissionFailure(Class<? extends MissionFailure> type, PathMetadata metadata, PathInits inits) {
        super(type, metadata, inits);
        this.mission = inits.isInitialized("mission") ? new QMission(forProperty("mission")) : null;
        this.user = inits.isInitialized("user") ? new QUser(forProperty("user")) : null;
    }

}

