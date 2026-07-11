package com.vedantu.organizations.auth;

import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.AuthType;
import com.vedantu.organization.models.Organization;
import com.vedantu.organization.pojos.requests.members.AddOrgMemberMappingReq;
import com.vedantu.organization.pojos.requests.members.AddOrgMemberReq;
import com.vedantu.organization.pojos.responses.members.AddOrgMemberMappingRes;
import com.vedantu.organization.pojos.responses.members.AddOrgMemberRes;
import com.vedantu.user.managers.UserManager;
import com.vedantu.user.pojos.requests.UserAuthReq;
import com.vedantu.user.pojos.responses.UserAuthRes;

public class VedantuAuthHandler extends AuthHandler {

    public VedantuAuthHandler(Organization org) {

        super(org, AuthType.VEDANTU);

    }

    @Override
    public UserAuthRes authenticate(String username, String password) throws VedantuException {

        super.checkOrgMember(username, organization._getStringId());

        UserAuthReq userAuthReq = new UserAuthReq();

        userAuthReq.setUsername(getMemberUsername(organization._getStringId(), username));
        userAuthReq.password = password;

        UserAuthRes response = UserManager.authenticateUser(userAuthReq);

        return response;
    }

    @Override
    public String getMemberUsername(String orgId, String memberId) {

        String memberUsername = StringUtils.lowerCase(orgId + ":" + memberId);
        return memberUsername;
    }

    @Override
    public boolean isUpdateValid(Set<String> updateList) throws VedantuException {

        return true;
    }

    @Override
    public AddOrgMemberRes addMember(AddOrgMemberReq req, boolean isMemberIdSysGenerated)
            throws VedantuException {
        if(!req.isOTPsignup)
            verifyExtraInputFields(req);
        return addOrgMember(req, isMemberIdSysGenerated);
    }

    @Override
    public AddOrgMemberMappingRes addMemberMapping(AddOrgMemberMappingReq addOrgMemberMappingReq,
            boolean noExceptionOnExistingMapping) throws VedantuException {

        checkIfAddMappingAllowed(addOrgMemberMappingReq);
        return addOrgMemberMapping(addOrgMemberMappingReq, noExceptionOnExistingMapping);
    }

}
