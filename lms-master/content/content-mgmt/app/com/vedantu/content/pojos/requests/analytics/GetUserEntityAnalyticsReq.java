package com.vedantu.content.pojos.requests.analytics;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GetUserEntityAnalyticsReq extends AbstractOrgScopeReq {

    @Required
    public SrcEntity entity;
    public SrcEntity target;

    @Required
    public String    targetUserId;

    public String _getResultForUserId() {

        return StringUtils.isEmpty(targetUserId) ? userId : targetUserId;
    }

}
