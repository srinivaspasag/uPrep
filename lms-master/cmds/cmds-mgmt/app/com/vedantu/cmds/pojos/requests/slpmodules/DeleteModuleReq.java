package com.vedantu.cmds.pojos.requests.slpmodules;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;


public class DeleteModuleReq extends AbstractOrgScopeReq{
     @Required
     public String id; 
}