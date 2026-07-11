package com.vedantu.content.pojos.requests.schedules;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GetScheduleReq extends AbstractOrgScopeReq {
    public String programId;
    public String centerId;
    @Required
    public String sectionId;
    @Required
    public long month;
    public long day;
}
