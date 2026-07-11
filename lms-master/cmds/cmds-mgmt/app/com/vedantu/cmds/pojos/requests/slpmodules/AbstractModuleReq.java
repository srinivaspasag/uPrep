package com.vedantu.cmds.pojos.requests.slpmodules;

import java.util.List;

import com.vedantu.content.models.ModuleRun;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public abstract class AbstractModuleReq extends AbstractOrgScopeReq {

    public ModuleRun    moduleRun;
    public List<String> tags;
    public List<String> brdIds;
    public List<String> targetIds;
    public String prerequsiteModuleID;
}
