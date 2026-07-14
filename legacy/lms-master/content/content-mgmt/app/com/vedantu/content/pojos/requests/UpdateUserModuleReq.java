package com.vedantu.content.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import com.vedantu.content.models.ModuleEntry;

public class UpdateUserModuleReq extends AbstractAppCheckReq{
    @Required
    public String  userId;
    @Required
    public String moduleId;
    @Required
    public ModuleEntry moduleEntry;
}
