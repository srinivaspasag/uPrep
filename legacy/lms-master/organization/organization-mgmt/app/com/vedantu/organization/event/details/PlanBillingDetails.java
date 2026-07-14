package com.vedantu.organization.event.details;

import org.json.JSONException;
import org.json.JSONObject;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;

public class PlanBillingDetails implements IEventDetails {

    public static final String ORG_ID     = "orgId";
    public static final String PLAN_ID    = "planId";

    public static final String START_TIME = "startTime";
    public static final String END_TIME   = "endTime";

    public String              orgId;

    public String              planId;
    @Required
    public long                startTime  = -1;
    public long                endTime    = -1;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(ORG_ID, orgId);
        jsonObject.put(PLAN_ID, planId);
        jsonObject.put(START_TIME, startTime);
        jsonObject.put(END_TIME, endTime);
        return jsonObject;

    }

    @Override
    public void fromJSON(JSONObject json) {

        orgId = JSONUtils.getString(json, ORG_ID);
        planId = JSONUtils.getString(json, PLAN_ID);
        startTime = JSONUtils.getLong(json, START_TIME);
        endTime = JSONUtils.getLong(json, END_TIME);

    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.ORGANIZATION, orgId);
    }

    @Override
    public NewsActivity toNewsActivity() {

        return null;
    }

    @Override
    public boolean enableNotifcation(boolean value) {

        return true;
    }

    @Override
    public boolean getNotificationEnabled() {

        return true;
    }

}
