package com.vedantu.organization.pojos.requests.organizations;

import com.vedantu.organization.enums.ProgramCategory;

import play.data.validation.Constraints.Required;

public class AddOrgProgramReq extends AbstractAddOrgStructureReq {

    @Required
    public String          departmentId;
    public String          description;
    public long            periodStart;
    public long            periodEnd;
    public boolean         isOffline;
    public ProgramCategory category;
    public boolean         sharedProgramAccess;
}
