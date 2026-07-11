package com.vedantu.content.event.details;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;

public class NewsRemoveDetails implements IEventDetails {

    public SrcEntity    content;
    public List<String> newsFeedsForRemoval;

    @Override
    public JSONObject toJSON() throws JSONException {

        // TODO Auto-generated method stub
        JSONObject json = new JSONObject();
        json.put("content", content.toJSON());
        if (CollectionUtils.isNotEmpty(newsFeedsForRemoval)) {
            json.put("newsFeedsForRemoval", newsFeedsForRemoval);
        }

        return json;

    }

    @Override
    public void fromJSON(JSONObject json) {

        // TODO Auto-generated method stub
        content = new SrcEntity();
        content = (SrcEntity) JSONUtils.getJSONAware(content, json, "content");
        newsFeedsForRemoval = JSONUtils.getList(json, "newsFeedsForRemoval",
                new ArrayList<String>());
    }

    @Override
    public SrcEntity __getSrcEntity() {

        // TODO Auto-generated method stub
        return content;
    }

    @Override
    public NewsActivity toNewsActivity() {

        // TODO Auto-generated method stub
        return null;
    }

 

    @Override
    public boolean getNotificationEnabled() {

        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean enableNotifcation(boolean value) {

        // TODO Auto-generated method stub
        return false;
    }

}
