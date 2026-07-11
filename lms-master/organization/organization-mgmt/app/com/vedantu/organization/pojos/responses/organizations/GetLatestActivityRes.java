package com.vedantu.organization.pojos.responses.organizations;

import com.vedantu.commons.pojos.responses.IListResponseObj;

public class GetLatestActivityRes implements IListResponseObj{
	
	public String userId;
	public String orgId;
	public String deviceId;
	public String deviceType;
	public String page;
	public String action;
	public String entityName;
	public String entityid;
	public long   timeCreated  = 0L;
	
	public GetLatestActivityRes(String userId, String orgId, String deviceId,
			String deviceType, String page, String action,long timeCreated) {
		super();
		this.userId = userId;
		this.orgId = orgId;
		this.deviceId = deviceId;
		this.deviceType = deviceType;
		this.page = page;
		this.action = action;
		this.timeCreated=timeCreated;
	}
	
	

}
