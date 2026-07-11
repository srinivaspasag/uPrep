package com.vedantu.organization.pojos.requests.members;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;

public class GetOrgMemberReq extends AbstractAppCheckReq {

    @Required
    public String  orgId;

    @Required
    public String  memberId;

    public boolean ensureCourseInfo;

    public boolean getKey;
    public boolean loginStatusRequested;

    public GetOrgMemberReq() {
        super();
    }

    public GetOrgMemberReq(String orgId, String memberId) {
        super();
        this.orgId = orgId;
        this.memberId = memberId;
    }
}
