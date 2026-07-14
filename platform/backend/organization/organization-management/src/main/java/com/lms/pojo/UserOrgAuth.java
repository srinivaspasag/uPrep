package com.lms.pojo;

import com.lms.user.vedantu.user.pojo.responce.UserAuthRes;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserOrgAuth extends UserAuthRes {
    public String username;
    public String password;
    public String salt;
    public String memberId;
    public OrgProfile orgProfile = new OrgProfile();
}
