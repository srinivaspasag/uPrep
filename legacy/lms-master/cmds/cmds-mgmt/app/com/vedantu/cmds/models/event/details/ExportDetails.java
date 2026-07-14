package com.vedantu.cmds.models.event.details;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.events.apis.JSONAware;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;

public class ExportDetails implements IEventDetails, JSONAware {

    public static final String EXPORT_ID = "exportId";
    public String              exportId;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        json.put(EXPORT_ID, exportId);

        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        exportId = JSONUtils.getString(json, EXPORT_ID);
    }

    @Override
    public SrcEntity __getSrcEntity() {

        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public NewsActivity toNewsActivity() {

        
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
