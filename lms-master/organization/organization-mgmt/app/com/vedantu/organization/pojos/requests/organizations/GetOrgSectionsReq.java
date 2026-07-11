package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.organization.enums.AccessScope;
import com.vedantu.organization.enums.RevenueModel;

public class GetOrgSectionsReq extends AbstractAuthCheckReq {

	@Required
	public String orgId;
	@Required
	public String programId;
	public String centerId;
	
	public AccessScope accessScope;
	public RevenueModel revenueModel;

}
