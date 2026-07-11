package com.vedantu.user.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;

public class UnsubscribeReq extends AbstractAppCheckReq {
    @Required
    public String  userId;
    @Required
    public String  targetUserId;
    @Required
    public String  mailCategory;
    public boolean external = false;
    public String  reason;

}