package com.vedantu.cmds.pojos.requests;

import play.data.validation.Constraints.Required;

public class VisibleMappingReq extends GetSharedQuestionsBasicInfoReq{
    @Required
    public String parentOrgId;
    @Required
    public String sharedToOrgId;

    public boolean visible;
    public boolean isSelfVisible;
}
