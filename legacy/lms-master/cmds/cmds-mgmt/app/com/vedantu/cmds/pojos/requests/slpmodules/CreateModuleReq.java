package com.vedantu.cmds.pojos.requests.slpmodules;

import play.data.validation.Constraints.Required;

public class CreateModuleReq extends AbstractModuleReq {

    @Required
    public String name;
    @Required
    public String folderId;

}
