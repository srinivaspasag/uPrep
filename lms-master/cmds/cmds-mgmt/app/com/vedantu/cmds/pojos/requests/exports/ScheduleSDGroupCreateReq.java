package com.vedantu.cmds.pojos.requests.exports;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class ScheduleSDGroupCreateReq extends AbstractOrgScopeReq {

    @Required
    public SrcEntity target;
    @Required
    public long      maxCardSize;
    @Required
    public String    name;
}
