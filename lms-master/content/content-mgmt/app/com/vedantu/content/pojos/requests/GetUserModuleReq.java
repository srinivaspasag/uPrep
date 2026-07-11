package com.vedantu.content.pojos.requests;

import play.data.validation.Constraints.Required;
import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;

public class GetUserModuleReq extends AbstractAppCheckReq{
    @Required
    public String  userId;
    @Required
    public String moduleId;
}
