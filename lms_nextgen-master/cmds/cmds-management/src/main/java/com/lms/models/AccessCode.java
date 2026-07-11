package com.lms.models;

import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import com.lms.enums.ShipmentStatus;
import com.lms.pojos.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Document(value = "accesscodes")
public class AccessCode extends VedantuBaseMongoModel {

    public String code;
    public String creatorId;
    public ContactDetails buyerContactDetails;
    public List<ConsumableItem> items;
    public String userId;
    public Set<String> deviceIds;
    public String orgId;
    public SellerInfo sellerInfo;
    public ShipmentStatus shipmentStatus;
    public long orderId;
    public boolean verified = false;

    @Override
    public ModelBasicInfo toBasicInfo() {
        List<ConsumableItemInfo> itemsInfo = new ArrayList<ConsumableItemInfo>();
        for (ConsumableItem item : items) {
            ConsumableItemInfo consumableItemInfo = new ConsumableItemInfo();
//            VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(item.entity.type);
//            consumableItemInfo.info = dao.getBasicInfo(item.entity.id);
            consumableItemInfo.entity = item.entity;
            consumableItemInfo.verified = item.verified;
            consumableItemInfo.verifiedTime = item.verifiedTime;
            itemsInfo.add(consumableItemInfo);
        }
        AccessCodeInfo info = new AccessCodeInfo(_getStringId(), code, creatorId, buyerContactDetails,
                itemsInfo, userId, deviceIds != null ? new ArrayList<String>(deviceIds) : null, orgId, sellerInfo, shipmentStatus,
                orderId, timeCreated, lastUpdated);
        return info;
    }
}
