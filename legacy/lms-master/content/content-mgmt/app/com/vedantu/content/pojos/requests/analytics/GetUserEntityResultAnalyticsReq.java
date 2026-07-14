package com.vedantu.content.pojos.requests.analytics;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.requests.AbstractOrgListReq;

public class GetUserEntityResultAnalyticsReq extends AbstractOrgListReq {

	@Required
	public EntityType entityType;
	public String targetUserId;

	public String __getResultForUserId() {
		return StringUtils.isNotEmpty(targetUserId) ? targetUserId : userId;
	}

}
