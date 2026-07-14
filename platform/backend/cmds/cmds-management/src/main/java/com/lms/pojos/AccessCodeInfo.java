package com.lms.pojos;

import com.lms.common.vedantu.commons.pojos.requests.IListResponseObj;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelExtendedInfo;
import com.lms.enums.ShipmentStatus;
import com.lms.user.vedantu.user.pojo.UserInfo;

import java.util.List;

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
                          ShipmentStatus shipmentStatus, long orderId, long timeCreated, long lastUpdated) {
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
