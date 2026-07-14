package com.vedantu.organization.pojos.requests.members;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetOrgMemberProfileReq extends AbstractAuthCheckReq {

    @Required
    public String  orgId;
    @Required
    public String  targetUserId;

    public boolean ensureCourseInfo;

    public boolean getKey;
}
