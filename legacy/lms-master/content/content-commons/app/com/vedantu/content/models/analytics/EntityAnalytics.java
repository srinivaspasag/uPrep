package com.vedantu.content.models.analytics;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "entityanalytics", noClassnameStored = true)
@Indexes(@Index("entity.id,entity.type,acadDim.id,acadDim.type"))
public class EntityAnalytics extends VedantuBaseMongoModel {

    // test, challenge, assignment
    public SrcEntity         entity;

    // overall, course, topic, subtopic-wise
    public AcademicDimension acadDim;

    public EntityMeasures    measures;

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{entity:").append(entity).append(", acadDim:").append(acadDim)
                .append(", measures:").append(measures).append(", id:").append(id)
                .append(", timeCreated:").append(timeCreated).append(", lastUpdated:")
                .append(lastUpdated).append(", recordState:").append(recordState).append("}");
        return builder.toString();
    }

}
