package com.vedantu.content.event.details;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.commons.utils.ObjectMapperUtils;

public class EndTestDetails implements IEventDetails {

    public String     attemptId;
    public String     userId;
    public String     entityId;
    public EntityType entityType;
    public String     setName;
    public long       startTime;
    public long       duration;
    public String     orgId;
    public String     processType;// TEST, USER

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
