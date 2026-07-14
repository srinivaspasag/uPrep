package com.vedantu.organization.event.ei.details;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.organization.enums.UploadState;

public class OrgAttemptUploadDetails implements IEventDetails {

    public String      attemptId;
    public String      orgId;
    public String      userId;
    public UploadState processUploadState;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        json.put(ConstantsGlobal.ATTEMPT_ID, attemptId);
        json.put(ConstantsGlobal.ORG_ID, orgId);
        json.put(ConstantsGlobal.USER_ID, userId);
        if (processUploadState != null) {
            json.put("processUploadState", processUploadState.name());
        }
        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        attemptId = JSONUtils.getString(json, ConstantsGlobal.ATTEMPT_ID);
        orgId = JSONUtils.getString(json, ConstantsGlobal.ORG_ID);
        userId = JSONUtils.getString(json, ConstantsGlobal.USER_ID);
        String uploadStatus = JSONUtils.getString(json, "processUploadState");

        if (StringUtils.isNotEmpty(uploadStatus)) {
            processUploadState = UploadState.valueOf(uploadStatus);
        }
    }

    @Override
    public NewsActivity toNewsActivity() throws VedantuException {

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

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.ORGANIZATION, orgId);
    }

}
