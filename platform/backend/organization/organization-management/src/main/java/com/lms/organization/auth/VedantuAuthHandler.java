package com.lms.organization.auth;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.enums.AuthType;
import com.lms.models.AddOrgMemberMappingReq;
import com.lms.models.OrgMember;
import com.lms.models.Organization;
import com.lms.pojo.responce.AddOrgMemberMappingRes;
import com.lms.user.vedantu.user.requests.UserAuthReq;

public class VedantuAuthHandler extends AuthHandler {

    public VedantuAuthHandler(Organization org) {

        super(org, AuthType.VEDANTU);

    }

    @Override
    public UserAuthReq authenticate(String username, String password ,OrgMember orgMember) throws VedantuException {

        super.checkOrgMember(orgMember);

        UserAuthReq userAuthReq = new UserAuthReq();

        userAuthReq.setUsername(getMemberUsername(organization._getStringId(), username));
        userAuthReq.setPassword(password);

        //UserAuthRes response = UserManager.authenticateUser(userAuthReq);

        return userAuthReq;
    }
    @Override
    public String getMemberUsername(String orgId, String memberId) {

        String memberUsername = (orgId + ":" + memberId).toLowerCase();
        return memberUsername;
    }

    @Override
    public AddOrgMemberMappingRes addMemberMapping(AddOrgMemberMappingReq addOrgMemberMappingReq, boolean noExceptionOnExistingMapping) {
        return null;
    }


}
