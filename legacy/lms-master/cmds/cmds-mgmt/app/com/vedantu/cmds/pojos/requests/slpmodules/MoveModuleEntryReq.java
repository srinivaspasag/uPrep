package com.vedantu.cmds.pojos.requests.slpmodules;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;


public class MoveModuleEntryReq extends AbstractOrgScopeReq{
    @Required
   public String moduleId;
    @Required
   public int oldPos;
    @Required
   public int pos;
}


