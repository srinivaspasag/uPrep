package com.vedantu.content.pojos.requests.schedules;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GetEntityScheduleInfoReq extends AbstractOrgScopeReq{

	@Required
	public SrcEntity entity;
}
