package com.vedantu.user.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import com.vedantu.content.models.ModuleEntry;

public class UpdateUserSLPReq extends AbstractAppCheckReq{
    @Required
    public String  userId;
    public String orgId;
    public String moduleId;
    public ModuleEntry moduleEntry;
}
