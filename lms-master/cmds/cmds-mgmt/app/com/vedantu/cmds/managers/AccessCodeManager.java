package com.vedantu.cmds.managers;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.CollectionUtils;

import play.Logger;
import play.Logger.ALogger;
import play.Play;

import com.vedantu.billing.dao.OrderDAO;
import com.vedantu.billing.enums.ItemCategory;
import com.vedantu.billing.managers.OrderManager;
import com.vedantu.billing.models.Order;
import com.vedantu.billing.pojos.AddressTo;
import com.vedantu.billing.pojos.OrderItemDetails;
import com.vedantu.cmds.daos.AccessCodeDAO;
import com.vedantu.cmds.enums.DeviceOperation;
import com.vedantu.cmds.enums.ShipmentStatus;
import com.vedantu.cmds.models.AccessCode;
import com.vedantu.cmds.pojos.AccessCodeInfo;
import com.vedantu.cmds.pojos.ConsumableItem;
import com.vedantu.cmds.pojos.ConsumableItemInfo;
import com.vedantu.cmds.pojos.SellableItemInfo;
import com.vedantu.cmds.pojos.requests.accesscodes.GenerateAccessCodeReq;
import com.vedantu.cmds.pojos.requests.accesscodes.GenerateBulkAccessCodesReq;
import com.vedantu.cmds.pojos.requests.accesscodes.GetAccessCodesReq;
import com.vedantu.cmds.pojos.requests.accesscodes.GetSellableItemsReq;
import com.vedantu.cmds.pojos.requests.accesscodes.ManageDevicesReq;
import com.vedantu.cmds.pojos.requests.accesscodes.ResendEmailReq;
import com.vedantu.cmds.pojos.requests.accesscodes.UpdateShipmentStatusReq;
import com.vedantu.cmds.pojos.requests.accesscodes.VerifyAccessCodeReq;
import com.vedantu.cmds.pojos.responses.accessCodes.GenerateAccessCodeRes;
import com.vedantu.cmds.pojos.responses.accessCodes.GenerateBulkAccessCodesRes;
import com.vedantu.cmds.pojos.responses.accessCodes.GetAccessCodesRes;
import com.vedantu.cmds.pojos.responses.accessCodes.GetSellableItemsRes;
import com.vedantu.cmds.pojos.responses.accessCodes.ManageDevicesRes;
import com.vedantu.cmds.pojos.responses.accessCodes.ResendEmailRes;
import com.vedantu.cmds.pojos.responses.accessCodes.UpdateShipmentStatusRes;
import com.vedantu.cmds.pojos.responses.accessCodes.VerifyAccessCodeRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.Configurations;
import com.vedantu.commons.constants.configuration.EmailConfigurationConstants;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.DeviceType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.enums.EventType;
import com.vedantu.commons.http.URLGenerator;
import com.vedantu.commons.pojos.CostRate;
import com.vedantu.commons.pojos.Interval;
import com.vedantu.commons.pojos.SellableItemDetails;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.user.event.details.AccessCodeDetails;
import com.vedantu.user.managers.AbstractVedantuEventManager;

public class AccessCodeManager extends AbstractVedantuEventManager {

    public static AccessCodeManager INSTANCE = new AccessCodeManager();

    private final static ALogger    LOGGER   = Logger.of(AccessCodeManager.class);

    public AccessCodeManager() {

    }

    public GenerateAccessCodeRes generateAccessCode(GenerateAccessCodeReq request)
            throws VedantuException {

        AccessCode accessCode = new AccessCode();
        accessCode.buyerContactDetails = request.buyerContactDetails;
        accessCode.creatorId = request.userId;
        accessCode.orgId = request.orgId;
        accessCode.sellerInfo = request.sellerInfo;
        accessCode.shipmentStatus = request.shipmentStatus;
        Set<SrcEntity> entitiesSet = new HashSet<SrcEntity>(request.entities);

        if (CollectionUtils.isNotEmpty(request.entities)) {
            List<ConsumableItem> items = new ArrayList<ConsumableItem>();
            for (SrcEntity entity : entitiesSet) {
                ConsumableItem item = new ConsumableItem();
                item.entity = entity;
                SellableItemDetails sellableItemDetails = OrderManager
                        .getSellableItemDetails(entity);
                item.__setItemSellableDetails(sellableItemDetails);
                item.costRate = sellableItemDetails.costRate;
                items.add(item);
            }
            accessCode.items = items;
        }

        // TODO: add desc
        long orderId = createOrder(request.userId, accessCode.buyerContactDetails.email,
                accessCode.items, accessCode.buyerContactDetails.billingAddress,
                accessCode.buyerContactDetails.shipmentAddress, /* TODO add desc */
                null, accessCode.sellerInfo.pointOfSale, accessCode.sellerInfo.sellerReferenceNo);

        accessCode.orderId = orderId;
        AccessCode accessCodeRes = AccessCodeDAO.INSTANCE.generateAccessCode(accessCode);
        GenerateAccessCodeRes getAccessCodeRes = new GenerateAccessCodeRes();
        getAccessCodeRes.accessCode = (AccessCodeInfo) accessCodeRes.toBasicInfo();
        generateAccessCodeEmail(request.userId, request.buyerContactDetails.email, "",
                getAccessCodeRes.accessCode.code, request.sellerInfo.pointOfSale,
                request.sellerInfo.sellerReferenceNo); // //TODO check null
                                                       // reference

        return getAccessCodeRes;
    }

    public GenerateBulkAccessCodesRes generateBulkAccessCodes(GenerateBulkAccessCodesReq request)
            throws VedantuException{
            GenerateAccessCodeReq req = new GenerateAccessCodeReq();
            req.buyerContactDetails = request.buyerContactDetails;
            req.sellerInfo = request.sellerInfo;
            req.entities = request.entities;
            req.userId = request.userId;
            req.orgId = request.orgId;
            req.shipmentStatus = request.shipmentStatus;
            int count = request.count;
            for(int i=0 ; i < count ; i++){
                generateAccessCode(req);
            }
            boolean done = true;
            GenerateBulkAccessCodesRes res = new GenerateBulkAccessCodesRes();
            res.done = done;
            return res;
    }

    private long createOrder(String userId, String email, List<ConsumableItem> items,
            AddressTo billingAddress, AddressTo shipmentAddress, String description,
            String pointOfSale, String sellerReferenceNo) throws VedantuException {

        SrcEntity customer = new SrcEntity(EntityType.USER, getUnregistredCustomerIdentifier(email));

        Order order = OrderManager.createOrder(userId, DeviceType.WEB, customer);
        order.tempUser = true;
        ItemCategory category = ItemCategory.CONTENT;
        CostRate overAllCostRate = new CostRate();
        if (items.get(0).costRate == null) {
            throw new VedantuException(VedantuErrorCode.COST_RATE_NOT_DEFINED_FOR_ITEM);
        }

        String currencyCode = items.get(0).costRate.currencyCode;

        for (ConsumableItem item : items) {
            if (item.costRate == null) {
                if (items.get(0).costRate == null) {
                    throw new VedantuException(VedantuErrorCode.COST_RATE_NOT_DEFINED_FOR_ITEM);
                }
            }
            String itemCurrencyCode = item.costRate.currencyCode;
            if (!itemCurrencyCode.equalsIgnoreCase(currencyCode)) {
                throw new VedantuException(VedantuErrorCode.ITEMS_HAVE_DIFFERENT_CURRENCY_CODE);
            }
        }
        int totalCost = 0;
        for (ConsumableItem item : items) {
            totalCost += item.costRate.value;
            if (item.entity.type == EntityType.SECTION) {
                category = ItemCategory.SECTION;
            } else if (item.entity.type == EntityType.SDCARDGROUP) {
                category = ItemCategory.SDCARDGROUP;
            }
            OrderItemDetails details = new OrderItemDetails();
            details.period = new Interval(Calendar.getInstance().getTimeInMillis(), -1);
            OrderManager.addItemToOrder(order, item.__getItemSellableDetails(), category, 1,
                    description, details,
                    shipmentAddress != null ? shipmentAddress.location : null, true);
        }
        overAllCostRate.currencyCode = currencyCode;
        overAllCostRate.value = totalCost;
        order.billingEmail = email;
        OrderManager.createThirdPartyTransaction(userId, order, DeviceType.WEB, overAllCostRate,
                pointOfSale, sellerReferenceNo, customer, billingAddress, shipmentAddress);
        OrderDAO.INSTANCE.save(order);
        return order.orderId;

    }

    public VerifyAccessCodeRes verifyAccessCode(VerifyAccessCodeReq request)
            throws VedantuException {

        AccessCode accessCode = AccessCodeDAO.INSTANCE.getByCodeAndEmailId(request.code,
                request.email);
        if (accessCode.deviceIds == null) {
            accessCode.deviceIds = new HashSet<String>();
            accessCode.deviceIds.add(request.deviceId);
            accessCode.userId = request.userId;
            accessCode.shipmentStatus = ShipmentStatus.RECEIVED;
        }
        if (accessCode.buyerContactDetails.email == null
                || accessCode.buyerContactDetails.email.equals("")) {
            accessCode.buyerContactDetails.email = request.email;
        }
        else if (!accessCode.deviceIds.contains(request.deviceId)) {
            throw new VedantuException(VedantuErrorCode.ITEM_ALREADY_VERIFIED_WITH_DIFFERENT_DEVICE);
        }

        else if (!accessCode.userId.equalsIgnoreCase(request.userId)) {
            throw new VedantuException(VedantuErrorCode.ITEM_ALREADY_VERIFIED_FOR_DIFFERENT_USER);
        }

        boolean itemFound = false;
        boolean sectionFound = false;
        if (accessCode.items != null && CollectionUtils.isNotEmpty(accessCode.items)) {
            for (ConsumableItem item : accessCode.items) {
                if (item.entity.type.equals(EntityType.SECTION)) {
                    sectionFound = true;
                }
                if (item.entity.equals(request.entity)) {
                    itemFound = true;
                    if (!item.verified) {
                        item.verifiedTime = new Date().getTime();
                        item.verified = true;
                    }
                }

            }
        }
        if (!itemFound) {
            throw new VedantuException(VedantuErrorCode.INVALID_ITEM);
        }

        AccessCode accessCodeRes = AccessCodeDAO.INSTANCE.update(accessCode);
        VerifyAccessCodeRes verifyAccessCodeRes = new VerifyAccessCodeRes();
        verifyAccessCodeRes.accessCode = accessCodeRes;

        String transactionId = OrderManager.getTransactionByOrderId(accessCode.orderId)
                ._getStringId();

        // need not call this method in case of an item present as program -->
        // by Shankar
        // OrderManager.markTransactionConmpleted(transactionId, null);
        // we can also make check if the order is already verified, then we will
        // not update the
        // order
        if (!sectionFound) {
            OrderManager.updateOrderOnUserVerification(accessCode.orderId, request.userId);
        }

        verifyAccessCodeRes.orderId = accessCode.orderId;
        verifyAccessCodeRes.transactionId = transactionId;

        return verifyAccessCodeRes;
    }

    @SuppressWarnings("unchecked")
    public GetAccessCodesRes getAccessCodes(GetAccessCodesReq request) throws VedantuException {

        GetAccessCodesRes res = new GetAccessCodesRes();
        ListResponse<AccessCodeInfo> accessCodes = AccessCodeDAO.INSTANCE.getAccessCodes(
                request.orgId, request.buyerEmail, request.pointOfSale, request.sellerReferenceNo,
                request.forUserId, request.start, request.size, request.orderBy, request.sortOrder);
        res.accessCodes = accessCodes;
        return res;
    }

    public ManageDevicesRes deviceManagement(ManageDevicesReq request) throws VedantuException {

        AccessCode accessCode = AccessCodeDAO.INSTANCE.getAccessCodeById(request.accessCodeId);
        if (accessCode.userId == null || CollectionUtils.isEmpty(accessCode.deviceIds)) {
            throw new VedantuException(VedantuErrorCode.ACCESS_CODE_NOT_VERIFIED);
        }

        if (request.operation == DeviceOperation.ADD) {
            if (accessCode.deviceIds == null) {
                accessCode.deviceIds = new HashSet<String>();
            }
            accessCode.deviceIds.addAll(request.deviceIds);
        } else if (request.operation == DeviceOperation.REMOVE) {
            if (CollectionUtils.isNotEmpty(accessCode.deviceIds)) {
                accessCode.deviceIds.removeAll(request.deviceIds);
            }
        }
        accessCode = AccessCodeDAO.INSTANCE.update(accessCode);
        ManageDevicesRes res = new ManageDevicesRes();
        res.deviceIds = accessCode.deviceIds != null ? new ArrayList<String>(accessCode.deviceIds)
                : null;
        return res;
    }

    public ResendEmailRes resendEmail(ResendEmailReq request) throws VedantuException {

        ResendEmailRes resendEmailRes = new ResendEmailRes();
        AccessCode accessCode = AccessCodeDAO.INSTANCE.getAccessCodeById(request.accessCodeId);
        List<ConsumableItem> items = new ArrayList<ConsumableItem>();

        for (ConsumableItem item : items) {
            ConsumableItemInfo consumableItemInfo = new ConsumableItemInfo();
            VedantuBasicDAO dao = EntityTypeDAOFactory.INSTANCE.get(item.entity.type);
            ModelBasicInfo info = dao.getBasicInfo(item.entity.id);

        }
        resendEmailRes.success = generateAccessCodeEmail(request.userId, accessCode.buyerContactDetails.email, "",
                accessCode.code, accessCode.sellerInfo.pointOfSale,
                accessCode.sellerInfo.sellerReferenceNo); // //TODO
        return resendEmailRes;
    }

    public UpdateShipmentStatusRes updateShipmentStatus(UpdateShipmentStatusReq request)
            throws VedantuException {

        UpdateShipmentStatusRes updateShipmentStatusRes = new UpdateShipmentStatusRes();
        AccessCode accessCode = AccessCodeDAO.INSTANCE.getAccessCodeById(request.accessCodeId);
        accessCode.shipmentStatus = request.shipmentStatus;
        AccessCode accessCodeRes = AccessCodeDAO.INSTANCE.update(accessCode);
        updateShipmentStatusRes.shipmentStatus = accessCodeRes.shipmentStatus;// //TODO
        return updateShipmentStatusRes;
    }

    private static boolean generateAccessCodeEmail(String userId, String email, String name, String code,
            String pointOfSale, String sellerReferenceNo) throws VedantuException {

        final String emailVerifyHost = Play.application().configuration()
                .getString(Configurations.APP_HOST);
        final String emailVerifyEndPoint = Play.application().configuration()
                .getString(EmailConfigurationConstants.EMAIL_VERIFICATION_ENDPOINT);

        URLGenerator generator = new URLGenerator();
        generator.host = emailVerifyHost;
        generator.endPoint = emailVerifyEndPoint;
        generator.protocol = Play.application().configuration()
                .getString(Configurations.APP_PROTOCOL);

        Map<String, Object> params = new HashMap<String, Object>();
        try {
            params.put("email", URLEncoder.encode(email, "UTF-8"));
            params.put("name", URLEncoder.encode(name, "UTF-8"));
            params.put("code", URLEncoder.encode(code, "UTF-8"));
            params.put("pointOfSale", URLEncoder.encode(pointOfSale, "UTF-8"));
            params.put("sellerReferenceNo", URLEncoder.encode(sellerReferenceNo, "UTF-8"));
        } catch (UnsupportedEncodingException ex) {
            LOGGER.error(" unsupported URL found", ex);
            throw new VedantuException(VedantuErrorCode.INVALID_URL);
        }

        generator.params = params;

        AccessCodeDetails details;
        try {
            details = new AccessCodeDetails();
        } catch (ClassNotFoundException e) {
            Logger.debug(" Not found access code details", e);
            throw new VedantuException(VedantuErrorCode.EMAIL_CAN_NOT_BE_SEND);
        }

        details.name = name;
        details.code = code;
        details.email = email;
        details.pointOfSale = pointOfSale;
        details.sellerReferenceNo = sellerReferenceNo;
        details.addRecepient(details.name, details.email);
        if (!email.equals("")){
             generateEventAysc(userId, details, EventType.SEND_EMAIL);
        }

        return true;
    }

    public GetSellableItemsRes getSellableItems(GetSellableItemsReq request)
            throws VedantuException {

        GetSellableItemsRes res = new GetSellableItemsRes();
        @SuppressWarnings("unchecked")
        ListResponse<SellableItemInfo> sellableItems = AccessCodeDAO.INSTANCE.getSellableItems(
                request.orgId, request.type, request.name, request.revenueModel,
                request.accessScope, request.start, request.size);
        res.sellableItems = sellableItems;
        return res;
    }

    private String getUnregistredCustomerIdentifier(String email) {

        return "email/" + email;
    }

    // @SuppressWarnings("unchecked")
    // public GetAccessCodesRes getAccessCodes1(GetAccessCodesReq request)
    // throws VedantuException {
    //
    // GetAccessCodesRes res = new GetAccessCodesRes();
    // ListResponse<AccessCodeInfo> accessCodes = AccessCodeDAO.INSTANCE
    // .getAccessCodes(request.buyerEmail, request.pointOfSell,
    // request.sellerReferenceNo, request.start, request.size);
    // res.accessCodes = accessCodes;
    // return res;
    // }

}
