package com.vedantu.content.event.details;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.commons.utils.ObjectMapperUtils;

public class DoubtsProcessingDetails implements IEventDetails {

    public String discussionId;

    public DoubtsProcessingDetails() {
        super();
    }

    public DoubtsProcessingDetails(String discussionId) {
        super();
        this.discussionId = discussionId;
    }

    @Override
    public JSONObject toJSON() throws JSONException {
        return new JSONObject(ObjectMapperUtils.convertValue(this, Map.class));
    }

    @Override
    public void fromJSON(JSONObject json) {
        discussionId = JSONUtils.getString(json, "discussionId");
    }

    @Override
    public SrcEntity __getSrcEntity() {
        return new SrcEntity(EntityType.DISCUSSION, discussionId);
    }

    @Override
    public String toString() {
        return "DoubtsProcessingDetails [discussionId=" + discussionId + "]";
    }

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

}
