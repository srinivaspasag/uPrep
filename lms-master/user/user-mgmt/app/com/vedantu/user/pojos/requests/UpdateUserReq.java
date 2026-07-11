package com.vedantu.user.pojos.requests;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.user.enums.Gender;

public class UpdateUserReq extends AbstractAuthCheckReq {

    @Required
    public String  targetUserId;
    @Required
    public String  firstName;
    public String  lastName;
    public String  dob;
    @Required
    public Gender  gender;
    private String email;

    public String orgId;
    public String orgName;
    public String password;

    public String getEmail() {

        return email;
    }

    public void setEmail(String email) {

        this.email = StringUtils.lowerCase(email);
    }

}
