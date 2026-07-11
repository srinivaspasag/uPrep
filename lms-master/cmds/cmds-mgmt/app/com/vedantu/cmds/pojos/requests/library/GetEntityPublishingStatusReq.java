package com.vedantu.cmds.pojos.requests.library;

import java.util.List;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetEntityPublishingStatusReq extends AbstractAuthCheckReq {

	public String orgId;
	public List<String> jobIds;
	
}
