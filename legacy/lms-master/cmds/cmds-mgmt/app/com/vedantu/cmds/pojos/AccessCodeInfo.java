package com.vedantu.cmds.pojos;

import java.util.List;

import com.vedantu.cmds.enums.ShipmentStatus;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.pojos.responses.ModelExtendedInfo;
import com.vedantu.user.pojos.UserInfo;

public class AccessCodeInfo extends ModelExtendedInfo implements
		IListResponseObj {
	public String id;
	public String code;
	public String creatorId;
	public ContactDetails buyerContactDetails;
	public List<ConsumableItemInfo> items;
	public String userId;
	public List<String> deviceIds;
	public String orgId;
	public SellerInfo sellerInfo;
	public ShipmentStatus shipmentStatus;
	public long orderId;
	public UserInfo userInfo;

	public AccessCodeInfo() {

	}

	public AccessCodeInfo(String id, String code, String creatorId,
			ContactDetails buyerContactDetails,
			List<ConsumableItemInfo> itemsInfo, String userId,
			List<String> deviceIds, String orgId, SellerInfo sellerInfo,
			ShipmentStatus shipmentStatus, long orderId, long timeCreated, long lastUpdated ) {
		this.id = id;
		this.code = code;
		this.creatorId = creatorId;
		this.buyerContactDetails = buyerContactDetails;
		this.items = itemsInfo;
		this.userId = userId;
		this.deviceIds = deviceIds;
		this.orgId = orgId;
		this.sellerInfo = sellerInfo;
		this.shipmentStatus = shipmentStatus;
		this.orderId = orderId;
		this.timeCreated = timeCreated;
		this.lastUpdated = lastUpdated;

	}

}
