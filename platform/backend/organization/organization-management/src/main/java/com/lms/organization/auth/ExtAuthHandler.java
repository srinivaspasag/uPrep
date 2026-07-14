package com.lms.organization.auth;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.enums.AuthType;
import com.lms.models.AddOrgMemberMappingReq;
import com.lms.models.OrgMember;
import com.lms.models.Organization;
import com.lms.pojo.responce.AddOrgMemberMappingRes;
import com.lms.user.vedantu.user.pojo.responce.UserAuthRes;
import com.lms.user.vedantu.user.requests.UserAuthReq;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExtAuthHandler extends AuthHandler {
    private static final String  CONTENT_TYPE_JSON = "application/json";
    private static final long    DEFAULT_TIMT_OUT  = 60L;
    private static final Logger LOGGER            = LoggerFactory.getLogger(ExtAuthHandler.class);

    public ExtAuthHandler(Organization org) {

        super(org, AuthType.EXT_AUTH_ORG);
    }

    @Override
    public UserAuthReq authenticate(String username, String password, OrgMember orgMember) throws VedantuException {

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
