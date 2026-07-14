package com.vedantu.user.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;

public class GetUserEmailSubscriptionReq extends AbstractAppCheckReq {

    @Required
    public String userId;
    @Required
    public String targetUserId;

}
