package com.vedantu.cmds.models.event.details;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;


public class UpdateEntityDetails implements IEventDetails{
    public String       userId;
    public String       orgId;
    public SrcEntity    content;
    public String       jobId;
    

    @Override
    public JSONObject toJSON() throws JSONException {
        // TODO Auto-generated method stub
        JSONObject json = new JSONObject();
        json.put(ConstantsGlobal.USER_ID, content);
        json.put(ConstantsGlobal.ORG_ID, userId);
        json.put(ConstantsGlobal.JOB_ID, jobId);
        json.put(ConstantsGlobal.CONTENT, content.toJSON());

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {
        // TODO Auto-generated method stub
        userId = JSONUtils.getString(json, ConstantsGlobal.USER_ID);
        orgId = JSONUtils.getString(json, ConstantsGlobal.ORG_ID);
        jobId = JSONUtils.getString(json, ConstantsGlobal.JOB_ID);
        content = (SrcEntity) JSONUtils.getJSONAware(new SrcEntity(), json,
                ConstantsGlobal.CONTENT);

    }

    @Override
    public SrcEntity __getSrcEntity() {

        // TODO Auto-generanullted method stub
        return content;
    }

    @Override
    public NewsActivity toNewsActivity() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public boolean enableNotifcation(boolean value) {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean getNotificationEnabled() {

        // TODO Auto-generated method stub
        return false;
    }

}
