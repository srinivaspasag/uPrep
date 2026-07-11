package com.vedantu.organization.pojos.requests.members;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;

public class GetOrgMemberWithEmailReq extends AbstractAppCheckReq {

    @Required
    public String  orgId;

    @Required
    public String  email;

    public boolean ensureCourseInfo;
    public boolean getKey;
    public boolean loginStatusRequested;

}
