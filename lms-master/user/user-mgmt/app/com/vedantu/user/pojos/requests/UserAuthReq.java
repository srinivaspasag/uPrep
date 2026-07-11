package com.vedantu.user.pojos.requests;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;

public class UserAuthReq extends AbstractAppCheckReq {

    @Required
    private String username;
    public String contactNumber;
    public String countryCode;
    public String  orgId;
    public String  password;
    public boolean isOTPlogin;
    public String progType;
    public boolean isNewPhone;

    public boolean dl = false;

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = StringUtils.lowerCase(username);
    }

}
