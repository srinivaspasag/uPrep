package com.vedantu.organization.pojos.requests.organizations;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.InputFieldInfo;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class UpdateOrgMemberExtraInfoInputFieldsReq extends AbstractOrgScopeReq {

    @Required
    public OrgMemberProfile     targetOrgMemberProfile;

    public List<InputFieldInfo> fields;

}
