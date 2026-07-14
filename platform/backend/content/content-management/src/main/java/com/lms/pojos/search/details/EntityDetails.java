package com.lms.pojos.search.details;

import com.lms.common.exception.VedantuException;
import com.lms.common.news.NewsActivity;
import com.lms.common.utils.ObjectMapperUtils;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.event.api.IEventDetails;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Map;

public class EntityDetails implements IEventDetails {

	public String userId;
	public String id;

	// The following entity is the 'entity' which created this event
	public SrcEntity entity;
	public UserActionType userAction;
	public boolean notificationEnabled;

	public EntityDetails() {

		notificationEnabled = true;
	}

	@Override
	public JSONObject toJSON() throws JSONException {

		return new JSONObject(ObjectMapperUtils.convertValue(this, Map.class));
	}

	@Override
	public void fromJSON(JSONObject json) {

		if (json != null) {
			userId = JSONUtils.getString(json, ConstantsGlobal.USER_ID);
			id = JSONUtils.getString(json, ConstantsGlobal.ID);
			JSONObject src = JSONUtils.getJSONObject(json, ConstantsGlobal.ENTITY);
			if (src != null) {
				entity = new SrcEntity();
				entity.id = JSONUtils.getString(src, ConstantsGlobal.ID);
				entity.type = EntityType.valueOfKey(JSONUtils.getString(src, ConstantsGlobal.TYPE));
			}
			userAction = UserActionType.valueOf(JSONUtils.getString(json, ConstantsGlobal.USER_ACTION));
			notificationEnabled = JSONUtils.getBoolean(json, ConstantsGlobal.NOTIFICATION_ENABLED);
		}
	}

	@Override
	public SrcEntity __getSrcEntity() {

		return entity;
	}

	// @Override
	// public NewsActivity toNewsActivity() {
	// // TODO Auto-generated method stub
	// return null;
	// }
	//
	// public ITemplateDetails toITempletDetails() {
	// return null;
	// }

	public boolean enableNotifcation(boolean value) {

		notificationEnabled = value;
		return notificationEnabled;
	}

	public boolean getNotificationEnabled() {

		return notificationEnabled;
	}

	public NewsActivity toNewsActivity() throws VedantuException {

		return null;
	}

}
