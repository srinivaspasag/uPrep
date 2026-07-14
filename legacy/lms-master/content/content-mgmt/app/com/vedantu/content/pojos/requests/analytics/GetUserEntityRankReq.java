package com.vedantu.content.pojos.requests.analytics;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GetUserEntityRankReq extends AbstractOrgScopeReq {

    @Required
    public SrcEntity entity;

    @Required
    public String    targetUserId;
}
