package com.vedantu.cmds.models.event.details;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;

public class SDCardDetails implements IEventDetails, JSONAware {

    public String groupId;

    @Override
    public NewsActivity toNewsActivity() throws VedantuException {

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

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        json.put("groupId", groupId);

        return json;
    }

    @Override
    public void fromJSON(JSONObject json) {

        groupId = JSONUtils.getString(json, "groupId");
    }

    @Override
    public SrcEntity __getSrcEntity() {
        return null;
    }

}
