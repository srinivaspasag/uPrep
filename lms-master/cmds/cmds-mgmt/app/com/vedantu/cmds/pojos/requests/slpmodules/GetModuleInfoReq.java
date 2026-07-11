package com.vedantu.cmds.pojos.requests.slpmodules;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;


public class GetModuleInfoReq extends AbstractOrgScopeReq{
	@Required
    public String id;
	public String sectionId;
	public SrcEntity target;

}
