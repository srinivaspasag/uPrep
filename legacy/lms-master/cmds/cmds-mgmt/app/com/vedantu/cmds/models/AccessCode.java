package com.vedantu.cmds.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.cmds.enums.ShipmentStatus;
import com.vedantu.cmds.pojos.AccessCodeInfo;
import com.vedantu.cmds.pojos.ConsumableItem;
import com.vedantu.cmds.pojos.ConsumableItemInfo;
import com.vedantu.cmds.pojos.ContactDetails;
import com.vedantu.cmds.pojos.SellerInfo;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;

@Entity(value = "accesscodes", noClassnameStored = true)
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
    	for(ConsumableItem item : items){
    		ConsumableItemInfo consumableItemInfo = new ConsumableItemInfo();
      	 	 VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(item.entity.type);
    	 	 consumableItemInfo.info = dao.getBasicInfo(item.entity.id);
    	 	 consumableItemInfo.entity = item.entity;
    	 	 consumableItemInfo.verified = item.verified;
    	 	 consumableItemInfo.verifiedTime = item.verifiedTime;
    	 	 itemsInfo.add(consumableItemInfo);
    	}
    	AccessCodeInfo info =  new AccessCodeInfo(_getStringId(), code, creatorId, buyerContactDetails,
    			itemsInfo, userId, deviceIds!=null?new ArrayList<String>(deviceIds):null , orgId, sellerInfo, shipmentStatus, 
    					orderId, timeCreated, lastUpdated);
    	return info;
    }
}
