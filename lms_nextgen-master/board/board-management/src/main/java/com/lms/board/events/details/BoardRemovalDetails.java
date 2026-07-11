package com.lms.board.events.details;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.event.api.IEventDetails;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class BoardRemovalDetails implements IEventDetails
{
    public List<String> brdIds = new ArrayList<String>();
    public boolean      changeState;
    public String       userId;

    public BoardRemovalDetails() throws ClassNotFoundException {

    }

    @Override
    public SrcEntity __getSrcEntity() {

        return null;
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

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject json = new JSONObject();
        json.put("brdIds", brdIds);
        json.put("changeState", changeState);
        json.put("userId", userId);

        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        brdIds = JSONUtils.getList(json, "brdIds");
        changeState = JSONUtils.getBoolean(json, "changeState");
        userId = JSONUtils.getString(json, "userId");
    }

}
