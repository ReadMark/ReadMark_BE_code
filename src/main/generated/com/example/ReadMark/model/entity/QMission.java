package com.example.ReadMark.model.entity;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;


/**
 * QMission is a Querydsl query type for Mission
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QMission extends EntityPathBase<Mission> {

    private static final long serialVersionUID = -1066409216L;

    public static final QMission mission = new QMission("mission");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final StringPath description = createString("description");

    public final NumberPath<Long> missionId = createNumber("missionId", Long.class);

    public final StringPath reward = createString("reward");

    public final NumberPath<Integer> targetValue = createNumber("targetValue", Integer.class);

    public final StringPath title = createString("title");

    public final StringPath type = createString("type");

    public final StringPath unit = createString("unit");

    public QMission(String variable) {
        super(Mission.class, forVariable(variable));
    }

    public QMission(Path<? extends Mission> path) {
        super(path.getType(), path.getMetadata());
    }

    public QMission(PathMetadata metadata) {
        super(Mission.class, metadata);
    }

}

