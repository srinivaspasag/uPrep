package com.lms.models.event.search.details;

import com.lms.common.news.NewsActivity;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.constants.ConstantsGlobal;
import com.lms.common.vedantu.event.api.IEventDetails;
import common.utils.JSONUtils;
import org.json.JSONException;
import org.json.JSONObject;

public class EntityPublishingDetails implements IEventDetails {
	public String userId;
	public String orgId;
	public SrcEntity content;
	public String jobId;

	public EntityPublishingDetails() {

	}

	public EntityPublishingDetails(String userId, String orgId, SrcEntity srcEntity, String statusId) {
		this.userId = userId;
		this.orgId = orgId;
		this.content = srcEntity;
		this.jobId = statusId;
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		// TODO Auto-generated method stub
		JSONObject json = new JSONObject();
		json.put(ConstantsGlobal.USER_ID, userId);
		json.put(ConstantsGlobal.ORG_ID, orgId);
		json.put(ConstantsGlobal.JOB_ID, jobId);
		json.put(ConstantsGlobal.CONTENT, content.toJSON());

		return json;
	}

	@Override
	public void fromJSON(JSONObject json) {
		// TODO Auto-generated method stub
		userId = JSONUtils.getString(json, ConstantsGlobal.USER_ID);
		orgId = JSONUtils.getString(json, ConstantsGlobal.ORG_ID);
		jobId = JSONUtils.getString(json, ConstantsGlobal.JOB_ID);
		content = (SrcEntity) JSONUtils.getJSONAware(new SrcEntity(), json, ConstantsGlobal.CONTENT);

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
