package com.vedantu.organization.pojos.responses.organizations;

import java.util.Set;

import com.vedantu.organization.pojos.OrgProgramCenterSections;

public class RemoveOrgProgramCentersRes extends AbstractOrgStructureRes {

	public Set<OrgProgramCenterSections> centerSections;
	public boolean isRemoved;
}
