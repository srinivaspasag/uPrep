package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.enums.OrgMemberProfile;

public class GetOrgMemberExtraInfoInputFieldsReq {

    @Required
    public OrgMemberProfile targetOrgMemberProfile;

    public boolean          checkIfSignupAllowed;

    public String           orgId;
}
