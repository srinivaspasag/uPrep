package com.vedantu.content.models.analytics;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Index;
import com.google.code.morphia.annotations.Indexes;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.pojos.responses.ModelExtendedInfo;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.ei.utils.StringUtils;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "userentityattempts", noClassnameStored = true)
@Indexes({
    @Index(value = "userId,entity.type,entity.id"),
    @Index(value = "orgId,entity.type,entity.id")
})
public class UserEntityAttempt extends VedantuBaseMongoModel {

    public String       userId;

    public String       orgId;

    // test, challenge, assignment
    public SrcEntity    entity;

    public SrcEntity    parent;  // if this is part of some entity {i.e
                                  // paper1/paper2 --> main test }

    public List<String> qIds;

    public long         endTime;

    public boolean      processed;

    public boolean      finished;
 // This is used for Test Pause, Resume
    public String       testStatus = StringUtils.EMPTY;  // The status we use here are ONGOING, PAUSED, RESUMED, FINISHED
    public long         timeLeft;
    public Map<String, Map<QuestionType,Integer>> mapping;
    public List<String> attemptedQIds=new ArrayList<String>();

    public UserEntityAttempt() {

        super();
    }

    public UserEntityAttempt(String userId, String orgId, SrcEntity entity, List<String> qIds) {

        super();
        this.userId = userId;
        this.orgId = orgId;
        this.entity = entity;
        this.qIds = qIds;
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        ModelBasicInfo info = new ModelBasicInfo(_getStringId(), recordState);
        return info;
    }

    @Override
    public ModelExtendedInfo toExtendedInfo() {

        // TODO Auto-generated method stub
        return super.toExtendedInfo();
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{userId:").append(userId).append(", entity:").append(entity)
                .append(", parent:").append(parent).append(", qIds:").append(qIds)
                .append(", endTime:").append(endTime).append(", finished:").append(finished)
                .append(", id:").append(id).append(", timeCreated:").append(timeCreated)
                .append(", lastUpdated:").append(lastUpdated).append(", recordState:")
                .append(recordState).append("}");
        return builder.toString();
    }

}
