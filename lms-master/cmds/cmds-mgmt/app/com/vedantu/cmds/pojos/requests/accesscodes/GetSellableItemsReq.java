package com.vedantu.cmds.pojos.requests.accesscodes;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.organization.enums.AccessScope;
import com.vedantu.organization.enums.RevenueModel;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GetSellableItemsReq extends AbstractOrgScopeReq {
	@Required
	public EntityType type;
	public String name;
	public RevenueModel revenueModel;
	public AccessScope accessScope;
	public int start;
	public int size;
}
