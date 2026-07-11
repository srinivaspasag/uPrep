package com.vedantu.cmds.pojos.requests;

import play.data.validation.Constraints.Required;

public class AddMappingsReq extends GetSharedQuestionsBasicInfoReq {
    @Required
    public String parentOrgId;
    @Required
    public String targetOrgId;
}
