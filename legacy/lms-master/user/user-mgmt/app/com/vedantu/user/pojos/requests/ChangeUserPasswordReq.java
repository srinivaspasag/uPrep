package com.vedantu.user.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class ChangeUserPasswordReq  extends AbstractAuthCheckReq{

    @Required
    public String email;
    @Required
    public String targetUserId;
    @Required
    public String oldPassword;
    @Required
    public String newPassword;
}
