package com.vedantu.organization.pojos;

import com.vedantu.user.pojos.responses.UserAuthRes;

public class UserOrgAuth extends UserAuthRes {
    public String username;
    public String password;
    public String salt;
    public String memberId;
    public OrgProfile orgProfile = new OrgProfile();
}
