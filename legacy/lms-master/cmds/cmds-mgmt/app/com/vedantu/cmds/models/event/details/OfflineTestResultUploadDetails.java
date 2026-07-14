package com.vedantu.cmds.models.event.details;

import java.util.List;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.events.apis.IEventDetails;
import com.vedantu.commons.news.NewsActivity;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.commons.utils.ObjectMapperUtils;

public class OfflineTestResultUploadDetails implements IEventDetails {

	public String uuid;
	public List<String> sheetNames;
	public String orgId;
	public String programId;
	public String testId;
	public String userId;// who uploaded the result sheet
	public String jobId;

	public OfflineTestResultUploadDetails() {
	}

	public OfflineTestResultUploadDetails(String uuid, List<String> sheetNames,
			String orgId, String programId, String testId, String userId, String jobId) {
		this.uuid = uuid;
		this.sheetNames = sheetNames;
		this.orgId = orgId;
		this.programId = programId;
		this.testId = testId;
		this.userId = userId;
		this.jobId = jobId;
	}

	@Override
	public JSONObject toJSON() throws JSONException {
		return new JSONObject(ObjectMapperUtils.convertValue(this, Map.class));
	}

	@Override
	public void fromJSON(JSONObject json) {
		uuid = JSONUtils.getString(json, ConstantsGlobal.UUID);
		sheetNames = JSONUtils.getList(json, "sheetNames");
		orgId = JSONUtils.getString(json, ConstantsGlobal.ORG_ID);
		testId = JSONUtils.getString(json, ConstantsGlobal.TEST_ID);
		programId = JSONUtils.getString(json, ConstantsGlobal.PROGRAM_ID);
		userId = JSONUtils.getString(json, ConstantsGlobal.USER_ID);
		jobId = JSONUtils.getString(json, ConstantsGlobal.JOB_ID);
	}

	@Override
	public SrcEntity __getSrcEntity() {
		return new SrcEntity(EntityType.USER, userId);
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
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{uuid:").append(uuid).append(", sheetNames:").append(sheetNames)
                .append(", orgId:").append(orgId).append(", programId:").append(programId)
                .append(", testId:").append(testId).append(", userId:").append(userId)
                .append(", jobId:").append(jobId).append("}");
        return builder.toString();
    }

}
