package com.lms.event.details;

import com.lms.common.news.EntityNewsInfo;
import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.enums.EntityType;
import com.lms.common.vedantu.enums.EventType;
import com.lms.common.vedantu.enums.UserActionType;
import com.lms.common.vedantu.event.api.IEventDetails;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

import javax.validation.constraints.NotBlank;

public class PostRemarkDetails implements IEventDetails {

	public static final String PROVIDEE_ID = "provideeId";

	public static final String PROVIDER_ID = "providerId";
	public static final String REMARK_ID = "remarkId";

	public String remarkId;
	@NotBlank
	public String providerId;

	@NotBlank
	public String provideeId;

	@NotBlank
	public String orgId;

	@Override
	public JSONObject toJSON() throws JSONException {

		// TODO Auto-generated method stub
		JSONObject jsonObject = new JSONObject();
		jsonObject.put(PROVIDER_ID, providerId);
		jsonObject.put(PROVIDEE_ID, provideeId);
		jsonObject.put(REMARK_ID, remarkId);
		jsonObject.put(ConstantsGlobal.ORG_ID, orgId);

		return jsonObject;

	}

	@Override
	public void fromJSON(JSONObject json) {

		// TODO Auto-generated method stub

		remarkId = JSONUtils.getString(json, REMARK_ID);
		providerId = JSONUtils.getString(json, PROVIDER_ID);
		provideeId = JSONUtils.getString(json, PROVIDEE_ID);
		orgId = JSONUtils.getString(json, ConstantsGlobal.ORG_ID);

	}

	@Override
	public SrcEntity __getSrcEntity() {

		// TODO Auto-generated method stub
		return new SrcEntity(EntityType.REMARK, remarkId);
	}

	@Override
	public NewsActivity toNewsActivity() {

		// TODO Auto-generated method stub
		NewsActivity activity = new NewsActivity();
		activity.eType = EventType.POST_REMARK;
		activity.src = new SrcEntity(EntityType.REMARK, remarkId);

		activity.srcOwner = new SrcEntity(EntityType.USER, provideeId);
		activity.actor = new SrcEntity(EntityType.USER, providerId);

		activity.info = new EntityNewsInfo();
		activity.info.actionType = UserActionType.ADDED;

		return activity;
	}

	@Override
	public boolean getNotificationEnabled() {

		return true;
	}

	@Override
	public boolean enableNotifcation(boolean value) {

		return true;
	}

}
