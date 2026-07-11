package com.lms.pojo.responce;

import java.util.List;

import com.lms.common.vedantu.commons.pojos.requests.InputFieldInfo;
import com.lms.enums.OrgMemberProfile;


public class GetOrgMemberExtraInfoInputFieldsRes {

    // update fields return to client
    public OrgMemberProfile     targetOrgMemberProfile;

    public List<InputFieldInfo> fields;

    public boolean enableOTP;
}
