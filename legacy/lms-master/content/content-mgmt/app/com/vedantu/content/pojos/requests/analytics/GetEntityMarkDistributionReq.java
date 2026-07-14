package com.vedantu.content.pojos.requests.analytics;

import play.data.validation.Constraints.Required;

public class GetEntityMarkDistributionReq extends GetEntityResultAnalyticsReq {

	@Required
	public int bucketCount;
	public String brdId;// filter for only this board
}
