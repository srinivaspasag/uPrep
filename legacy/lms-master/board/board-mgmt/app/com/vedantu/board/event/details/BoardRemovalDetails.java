package com.vedantu.board.event.details;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;

public class BoardRemovalDetails implements IEventDetails {

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
