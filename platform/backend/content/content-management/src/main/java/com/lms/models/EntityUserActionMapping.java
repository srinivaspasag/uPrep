package com.lms.models;


import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;

@Getter
@Setter
@Document(value = "entityuseractionmapping")
@CompoundIndexes({ @CompoundIndex(name = "userId,actionType,target.id"),
        @CompoundIndex(name = "userId,actionType") })
public class EntityUserActionMapping  extends VedantuBaseMongoModel

{
    public String userId;

    public UserActionType actionType;

    public SrcEntity target;

    public SrcEntity context;

    public EntityUserActionMapping() {
        this(null, null, null, null);
    }

    public EntityUserActionMapping(String userId, UserActionType actionType,
                                   SrcEntity target, SrcEntity context) {
        super();
        this.userId = userId;
        this.actionType = actionType;
        this.target = target;
        this.context = context;
    }

    @Override
    public String toString() {
        return "EntityUserActionMapping [userId=" + userId + ", actionType="
                + actionType + ", target=" + target + ", id=" + id
                + ", timeCreated=" + timeCreated + ", lastUpdated="
                + lastUpdated + ", recordState=" + recordState + "]";
    }

}
