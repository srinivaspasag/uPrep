package com.vedantu.content.pojos.requests;

import java.util.List;

import com.vedantu.content.models.ModuleRun;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public abstract class AbstractModuleReq extends AbstractOrgScopeReq {

    public ModuleRun    moduleRun;
    public List<String> tags;
    public List<String> brdIds;
    public List<String> targetIds;
}   