package com.vedantu.organization.pojos.requests.members;

import java.util.List;

import com.vedantu.commons.pojos.requests.AbstractOrgListReq;
import com.vedantu.organization.enums.OrgMemberProfile;

public class GetOrgMembersReq extends AbstractOrgListReq {

	public OrgMemberProfile targetProfile;
	public List<OrgMemberProfile> excludeProfiles;
    public String programId;
	public String centerId;
	public String sectionId;
	public String courseId;
	public String query;
	public List<String> excludes;
	public Boolean canImpersonate;

}
