package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelExtendedInfo;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Document(value = "userentityattempts")
@CompoundIndexes({
        @CompoundIndex(name = "userId,entity.type,entity.id"),
        @CompoundIndex(name = "orgId,entity.type,entity.id")
})
@Setter
@Getter
public class UserEntityAttempt extends VedantuBaseMongoModel {

    public String       userId;

    public String       orgId;

    // test, challenge, assignment
    public SrcEntity entity;

    public SrcEntity    parent;  // if this is part of some entity {i.e
    // paper1/paper2 --> main test }

    public List<String> qIds;

    public long         endTime;

    public boolean      processed;

    public boolean      finished;
    // This is used for Test Pause, Resume
    public String       testStatus = HardCodedConstants.emptyString;  // The status we use here are ONGOING, PAUSED, RESUMED, FINISHED
    public long         timeLeft;

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
