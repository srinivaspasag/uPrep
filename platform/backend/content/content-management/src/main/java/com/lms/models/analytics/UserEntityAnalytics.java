package com.lms.models.analytics;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.models.EntityMeasures;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(value = "userentityanalytics")
public class UserEntityAnalytics extends UserAnalytics
{

    public String    attemptId;

    @Indexed
    // test, challenge, assignment
    public SrcEntity entity;
    public double     percentage;
    public String    orgId;

    public UserEntityAnalytics() {

        super();
    }

    public UserEntityAnalytics(String userId, AcademicDimension acadDim, EntityMeasures measures,
                               String attemptId, SrcEntity entity, double percentage) {

        super(userId, acadDim, measures);
        this.attemptId = attemptId;
        this.entity = entity;
        this.percentage = percentage;
    }

    public UserEntityAnalytics(String userId, AcademicDimension acadDim,
                               EntityMeasures measures, String attemptId, SrcEntity entity,
                               double percentage, String orgId) {
        this(userId, acadDim, measures, attemptId, entity, percentage);
        this.orgId = orgId;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{attemptId:");
        builder.append(attemptId);
        builder.append(", entity:");
        builder.append(entity);
        builder.append(", userId:");
        builder.append(userId);
        builder.append(", acadDim:");
        builder.append(acadDim);
        builder.append(", measures:");
        builder.append(measures);
        builder.append(", id:");
        builder.append(id);
        builder.append(", percentage:");
        builder.append(percentage);
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
