package com.vedantu.content.pojos.requests.analytics;

import java.util.ArrayList;
import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class StartAttemptReq extends AbstractAuthCheckReq {
	@Required
	public String entityId;
	@Required
	public EntityType entityType;
	public SrcEntity target = new SrcEntity();
	public String orgId;
	public String studentUserId;
	public String sectionId;

	public String setName; // this field is optional--> will be used when there
							// are multiple sets for a test

	public List<String> qIds = new ArrayList<String>();// if qids are provide than qids order will not be
								// computed, this will be helpfull for challenge

	public String attemptId; // This field is for other POJOS
	public long timeLeft;
	public String testState;

	public StartAttemptReq() {
		super();
	}

	public StartAttemptReq(String callingUserId, String userId,
			String entityId, EntityType entityType, String setName) {
		super(callingUserId, userId);
		this.entityId = entityId;
		this.entityType = entityType;
		this.setName = setName;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{entityId:");
		builder.append(entityId);
		builder.append(", entityType:");
		builder.append(entityType);
		builder.append(", setName:");
		builder.append(setName);
		builder.append(", callingUserId:");
		builder.append(callingUserId);
		builder.append(", userId:");
		builder.append(userId);
		builder.append(", callingApp:");
		builder.append(callingApp);
		builder.append(", callingAppId:");
		builder.append(callingAppId);
		builder.append("}");
		return builder.toString();
	}

}
