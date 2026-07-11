package com.vedantu.content.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.ScheduleInfo;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class ModuleScheduleReq extends AbstractOrgScopeReq{
    @Required
    public SrcEntity entity;
    @Required
    public SrcEntity source;
    @Required
    public SrcEntity target;
    public ScheduleInfo schedule;
}
