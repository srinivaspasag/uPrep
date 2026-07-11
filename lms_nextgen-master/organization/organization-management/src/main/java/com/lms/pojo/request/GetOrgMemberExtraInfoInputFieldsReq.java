package com.lms.pojo.request;

import com.lms.enums.OrgMemberProfile;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetOrgMemberExtraInfoInputFieldsReq {

    public OrgMemberProfile targetOrgMemberProfile;

    public boolean checkIfSignupAllowed;

    public String orgId;
}
