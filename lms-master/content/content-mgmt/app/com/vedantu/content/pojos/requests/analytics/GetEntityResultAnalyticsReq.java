package com.vedantu.content.pojos.requests.analytics;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractOrgListReq;

public class GetEntityResultAnalyticsReq extends AbstractOrgListReq {

	@Required
	public SrcEntity entity;
	public boolean isDetailedResultSheet;
	public double maxScore;
	public double minScore;
	public String studentUserId;
	public String queryText;
}
