package com.vedantu.organization.pojos.requests.organizations;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.enums.AccessScope;
import com.vedantu.organization.enums.RevenueModel;

public class AddOrgSectionReq extends AbstractAddOrgStructureReq {

	@Required
	public String programId;
	@Required
	public String centerId;

	public AccessScope scope;
	public RevenueModel revenueModel;
	public List<String> descriptionPoints;
	public String thumbnail;
	public String imageNameWithExtension;
}
