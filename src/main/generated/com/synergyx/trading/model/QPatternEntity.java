package com.synergyx.trading.model;

import static com.querydsl.core.types.PathMetadataFactory.*;

import com.querydsl.core.types.dsl.*;

import com.querydsl.core.types.PathMetadata;
import javax.annotation.processing.Generated;
import com.querydsl.core.types.Path;
import com.querydsl.core.types.dsl.PathInits;


/**
 * QPatternEntity is a Querydsl query type for PatternEntity
 */
@Generated("com.querydsl.codegen.DefaultEntitySerializer")
public class QPatternEntity extends EntityPathBase<PatternEntity> {

    private static final long serialVersionUID = 839707953L;

    public static final QPatternEntity patternEntity = new QPatternEntity("patternEntity");

    public final DateTimePath<java.time.LocalDateTime> createdAt = createDateTime("createdAt", java.time.LocalDateTime.class);

    public final NumberPath<Long> id = createNumber("id", Long.class);

    public final StringPath patternName = createString("patternName");

    public final EnumPath<PeriodUnit> periodUnit = createEnum("periodUnit", PeriodUnit.class);

    public final NumberPath<Integer> periodValue = createNumber("periodValue", Integer.class);

    public final ListPath<Double, NumberPath<Double>> points = this.<Double, NumberPath<Double>>createList("points", Double.class, NumberPath.class, PathInits.DIRECT2);

    public final StringPath stockId = createString("stockId");

    public final StringPath symbol = createString("symbol");

    public final NumberPath<Double> tolerance = createNumber("tolerance", Double.class);

    public final DateTimePath<java.time.LocalDateTime> updatedAt = createDateTime("updatedAt", java.time.LocalDateTime.class);

    public final StringPath userId = createString("userId");

    public QPatternEntity(String variable) {
        super(PatternEntity.class, forVariable(variable));
    }

    public QPatternEntity(Path<? extends PatternEntity> path) {
        super(path.getType(), path.getMetadata());
    }

    public QPatternEntity(PathMetadata metadata) {
        super(PatternEntity.class, metadata);
    }

}

