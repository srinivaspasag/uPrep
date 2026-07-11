package com.vedantu.content.pojos.requests.analytics;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetUserAnalyticsStatsReq extends AbstractAuthCheckReq {

	@Required
	public EntityType entityType;

	public String targetUserId;

	public String _getResultForUserId() {
		return StringUtils.isEmpty(targetUserId) ? userId : targetUserId;
	}
}
