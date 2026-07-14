package com.vedantu.content.pojos.requests;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

import play.data.validation.Constraints.Required;

public class GetEntityReq extends AbstractOrgScopeReq{
	@Required
	public EntityType         entityType;
	@Required
	public String			  entityId;
	@Required
	public String			  sectionId;
}
