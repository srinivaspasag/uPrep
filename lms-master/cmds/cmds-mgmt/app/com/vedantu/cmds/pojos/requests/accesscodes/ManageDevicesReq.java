package com.vedantu.cmds.pojos.requests.accesscodes;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.cmds.enums.DeviceOperation;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class ManageDevicesReq extends AbstractOrgScopeReq{
	@Required
	public String accessCodeId;
	@Required
	public List<String> deviceIds;
	@Required
	public DeviceOperation operation;	
}