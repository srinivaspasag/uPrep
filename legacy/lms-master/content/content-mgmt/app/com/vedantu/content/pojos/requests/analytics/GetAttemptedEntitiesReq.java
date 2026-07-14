package com.vedantu.content.pojos.requests.analytics;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GetAttemptedEntitiesReq extends AbstractOrgScopeReq {

    public EntityType   type;
    public List<String> ids;
    public long         attemptedAfter;

    @Required
    public String       targetUserId;

    public String _getResultForUserId() {

        return StringUtils.isEmpty(targetUserId) ? userId : targetUserId;
    }
}
