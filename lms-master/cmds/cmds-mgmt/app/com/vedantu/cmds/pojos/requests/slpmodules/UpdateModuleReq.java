package com.vedantu.cmds.pojos.requests.slpmodules;

import java.util.ArrayList;
import java.util.List;

import play.data.validation.Constraints.Required;

public class UpdateModuleReq extends AbstractModuleReq {

    @Required
    public String       id;
    public String       name;
    public String       prerequsiteModuleId;
    public List<String> updateList = new ArrayList<String>();

}
