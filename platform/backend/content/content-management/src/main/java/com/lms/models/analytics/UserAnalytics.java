package com.lms.models.analytics;

import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.models.EntityMeasures;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter

@Document(value = "useranalytics")
public class UserAnalytics extends VedantuBaseMongoModel
{
    @Indexed
    public String userId;

    // overall, course, topic, subtopic-wise
    @Indexed
    public AcademicDimension acadDim;

    public EntityMeasures measures;

    public UserAnalytics() {
        super();
    }

    public UserAnalytics(String userId, AcademicDimension acadDim,
                         EntityMeasures measures) {
        super();
        this.userId = userId;
        this.acadDim = acadDim;
        this.measures = measures;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{userId:");
        builder.append(userId);
        builder.append(", acadDim:");
        builder.append(acadDim);
        builder.append(", measures:");
        builder.append(measures);
        builder.append(", id:");
        builder.append(id);
        builder.append(", timeCreated:");
        builder.append(timeCreated);
        builder.append(", lastUpdated:");
        builder.append(lastUpdated);
        builder.append(", recordState:");
        builder.append(recordState);
        builder.append("}");
        return builder.toString();
    }

}
