package com.vedantu.cmds.pojos;

import java.util.List;

import com.vedantu.cmds.enums.ShipmentStatus;

public class AccessCodeBasicInfo {
	public String id;
	public String code;
	public String creatorId;
	public ContactDetails buyerContactDetails;
	public List<ConsumableItem> items;
	public String userId;
	public List<String> deviceIds;
	public String orgId;
	public SellerInfo sellerInfo;
    public ShipmentStatus shipmentStatus;
	
    public AccessCodeBasicInfo() {
		
	}
	
    public AccessCodeBasicInfo(String id, String code, String creatorId, ContactDetails buyerContactDetails,
    		List<ConsumableItem> items, String userId, List<String> deviceIds, 
    		String orgId, SellerInfo sellerInfo, ShipmentStatus shipmentStatus) {
    	this.id = id;
		this.code =code;
		this.creatorId = creatorId;
		this.buyerContactDetails = buyerContactDetails;
		this.items = items;
		this.userId = userId;
		this.deviceIds = deviceIds;
		this.orgId = orgId;
		this.sellerInfo = sellerInfo;
		this.shipmentStatus = shipmentStatus;
		
	}
	
}
