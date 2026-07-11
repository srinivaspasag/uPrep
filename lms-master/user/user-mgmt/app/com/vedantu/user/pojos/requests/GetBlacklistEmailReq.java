package com.vedantu.user.pojos.requests;

import play.data.validation.Constraints.Required;

public class GetBlacklistEmailReq {

    @Required
    public String email;
}
