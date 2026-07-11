package com.vedantu.organization.pojos.requests.members;

import play.data.validation.Constraints.Required;


public class MemberActivationPeriodsReq {
    @Required
    public String orgId;
    @Required
    public String userId;
    @Required
    public long from;
    @Required
    public long till;
}
