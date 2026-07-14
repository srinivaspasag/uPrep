package com.vedantu.organization.pojos.responses.organizations;

import com.vedantu.organization.enums.OrganizationStatus;

public class ApproveOrgRes {

	public String orgId;
	public boolean isNewUserAdded;
	public String adminUserId;
	public String adminPassword;
	public boolean isNewOrgMemberAdded;
	public String adminOrgMemberId;
	public OrganizationStatus status;

}
