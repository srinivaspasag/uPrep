package com.lms.event.details;

import com.lms.common.news.NewsActivity;
import com.lms.common.utils.ObjectMapperUtils;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.event.api.IEventDetails;
import common.utils.JSONUtils;
import lombok.Getter;
import lombok.Setter;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

@Getter
@Setter
public class EndTestDetails implements IEventDetails {

    public String attemptId;
    public String userId;
    public String entityId;
    public EntityType entityType;
    public String setName;
    public long startTime;
    public long duration;
    public String orgId;
    public String processType;// TEST, USER

    public EndTestDetails() {
        super();
    }

    public EndTestDetails(String attemptId, String userId, String entityId, EntityType entityType,
                          String setName, long startTime, long duration, String orgId, String processType) {
        super();
        this.attemptId = attemptId;
        this.userId = userId;
        this.entityId = entityId;
        this.entityType = entityType;
        this.setName = setName;
        this.startTime = startTime;
        this.duration = duration;
        this.orgId = orgId;
        this.processType = processType;
    }

    @Override
    public JSONObject toJSON() throws JSONException {

        return new JSONObject(ObjectMapperUtils.convertValue(this, Map.class));
    }

    @Override
    public void fromJSON(JSONObject json) {

        attemptId = JSONUtils.getString(json, ConstantsGlobal.ATTEMPT_ID);
        userId = JSONUtils.getString(json, ConstantsGlobal.USER_ID);
        setName = JSONUtils.getString(json, "setName");
        entityId = JSONUtils.getString(json, ConstantsGlobal.ENTITY_ID);
        entityType = EntityType.valueOfKey(JSONUtils.getString(json, ConstantsGlobal.ENTITY_TYPE));
        startTime = JSONUtils.getLong(json, ConstantsGlobal.START_TIME);
        duration = JSONUtils.getLong(json, ConstantsGlobal.DURATION);
        orgId = JSONUtils.getString(json, ConstantsGlobal.ORG_ID);
        processType = JSONUtils.getString(json, ConstantsGlobal.PROCESS_TYPE);
    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(entityType, entityId);
    }

    @Override
    public NewsActivity toNewsActivity() {

        return null;
    }

    @Override
    public boolean enableNotifcation(boolean value) {

        return false;
    }

    @Override
    public boolean getNotificationEnabled() {

        return false;
    }
}
