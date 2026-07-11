package com.vedantu.user.pojos.requests;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;

public class SendForgotPasswordEmailReq extends AbstractAppCheckReq {

    @Required
    private String username;
    private String orgId;

    public String getUsername() {

        return username;
    }

    public void setUsername(String username) {

        this.username = StringUtils.lowerCase(username);
    }

    
    public String getOrgId() {
    
        return orgId;
    }

    
    public void setOrgId(String orgId) {
    
        this.orgId = orgId;
    }

}
