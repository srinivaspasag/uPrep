package com.vedantu.content.pojos.requests.analytics;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.requests.AbstractOrgListReq;

public class GetEntityScheduleAnalyticsReq extends AbstractOrgListReq {

	@Required
	public EntityType entityType;

	@Required
	public String programId;

	public String centerId;

	public String sectionId;

	public String brdId;

	public String courseName = "";
	public String courseId = "";

	public String topicName = "";
	public String topicId = "";

	public String query;
}
