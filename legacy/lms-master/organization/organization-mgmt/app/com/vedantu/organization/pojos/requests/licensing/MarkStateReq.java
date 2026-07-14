package com.vedantu.organization.pojos.requests.licensing;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.organization.enums.PlanState;


public class MarkStateReq extends AbstractAuthCheckReq {
    @Required
    public String planId;
    @Required
    public PlanState state;
    
    
}
