package com.vedantu.organization.pojos.requests;

import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;

import play.data.validation.Constraints.Required;

public class GetAllUserDataReq extends AbstractAppCheckReq{
    @Required
    public String orgId;
    public String targetUserId;
    public long lastUpdated = Long.MIN_VALUE;
}
