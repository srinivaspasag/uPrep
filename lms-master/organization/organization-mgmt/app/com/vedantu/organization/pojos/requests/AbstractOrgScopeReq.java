package com.vedantu.organization.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.organization.enums.OrgMemberProfile;

public class AbstractOrgScopeReq extends AbstractAuthCheckReq {

    @Required
    public String           orgId;

    public OrgMemberProfile orgMemberProfile;

    public String getOrgId() {
        return orgId;
    }

    public void setOrgId(String orgId) {
        this.orgId = orgId;
    }

    public OrgMemberProfile getOrgMemberProfile() {
        return orgMemberProfile;
    }

    public void setOrgMemberProfile(OrgMemberProfile orgMemberProfile) {
        this.orgMemberProfile = orgMemberProfile;
    }
}
