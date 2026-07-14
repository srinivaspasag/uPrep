package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetOrgProgramCoursesReq extends AbstractAuthCheckReq {

	@Required
	public String orgId;
	@Required
	public String programId;

    public GetOrgProgramCoursesReq() {
        super();
    }

    public GetOrgProgramCoursesReq(String orgId, String programId) {
        super();
        this.orgId = orgId;
        this.programId = programId;
    }

}
