package com.lms.event.details;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.event.api.IEventDetails;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.validation.constraints.NotBlank;

public class MessageDistributeDetails implements IEventDetails {

	public static final String MESSAGE_ID = "messageId";

	public static final String CONVERSATION_ID = "conversationId";

	public String messageId;
	@NotBlank(message = "conversationId should not be empty")
	public String conversationId;

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
