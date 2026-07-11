package com.vedantu.user.pojos.requests;

import play.data.validation.Constraints.Required;

public class BlacklistEmailReq {

    @Required
    public String email;

    @Required
    public String reason;
}
