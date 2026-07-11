package com.vedantu.comm.event.details;

import org.json.JSONException;
import org.json.JSONObject;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;

public class MessageDistributeDetails implements IEventDetails {

    public static final String MESSAGE_ID      = "messageId";

    public static final String CONVERSATION_ID = "conversationId";

    public String              messageId;
    @Required
    public String              conversationId;

    @Override
    public JSONObject toJSON() throws JSONException {

        JSONObject jsonObject = new JSONObject();
        jsonObject.put(MESSAGE_ID, messageId);
        jsonObject.put(CONVERSATION_ID, conversationId);

        return jsonObject;

    }

    @Override
    public void fromJSON(JSONObject json) {

        messageId = JSONUtils.getString(json, MESSAGE_ID);
        conversationId = JSONUtils.getString(json, CONVERSATION_ID);

    }

    @Override
    public SrcEntity __getSrcEntity() {

        return new SrcEntity(EntityType.MESSAGE, messageId);
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
