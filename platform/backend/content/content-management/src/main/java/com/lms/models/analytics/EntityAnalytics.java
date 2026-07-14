package com.lms.models.analytics;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.models.EntityMeasures;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(value = "entityanalytics")
@CompoundIndexes(@CompoundIndex(name = "entity.id,entity.type,acadDim.id,acadDim.type"))

public class EntityAnalytics extends VedantuBaseMongoModel {
    // test, challenge, assignment
    public SrcEntity entity;

    // overall, course, topic, subtopic-wise
    @Indexed
    public AcademicDimension acadDim;

    public EntityMeasures measures;


}
