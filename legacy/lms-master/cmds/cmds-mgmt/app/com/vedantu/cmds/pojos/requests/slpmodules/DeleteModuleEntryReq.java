package com.vedantu.cmds.pojos.requests.slpmodules;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;


public class DeleteModuleEntryReq extends AbstractOrgScopeReq{
    @Required
    public String moduleId;
    public int pos;
}
