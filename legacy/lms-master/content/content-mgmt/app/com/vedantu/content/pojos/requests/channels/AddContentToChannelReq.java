package com.vedantu.content.pojos.requests.channels;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class AddContentToChannelReq extends AbstractOrgScopeReq {

	@Required
	public String id;// channel id

	@Required
	public SrcEntity entity;
}
