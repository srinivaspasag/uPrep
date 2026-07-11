package com.vedantu.organization.pojos.responses.organizations;

import java.util.List;

import com.vedantu.commons.pojos.InputFieldInfo;
import com.vedantu.organization.enums.OrgMemberProfile;

public class GetOrgMemberExtraInfoInputFieldsRes {

    // update fields return to client
    public OrgMemberProfile     targetOrgMemberProfile;

    public List<InputFieldInfo> fields;

    public boolean enableOTP;
}
