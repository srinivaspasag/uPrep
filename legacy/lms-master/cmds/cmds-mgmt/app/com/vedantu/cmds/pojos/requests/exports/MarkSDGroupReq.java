package com.vedantu.cmds.pojos.requests.exports;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.CostRate;
import com.vedantu.organization.enums.AccessScope;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class MarkSDGroupReq extends AbstractOrgScopeReq {

    @Required
    public String             groupId;
    @Required
    public AccessScope state;

    public CostRate           costRate;
}
