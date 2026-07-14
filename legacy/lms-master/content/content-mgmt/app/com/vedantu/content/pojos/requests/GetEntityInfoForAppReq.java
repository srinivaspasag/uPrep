package com.vedantu.content.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetEntityInfoForAppReq extends AbstractAuthCheckReq {

    @Required
    public SrcEntity entity;
    @Required
    public String orgId;

}
