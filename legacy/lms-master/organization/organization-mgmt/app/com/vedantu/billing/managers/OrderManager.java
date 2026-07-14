package com.vedantu.billing.managers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Logger.ALogger;
import play.Play;
import play.mvc.Http;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.mongodb.BasicDBList;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.vedantu.billing.dao.CouponCodeDAO;
import com.vedantu.billing.dao.OrderDAO;
import com.vedantu.billing.dao.TransactionDAO;
import com.vedantu.billing.enums.CouponType;
import com.vedantu.billing.enums.ItemCategory;
import com.vedantu.billing.enums.OrderState;
import com.vedantu.billing.enums.TransactionStatus;
import com.vedantu.billing.enums.TransactionType;
import com.vedantu.billing.models.CouponCode;
import com.vedantu.billing.models.Order;
import com.vedantu.billing.models.OrderedItem;
import com.vedantu.billing.models.Tax;
import com.vedantu.billing.models.Transaction;
import com.vedantu.billing.payment.managers.AbstractPaymentManager;
import com.vedantu.billing.payment.managers.VedantuTransactionManager;
import com.vedantu.billing.pojos.AddressTo;
import com.vedantu.billing.pojos.InvoiceInfo;
import com.vedantu.billing.pojos.OrderInfo;
import com.vedantu.billing.pojos.OrderItemDetails;
import com.vedantu.billing.pojos.SectionItemInfo;
import com.vedantu.billing.pojos.requests.ApplyCouponReq;
import com.vedantu.billing.pojos.requests.ApplyLPCreditsReq;
import com.vedantu.billing.pojos.requests.ConfirmPaymentReq;
import com.vedantu.billing.pojos.requests.GenerateOrderInfoReq;
import com.vedantu.billing.pojos.requests.GetBuyOrdersReq;
import com.vedantu.billing.pojos.requests.GetOrderItemInfoReq;
import com.vedantu.billing.pojos.requests.GetSellOrdersReq;
import com.vedantu.billing.pojos.requests.GetTransactionStatusReq;
import com.vedantu.billing.pojos.requests.StartTransactionReq;
import com.vedantu.billing.pojos.requests.UpdateTransactionReq;
import com.vedantu.billing.pojos.responses.ApplyCouponRes;
import com.vedantu.billing.pojos.responses.ApplyLPCreditsRes;
import com.vedantu.billing.pojos.responses.ConfirmPaymentRes;
import com.vedantu.billing.pojos.responses.GetOrderItemInfoRes;
import com.vedantu.billing.pojos.responses.GetOrdersRes;
import com.vedantu.billing.pojos.responses.GetTransactionStatusRes;
import com.vedantu.billing.pojos.responses.OnPaymentReceivedRes;
import com.vedantu.billing.pojos.responses.StartTransactionRes;
import com.vedantu.billing.pojos.responses.UpdateTransactionRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.content.interfaces.ISellableEntity;
import com.vedantu.commons.daos.CounterDAO;
import com.vedantu.commons.entity.factory.EntityTypeDAOFactory;
import com.vedantu.commons.enums.DeviceType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.exceptions.ExportException;
import com.vedantu.commons.pojos.CostRate;
import com.vedantu.commons.pojos.Interval;
import com.vedantu.commons.pojos.Location;
import com.vedantu.commons.pojos.SellableItemDetails;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.commons.utils.JSONUtils;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.mongo.VedantuBaseMongoModel;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuDBResult;
import com.vedantu.mongo.VedantuRecordState;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.OrgSectionDAO;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.enums.OrgMemberProfile;
import com.vedantu.organization.managers.OrgMemberManager;
import com.vedantu.organization.models.OrgMember;
import com.vedantu.organization.models.OrgSection;
import com.vedantu.organization.models.Organization;
import com.vedantu.organization.pojos.PackageInfo;
import com.vedantu.organization.pojos.requests.members.GetWalletBalanceReq;
import com.vedantu.user.daos.UserDAO;
import com.vedantu.user.models.User;

public class OrderManager {

    // OrderManager manager need to be moved to org-mgmt project as we need it in org-mgmt as well
    // as in billing-mgmt so keeping it in billing-mgmt was creating a cyclic dependency, hence
    // moving it to org-mgmt

    private final static ALogger LOGGER = Logger.of(OrderManager.class);

    /**
     * Order workflow
     *
     * Order created => Order Edited => Order Confirmed => [ Order Cancelled ] => Order processed =>
     * [Order shipped / fulfilled / cancelled ]
     *
     *
     */

    public static Order createOrder(String userId, DeviceType deviceType, SrcEntity customer)
            throws VedantuException {

        long orderId = CounterDAO.INSTANCE.getNextSequence(OrderDAO.INSTANCE.getCollection()
                .getName(), Order.ORDER_ID);
        Order order = new Order(userId, deviceType, customer, orderId);
        OrderDAO.INSTANCE.save(order);

        return order;
    }

    public static StartTransactionRes startTransaction(StartTransactionReq req)
            throws VedantuException {

        User user = null;
        if (EntityType.USER.equals(req.customer.type)) {
            user = UserDAO.INSTANCE.getById(req.customer.id);
            if (user == null) {
                throw new VedantuException(VedantuErrorCode.USER_NOT_FOUND,
                        "no user found with id: " + req.customer.id);
            }
        }

        VedantuTransactionManager transactionManager = VedantuTransactionManager.getInstance();

        Order order = createOrder(req.userId, req.deviceType, req.customer);
        OrderItemDetails orderItemDetails = new OrderItemDetails();

        LOGGER.debug("Package days: " + req.packageDays);
        SellableItemDetails sellableItemInfo = null;
        if (req.item.type.equals(EntityType.SECTION)) {
            sellableItemInfo = getSellableSectionDetails(req.item, req.packageOrgId, req.packageDays);
            long currentTimeMillis = System.currentTimeMillis();
            long periodTill = currentTimeMillis + TimeUnit.DAYS.toMillis(req.packageDays);
            orderItemDetails.period = new Interval(System.currentTimeMillis(), periodTill);
        } else {
            sellableItemInfo = getSellableItemDetails(req.item);
            orderItemDetails.period = new Interval(System.currentTimeMillis(), Interval.NO_END);
        }

        addItemToOrder(order, sellableItemInfo, ItemCategory.valueOf(req.item.type.name()), 1,
                StringUtils.EMPTY, orderItemDetails, new Location());

        String transactionId = transactionManager.getVedantuTransactionId(req.userId,
                order.orderId, req.paymentChannel, req.deviceType, sellableItemInfo.costRate.value,
                sellableItemInfo.costRate.currencyCode);
        StartTransactionRes res = new StartTransactionRes();
        res.transactionId = transactionId;
        res.orderId = order.toStringOrderId();
        res.orderTotal = order.toStringOrderTotal();
        res.paymentUrl = Play.application().configuration().getString("payment.page.url");

        // Put the logic for payment channel here.
        res.paymentChannel = getPaymentChannel(req.orgId);
        if (EntityType.USER.equals(req.customer.type)) {
            res.email = StringUtils.defaultString(user._getCommunicationEmail(), StringUtils.EMPTY);
            res.phone = StringUtils.defaultString(OrgMemberDAO.INSTANCE.getByUserId(user._getStringId()).contactNumber, StringUtils.EMPTY);
            res.needEmail = !user.isEmailVerified;
        }
        return res;
    }

    public static GetTransactionStatusRes getTransactionStatus(GetTransactionStatusReq req)
            throws VedantuException {

        Transaction transaction = TransactionDAO.INSTANCE.getTransactionByOrderId(req.orderId);
        GetTransactionStatusRes res = new GetTransactionStatusRes(transaction.deviceType,
                transaction._getStringId(), transaction.orderId, transaction.status,
                transaction.item, transaction.item_sku, transaction.callbackUrl,
                transaction.consumed, transaction.amount);
        return res;
    }

    public static GetOrderItemInfoRes getOrderItemInfo(GetOrderItemInfoReq req)
            throws VedantuException {

        GetOrderItemInfoRes res = new GetOrderItemInfoRes();

        Order order = OrderDAO.INSTANCE.getOrderById(req.orderId);
        Transaction transaction = TransactionDAO.INSTANCE.getTransactionByOrderId(req.orderId);
        res.orderId = String.valueOf(transaction.orderId);
        res.transactionId = transaction._getStringId();
        // for now only the 1st item is returned
        if (order.items.size() > 0) {
            OrderedItem orderedItem = order.items.get(0);
            if (EntityType.SECTION.equals(orderedItem.item.type)) {
                SectionItemInfo sInfo = new SectionItemInfo();
                res.item = sInfo;
                sInfo.name = orderedItem.name;
                sInfo.id = orderedItem.item.id;
                sInfo.type = orderedItem.item.type;
                OrgSection section = OrgSectionDAO.INSTANCE.getById(res.item.id);
                sInfo.centerId = section.centerId;
                sInfo.programId = section.programId;
                sInfo.sectionId = section._getStringId();
            }
        }
        return res;
    }

    public static OrderInfo getOrderInfo(GenerateOrderInfoReq req) throws VedantuException {

        OrderInfo orderInfo = null;

        OrgMember orgMember = OrgMemberDAO.INSTANCE.getMemberByUserId(req.orgId, req.userId);

        Order order = OrderDAO.INSTANCE.getOrderById(req.orderId);

        if (orgMember == null && !req.userId.equals(order.userId)) {
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_MEMBER_NOT_FOUND,
                    "not a valid org member");
        }

        if (orgMember != null && orgMember.profile == OrgMemberProfile.STUDENT
                && !req.userId.equals(order.customer.id)) {
            throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED,
                    "action not allowed for orderId: " + req.orderId + " and userId: "
                            + order.customer.id);
        }

        boolean isOrgSeller = false;

        SrcEntity seller = new SrcEntity(EntityType.ORGANIZATION, req.orgId);

        for (OrderedItem itemItem : order.items) {
            if ((isOrgSeller = seller.equals(itemItem.seller))) {
                break;
            }
        }

        if (!req.userId.equals(order.userId) && !isOrgSeller) {

            if (!order.customer.equals(seller) || orgMember.profile != OrgMemberProfile.MANAGER) {
                throw new VedantuException(VedantuErrorCode.ACTION_NOT_ALLOWED,
                        "action not allowed for orderId: " + req.orderId + " and userId: "
                                + order.customer.id);
            }
        }

        orderInfo = order.toOrderInfo();

        return orderInfo;
    }

    /**
     * NOTE: this method will only be called for GoogleWallet Server from Android APP, in rest of
     * the flow onPaymentReceive method will be called
     *
     * @param req
     * @return
     * @throws VedantuException
     */
    public static UpdateTransactionRes updateTransaction(UpdateTransactionReq req)
            throws VedantuException {

        Order order = OrderDAO.INSTANCE.getOrderById(Long.parseLong(req.orderId.trim()));
        if (!order.customer.equals(req.customer)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ORDER, "provided orderId: "
                    + req.orderId + " is not valid for customer: " + req.customer);
        }

        order.orderState = req.orderState;

        VedantuTransactionManager transactionManager = VedantuTransactionManager.getInstance();

        if (order.orderState == OrderState.CONFIRMED
                && req.transactionStatus == TransactionStatus.SUCCESS) {

            transactionManager.generateInvoice(order);
            // TODO: for now we are assuming that user will pay amount inclusing of taxes
            req.amountPaid = order.invoiceInfo.total - order.invoiceInfo.discount;
        }
        OrderDAO.INSTANCE.save(order);

        JSONObject transactionInfo = null;
        try {
            transactionInfo = new JSONObject(req.transactionInfo);
        } catch (Throwable e) {
            transactionInfo = new JSONObject();
            try {
                transactionInfo.put("NO_JSON", req.transactionInfo);
            } catch (Throwable e1) {}
        }
        Transaction transaction = transactionManager.updateTransactionStatus(order.orderId, req.transactionId,
                req.paymentChannelTransactionId, req.paymentInstrument, req.paymentMethod, req.transactionStatus,
                String.valueOf(req.transactionTime), JSONUtils.toMap(transactionInfo),
                req.amountPaid);
        transaction.paymentChannel = req.getPaymentChannel();
        TransactionDAO.INSTANCE.save(transaction);
        if (req.transactionStatus == TransactionStatus.SUCCESS
                && order.updatePaymentStatus(req.paymentMethod, req.transactionId, req.amountPaid)) {
            OrderDAO.INSTANCE.save(order);
        }
        UpdateTransactionRes res = new UpdateTransactionRes();
        res.transactionId = req.transactionId;
        res.orderId = order.toStringOrderId();
        return res;
    }

    public static void updateOrderOnUserVerification(long orderId, String userId)
            throws VedantuException {

        Order order = OrderDAO.INSTANCE.getOrderById(orderId);
        if (order.customer != null) {
            order.customer.id = userId;
        }
        order.tempUser = false;
        if (order.invoiceInfo != null && order.invoiceInfo.customer != null) {
            order.invoiceInfo.customer.id = userId;
        }
        OrderDAO.INSTANCE.save(order);
    }

    public static void addItemToOrder(Order order, SellableItemDetails sellableItemDetails,
            ItemCategory category, int count, String desc, OrderItemDetails details,
            Location shippedToLocation) throws VedantuException {

        addItemToOrder(order, sellableItemDetails, category, count, desc, details,
                shippedToLocation, false);
    }

    public static void addItemToOrder(Order order, SellableItemDetails sellableItemDetails,
            ItemCategory category, int count, String desc, OrderItemDetails details,
            Location shippedToLocation, boolean dontSave) throws VedantuException {

        if (sellableItemDetails.seller != null
                && EntityType.ORGANIZATION.equals(sellableItemDetails.seller.type)
                && StringUtils.isEmpty(desc)) {
            Organization org = OrganizationDAO.INSTANCE.getById(sellableItemDetails.seller.id);
            if (org == null) {
                throw new ExportException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
            }
            desc = org.fullName;
            if (StringUtils.isEmpty(desc)) {
                desc = org.name;
            }
        }
        OrderedItem orderedItem = new OrderedItem(sellableItemDetails.itemName,
                sellableItemDetails.item, sellableItemDetails.seller, category,
                sellableItemDetails.costRate, desc, count, details);
        List<Tax> taxes = TaxManager.getTaxes(category, shippedToLocation);
        orderedItem.calculateCost(taxes);
        if (order.items == null) {
            order.items = new ArrayList<OrderedItem>();
        }
        order.items.add(orderedItem);
        order.updateOrderTotal();
        if (!dontSave) {
            OrderDAO.INSTANCE.save(order);
        }
    }

    public static void confirmOrder(long orderId) throws VedantuException {

        Order order = OrderDAO.INSTANCE.getOrderById(orderId);

        if (order == null || CollectionUtils.isNotEmpty(order.items)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ORDER);
        }

        if (order.orderState != OrderState.DRAFT) {
            throw new VedantuException(VedantuErrorCode.ORDER_STATE_NOT_ALLOWED);
        }
        OrderDAO.INSTANCE.confirmOrder(order.orderId);
    }

    /**
     *
     * @return will return all the orders for which request.customer is seller
     */
    public static GetOrdersRes getItemOrders(GetSellOrdersReq req) throws VedantuException {

        DBObject query = getOrdersQuery(req);
        query.put("items.seller.id", req.customer.id);
        query.put("items.seller.type", req.customer.type.name());

        if (req.itemCategory != null) {
            query.put("items.category", req.itemCategory.name());
        }

        if (req.item != null) {
            query.put("items.item.id", req.item.id);
            query.put("items.item.type", req.item.type.name());
        }

        return getOrders(req.orgId, query, req.start, req.size);
    }

    /**
     *
     * @param req
     * @return list of orders a user has placed
     * @throws VedantuException
     */
    public static GetOrdersRes getOrders(GetBuyOrdersReq req) throws VedantuException {

        DBObject query = getOrdersQuery(req);
        query.put("customer.id", req.customer.id);
        query.put("customer.type", req.customer.type.name());
        return getOrders(req.orgId, query, req.start, req.size);
    }

    private static DBObject getOrdersQuery(GetBuyOrdersReq req) throws VedantuException {

        DBObject query = new BasicDBObject(Order.ITEMS, new BasicDBObject(MongoManager.NE_QUERY,
                null));
        if (StringUtils.isNotEmpty(req.invoiceNo)) {
            query.put("invoiceInfo.invoiceNo", req.invoiceNo);
        }
        long orderId = 0;
        if (StringUtils.isNotEmpty(req.orderId)) {
            try {
                orderId = Long.parseLong(req.orderId);
            } catch (Throwable e) {
                throw new VedantuException(VedantuErrorCode.INVALID_ORDER_ID, "orderId: "
                        + req.orderId + " is not valid");
            }
            query.put(Order.ORDER_ID, orderId);
        }

        if (req.period != null) {
            BasicDBList filterQuery = new BasicDBList();

            if (req.period.getFrom() > 0) {
                filterQuery.add(new BasicDBObject(Order.ORDER_TIME, new BasicDBObject("$gte",
                        req.period.getFrom())));
            }

            if (req.period.getTill() > 0) {
                filterQuery.add(new BasicDBObject(Order.ORDER_TIME, new BasicDBObject("$lte",
                        req.period.getTill())));
            }
            query.put("$and", filterQuery);
        }
        return query;
    }

    private static GetOrdersRes getOrders(String orgId, DBObject query, int start, int size) {

        GetOrdersRes res = new GetOrdersRes();
        VedantuDBResult<Order> orders = OrderDAO.INSTANCE.getInfos(query, null, start, size,
                MongoManager.getSortQuery(ConstantsGlobal.TIME_CREATED, SortOrder.DESC.name()));
        res.totalHits = orders.totalHits;
        Set<String> userIds = new HashSet<String>();

        for (Order order : orders.results) {
            res.list.add(order.toOrderInfo());
            if (EntityType.USER.equals(order.customer.type)) {
                userIds.add(order.customer.id);
            }
        }
        Map<String, ModelBasicInfo> userInfo = OrgMemberManager
                .getUserInfoMap(orgId, userIds, true);
        for (IListResponseObj listItem : res.list) {
            OrderInfo orderInfo = (OrderInfo) listItem;
            if (EntityType.USER.equals(orderInfo.customer.type)
                    && userIds.contains(orderInfo.customer.id)) {
                orderInfo.customer.info = userInfo.get(orderInfo.customer.id);
            }
        }
        return res;
    }

    @Deprecated
    public static void process(long orderId, OrderState state) throws VedantuException {

        Order order = OrderDAO.INSTANCE.getOrderById(orderId);
        if (order == null || CollectionUtils.isNotEmpty(order.items)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ORDER);
        }

        if (order.orderState != OrderState.CONFIRMED) {
            throw new VedantuException(VedantuErrorCode.ORDER_STATE_NOT_ALLOWED);

        }

        // create expense for ordered Items
        //

    }

    public static void delete(long orderId) throws VedantuException {

        Order order = OrderDAO.INSTANCE.getOrderById(orderId);

        if (order == null || CollectionUtils.isNotEmpty(order.items)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ORDER);
        }
        OrderDAO.INSTANCE.deleteOrder(order.orderId);
    }

    public static String markTransactionConmpleted(String transactionId, SrcEntity item)
            throws VedantuException {

        Transaction transaction = TransactionDAO.INSTANCE.markTransactionConmpleted(item,
                transactionId);
        return String.valueOf(transaction.orderId);
    }

    public static Transaction getTransactionByOrderId(long orderId) throws VedantuException {

        return TransactionDAO.INSTANCE.getTransactionByOrderId(orderId);
    }

    // private static SrcEntity getItemSeller(SrcEntity item) throws VedantuException {

    // SrcEntity seller = getSellableEntity(item)._getSeller();
    // LOGGER.debug("item : " + item + " seller : " + seller);

    //
    // switch (item.type) {
    // case PLAN:
    // seller = new SrcEntity(EntityType.ORGANIZATION, "VEDANTU");
    // break;
    // case SECTION:
    // OrgSection orgSection = OrgSectionDAO.INSTANCE.getById(item.id);
    // if (orgSection == null) {
    // throw new VedantuException(VedantuErrorCode.INVALID_ITEM, "item " + item
    // + " is not valid");
    // }
    // seller = new SrcEntity(EntityType.ORGANIZATION, orgSection.orgId);
    // case SDCARDGROUP:
    // // SDCardGroup sdCardGroup = SDCardGroupDAO.INSTANCE.getById(item.id);
    // // if (orgSection == null) {
    // // throw new VedantuException(VedantuErrorCode.INVALID_ITEM, "item " + item
    // // + " is not valid");
    // // }
    // // seller = new SrcEntity(EntityType.ORGANIZATION, orgSection.orgId);
    // default:
    // break;
    // }
    // return seller;
    // }

    public static SellableItemDetails getSellableItemDetails(SrcEntity item)
            throws VedantuException {

        ISellableEntity sellableItem = getSellableEntity(item);
        SellableItemDetails itemInfo = new SellableItemDetails(sellableItem._getCostRate(),
                    sellableItem._getItemName(), sellableItem._getSeller(), item);
        LOGGER.debug("item: " + item + ", sellableItemInfo: " + itemInfo);

        // switch (item.type) {
        // case PLAN:
        // LicensingPlan plan = LicensingPlanDAO.INSTANCE.getById(item.id);
        // if (plan == null) {
        // throw new VedantuException(VedantuErrorCode.INVALID_ITEM, "item " + item
        // + " is not valid");
        // }
        // costRate = new CostRate((int) (plan.cost * 100), "per user", Currency.getInstance(
        // Locale.US).getCurrencyCode());
        // break;
        // case SECTION:
        // OrgSection orgSection = OrgSectionDAO.INSTANCE.getById(item.id);
        // if (orgSection == null) {
        // throw new VedantuException(VedantuErrorCode.INVALID_ITEM, "item " + item
        // + " is not valid");
        // }
        // costRate = orgSection.costRate;
        // default:
        // break;
        // }
        return itemInfo;
    }

    private static ISellableEntity getSellableEntity(SrcEntity item) throws VedantuException {

        @SuppressWarnings("rawtypes")
        VedantuBasicDAO basicDAO = EntityTypeDAOFactory.INSTANCE.get(item.type);
        if (basicDAO == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_ITEM, "item " + item
                    + " is not enabled as sellable entity");
        }

        VedantuBaseMongoModel model = basicDAO.getById(item.id, VedantuRecordState.ACTIVE);
        if (model == null) {
            throw new VedantuException(VedantuErrorCode.ENTITY_NOT_FOUND, "item " + item
                    + " not found");
        }

        if (!(model instanceof ISellableEntity)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ITEM, "item " + item
                    + " is not a sellable entity");
        }
        return (ISellableEntity) model;

    }

    private static SellableItemDetails getSellableSectionDetails(SrcEntity item, String packageOrgId, int packageDays)
            throws VedantuException {
        if (!item.type.equals(EntityType.SECTION)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ITEM, "item " + item
                    + " is not a section");
        }
        OrgSection section = OrgSectionDAO.INSTANCE.getById(item.id, VedantuRecordState.ACTIVE);
        SellableItemDetails itemInfo = new SellableItemDetails(getSectionPackageCost(section, packageOrgId, packageDays),
                section._getItemName(), new SrcEntity(EntityType.ORGANIZATION, packageOrgId), item);
        return itemInfo;
    }

    private static CostRate getSectionPackageCost(OrgSection section, String packageOrgId,
            int packageDays) throws VedantuException {
        Map<String, List<PackageInfo>> packagesMap = section.packagesMap;
        if (!packagesMap.containsKey(packageOrgId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_ITEM, "section " + section
                    + " is not valid");
        }
        List<PackageInfo> packagesList = packagesMap.get(packageOrgId);
        if (packagesList == null || packagesList.isEmpty()) {
            throw new VedantuException(VedantuErrorCode.INVALID_ITEM, "section " + section
                    + " has no valid packages");
        }
        CostRate costRate = null;
        for (PackageInfo packageInfo : packagesList) {
            if (packageInfo.numDays == packageDays) {
                return packageInfo.costRate;
            }
        }
        return costRate;
    }

    /**
     *
     * @param userId
     * @param order
     * @param deviceType
     * @param costRate
     * @param pointOfSale
     * @param sellerReferenceNo
     * @param customer
     * @param billTo
     * @param shipTo
     * @return transactionId
     * @throws VedantuException
     */
    public static String createThirdPartyTransaction(String userId, Order order,
            DeviceType deviceType, CostRate costRate, String pointOfSale, String sellerReferenceNo,
            SrcEntity customer,/* billTo and shipTo will be null if the order tempUser==true */
            AddressTo billTo, AddressTo shipTo) throws VedantuException {

        VedantuTransactionManager transactionManager = VedantuTransactionManager.getInstance();

        String transactionId = transactionManager.getVedantuTransactionId(userId, order.orderId,
                StringUtils.EMPTY, deviceType, costRate.value, costRate.currencyCode);

        Transaction transaction = TransactionDAO.INSTANCE.getTransaction(transactionId);
        transaction.amountPaid = costRate.value;
        transaction.paymentChannel = Transaction.PAYMENT_CHANNEL_THIRD_PATY;
        transaction.pointOfSale = pointOfSale;
        transaction.sellerReferenceNo = sellerReferenceNo;
        transaction.status = TransactionStatus.SUCCESS;
        transaction.transactionTime = String.valueOf(System.currentTimeMillis());
        transaction.type = TransactionType.THIRD_PARTY_CREDIT;
        transaction.ipAddress = Http.Context.current().request().getHeader("X-Real-IP");
        TransactionDAO.INSTANCE.save(transaction);

        order.ipAddress = transaction.ipAddress;
        order.orderState = OrderState.CONFIRMED;
        order.pointOfSale = transaction.pointOfSale;
        order.sellerReferenceNo = transaction.sellerReferenceNo;
        if (!order.tempUser) {
            User user = UserDAO.INSTANCE.getById(customer.id);
            order.billingEmail = user._getCommunicationEmail();
        } else {
            generateInvoiceForTempUser(order, billTo, shipTo);
        }
        transactionManager.updateOrderAndGetPaymentReceivedRes(order, transaction);
        return transactionId;
    }

    private static void generateInvoiceForTempUser(Order order, AddressTo billTo, AddressTo shipTo) {

        InvoiceInfo invoiceInfo = new InvoiceInfo();
        invoiceInfo.invoiceNo = String.valueOf(CounterDAO.INSTANCE.getNextSequence(
                OrderDAO.INSTANCE.getCollection().getName(), "invoiceNo"));
        invoiceInfo.customer = order.customer;
        invoiceInfo.currencyCode = order.items.get(0).rate.currencyCode;
        invoiceInfo.billTo = billTo;
        invoiceInfo.shipTo = shipTo;
        order.invoiceInfo = invoiceInfo;
        order.calculateFinalBillAmount();

    }

    public static ApplyCouponRes applyCoupon(ApplyCouponReq request) throws VedantuException {
        Order order = OrderDAO.INSTANCE.getOrderById(request.orderId);
        if (order == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_ORDER_ID, "Order is NOT valid");
        }
        CouponCode coupon = CouponCodeDAO.INSTANCE.getByCodeAndOrgId(request.couponCode, request.orgId);
        if (coupon == null) {
            resetDiscount(order);
            throw new VedantuException(VedantuErrorCode.INVALID_COUPON_CODE, "Coupon is NOT valid");
        }
        LOGGER.debug("OrderManager: Applying coupon " + coupon.code + " on order id: " + order.orderId);

        if (coupon.expired) {
            resetDiscount(order);
            throw new VedantuException(VedantuErrorCode.COUPON_CODE_EXPIRED, "Coupon " + coupon.code
                    + " has expired");
        }
        if (coupon.usageCount >= coupon.maxUsageCount) {
            resetDiscount(order);
            throw new VedantuException(VedantuErrorCode.COUPON_CODE_EXPIRED, "Coupon " + coupon.code
                    + " has been used for maximum times");
        }
        if (coupon.minPurchaseValue != 0 && coupon.minPurchaseValue > order.totalAmount) {
            resetDiscount(order);
            throw new VedantuException(VedantuErrorCode.COUPON_NOT_APPLICABLE, "Coupon " + coupon.code
                    + " is applicable only on orders above Rs. " + coupon.minPurchaseValue / 100);
        }
        if (coupon.expiryTime > 0 && coupon.expiryTime < System.currentTimeMillis()) {
            resetDiscount(order);
            throw new VedantuException(VedantuErrorCode.COUPON_CODE_EXPIRED, "Coupon " + coupon.code
                    + " has expired");
        }
        int discount = calculateDiscount(order.totalAmount, coupon);
        if (discount < 0) {
            resetDiscount(order);
            throw new VedantuException(VedantuErrorCode.ERROR_APPLY_COUPON,
                    "Error occured while applying coupon");
        }
        order.discount = discount;
        order.couponCode = coupon.code;
        OrderDAO.INSTANCE.save(order);
        updateTransactionAmount(order);
        ApplyCouponRes res = new ApplyCouponRes();
        res.discount = discount;
        res.discountedAmount = order.toStringDiscountedAmount();
        res.orderId = order.orderId;
        return res;
    }

    public static ApplyLPCreditsRes applyLPCredits(ApplyLPCreditsReq request)
            throws VedantuException {
        Order order = OrderDAO.INSTANCE.getOrderById(request.orderId);
        if (order == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_ORDER_ID, "Order is NOT valid");
        }
        OrgMember member = OrgMemberDAO.INSTANCE.getByUserId(request.userId);
        if(request.lpCredits == 0 && !OrgMemberManager.isRewardsFreezed(member)){
            GetWalletBalanceReq bReq = new GetWalletBalanceReq();
            bReq.userId = request.userId;
            bReq.lpCredits = request.lpCredits;
            bReq.orderId = request.orderId;
            bReq.orgId = request.orgId;
            request.lpCredits = OrgMemberManager.getWalletBalance(bReq).maxRewardPointsToRedeem;
        }
        if (request.lpCredits > 0 && !OrgMemberManager.isRewardsFreezed(member)) {
            order.lpCreditsRedeemed = request.lpCredits * 100;
            OrderDAO.INSTANCE.save(order);
            updateTransactionAmount(order);
            OrgMemberDAO.INSTANCE.freezeRewards(request.userId, request.orderId, request.lpCredits);
        }
        ApplyLPCreditsRes res = new ApplyLPCreditsRes();
        res.discountedAmount = order.toStringDiscountedAmount();
        if (OrgMemberManager.isRewardsFreezed(member)) {
            res.lpCredits = 0;
        } else {
            res.lpCredits = request.lpCredits;
        }
        res.orderId = order.orderId;
        return res;
    }

    public static ApplyLPCreditsRes removeLPCredits(ApplyLPCreditsReq request)
            throws VedantuException {
        Order order = OrderDAO.INSTANCE.getOrderById(request.orderId);
        if (order == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_ORDER_ID, "Order is NOT valid");
        }
        ApplyLPCreditsRes res = new ApplyLPCreditsRes();
        OrgMember member = OrgMemberDAO.INSTANCE.getByUserId(request.userId);
        if (OrgMemberManager.isRewardsFreezed(member)
                && member.freezedRewardsOrderId != request.orderId) {
            res.lpCredits = 0;
            request.lpCredits = 0;
        } else {
            res.lpCredits = request.lpCredits;
        }
        if (request.lpCredits > 0) {
            int lpCreditsRedeemed = order.lpCreditsRedeemed;
            order.lpCreditsRedeemed = 0;
            OrderDAO.INSTANCE.save(order);
            updateTransactionAmount(order);
            OrgMemberDAO.INSTANCE.addBackRewards(request.userId, lpCreditsRedeemed / 100);
        }
        res.discountedAmount = order.toStringDiscountedAmount();
        res.orderId = order.orderId;
        return res;
    }

    private static void resetDiscount(Order order) {
        order.discount = 0;
        OrderDAO.INSTANCE.save(order);
        updateTransactionAmount(order);
    }

    private static int calculateDiscount(int orderAmount, CouponCode coupon) {
        int discount = 0;
        if (coupon.couponType.equals(CouponType.FLAT)) {
            discount = coupon.discountValue;
        } else if (coupon.couponType.equals(CouponType.PERCENTAGE)) {
            discount = orderAmount * coupon.discountPercentage / 100;
        }

        if (coupon.maxDiscount > 0 && discount > coupon.maxDiscount) {
            discount = coupon.maxDiscount;
        }
        if (discount > orderAmount) {
            discount = orderAmount;
        }
        return discount;
    }

    private static void updateTransactionAmount(Order order) {
        try {
            Transaction transaction = getTransactionByOrderId(order.orderId);
            if (transaction != null) {
                transaction.amount = order.totalAmount - order.discount - order.lpCreditsRedeemed;
                TransactionDAO.INSTANCE.save(transaction);
            }
        } catch (VedantuException e) {
            LOGGER.error("Error updating transaction amount", e);
        }
    }

    private static String getPaymentChannel(String orgId) {
        LOGGER.debug("GetPaymentChannel function");
        String paymentChannel = Play.application().configuration().getString("payment.defaultChannel");
//        if (orgId != null && !orgId.isEmpty()) {
//            if (Play.application().configuration().getString("targetPMT.orgId").equals(orgId)) {
//                LOGGER.info("Changing payment channel for this org with orgId:" + orgId);
//                paymentChannel = Play.application().configuration().getString("targetPMT.paymentChannel");
//            }
//        }
        return paymentChannel;
    }

    public static ConfirmPaymentRes confirmPayment(ConfirmPaymentReq request)
            throws VedantuException, JSONException, IOException {
        LOGGER.debug("Divesh Entered confirm payment in order manager     "
                + request.payment_request_id);
        Order order = OrderDAO.INSTANCE.getOrderByPaymentChannelTransactionId(request.payment_request_id);
        ConfirmPaymentRes response = new ConfirmPaymentRes();
        response.item_sku = URLEncoder.encode(order.item_sku, "UTF-8");
        String payload = getInstamojoPaymentDetailsFromPHP(request.payment_request_id, request.orgId);
        JSONObject payloadResp = new JSONObject(payload);
        Map<String, Object> httpResParams = buildParams(payloadResp);
        LOGGER.debug("Divesh httpResParams are : "+httpResParams);
        if (payloadResp.getBoolean("success")) {
            response.status = payloadResp.getJSONObject("payment_request").getJSONArray("payments").getJSONObject(0).getString("status");
        } else {
            response.status = "Failed";
        }

        TransactionStatus transactionStatus = null;
        OrderState orderState = null;

        if (response.status.equalsIgnoreCase("Credit")) {
            orderState = OrderState.CONFIRMED;
            transactionStatus = TransactionStatus.SUCCESS;
        } else if (response.status.equalsIgnoreCase("Failed")) {
            orderState = OrderState.CANCELLED;
            transactionStatus = TransactionStatus.FAILED;
        } else {
            orderState = OrderState.CANCELLED;
            transactionStatus = TransactionStatus.CANCELLED;
        }

        String paymentMethod = payloadResp.getJSONObject("payment_request").getJSONArray("payments").getJSONObject(0).getString("instrument_type");

        String paymentInstrument = payloadResp.getJSONObject("payment_request").getJSONArray("payments").getJSONObject(0).getString("billing_instrument");

        String amount = payloadResp.getJSONObject("payment_request").getString("amount");

        int amountPaid = (int) (Float.parseFloat(amount) * 100);

        if (transactionStatus != TransactionStatus.SUCCESS) {
            amountPaid = 0;
        }
        order.orderState = orderState;
        String transactionId = null;

        Transaction transactionByOrder = TransactionDAO.INSTANCE
                .getTransactionByOrderId(order.orderId);
        transactionId = transactionByOrder.id.toString();
        response.transactionId = transactionId;
        /*
         * Check if there is anyway we pass txn id and they return on response.
         * However the method updateTransaction handling the null by getting
         * transaction by orderid.
         */

        Transaction transaction = updateTransactionStatus(order.orderId, transactionId,
                order.paymentChannelTransactionId, paymentInstrument, paymentMethod, transactionStatus,
                String.valueOf(System.currentTimeMillis()), httpResParams, amountPaid);

        updateOrderAndGetPaymentReceivedRes(order, transaction);
        return response;
    }

    private static Map<String, Object> buildParams(JSONObject payloadResp) throws JSONException {
        // TODO Auto-generated method stub
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("payment_id", payloadResp.getJSONObject("payment_request").getJSONArray("payments").getJSONObject(0).getString("payment_id"));
        params.put("status", payloadResp.getJSONObject("payment_request").getJSONArray("payments").getJSONObject(0).getString("status"));
        params.put("buyer_name", payloadResp.getJSONObject("payment_request").getJSONArray("payments").getJSONObject(0).getString("buyer_name"));
        params.put("buyer_phone", payloadResp.getJSONObject("payment_request").getJSONArray("payments").getJSONObject(0).getString("buyer_phone"));
        params.put("buyer_email", payloadResp.getJSONObject("payment_request").getJSONArray("payments").getJSONObject(0).getString("buyer_email"));
        params.put("currency", payloadResp.getJSONObject("payment_request").getJSONArray("payments").getJSONObject(0).getString("currency"));
        params.put("amount", payloadResp.getJSONObject("payment_request").getString("amount"));
        params.put("instrument_type", payloadResp.getJSONObject("payment_request").getJSONArray("payments").getJSONObject(0).getString("instrument_type"));
        params.put("billing_instrument", payloadResp.getJSONObject("payment_request").getJSONArray("payments").getJSONObject(0).getString("billing_instrument"));
        params.put("Description", payloadResp.getJSONObject("payment_request").getString("purpose"));
        params.put("created_at", payloadResp.getJSONObject("payment_request").getJSONArray("payments").getJSONObject(0).getString("created_at"));
        params.put("payment_request", payloadResp.getJSONObject("payment_request").getJSONArray("payments").getJSONObject(0).getString("payment_request"));
        params.put("allow_repeated_payments", payloadResp.getJSONObject("payment_request").getBoolean("allow_repeated_payments"));
        params.put("PaymentMethod", "Instamojo");
        return params;
    }

    public static OnPaymentReceivedRes updateOrderAndGetPaymentReceivedRes(Order order,
            Transaction transaction) throws VedantuException {

        if (transaction.status == TransactionStatus.SUCCESS) {
            if (order.orderState == OrderState.CONFIRMED && !order.tempUser) {
                generateInvoice(order);
            }
            order.updatePaymentStatus(transaction.paymentMethod, transaction._getStringId(),
                    transaction.amountPaid);
        }

        OrderDAO.INSTANCE.save(order);
        OnPaymentReceivedRes res = new OnPaymentReceivedRes(transaction.orderId,
                transaction._getStringId(), transaction.status, transaction.item_sku,
                transaction.callbackUrl);
        LOGGER.debug("sending onPayment received res : " + res);
        return res;
    }

    public static void generateInvoice(Order order) throws VedantuException {

        generateInvoice(order, UserDAO.INSTANCE.getById(order.customer.id), null, null);
    }

    public static void generateInvoice(Order order, User user, String contactNo, String address)
            throws VedantuException {

        if (CollectionUtils.isEmpty(order.items) || order.orderState != OrderState.CONFIRMED) {
            throw new VedantuException(VedantuErrorCode.INVALID_ORDER,
                    "no items or invalid orderState in order[orderId: " + +order.orderId
                            + ", orderState:" + order.orderState + "]");
        }

        InvoiceInfo invoiceInfo = new InvoiceInfo();
        invoiceInfo.invoiceNo = String.valueOf(CounterDAO.INSTANCE.getNextSequence(
                OrderDAO.INSTANCE.getCollection().getName(), "invoiceNo"));
        invoiceInfo.customer = order.customer;
        invoiceInfo.currencyCode = order.items.get(0).rate.currencyCode;
        if (!order.tempUser) {
            populateBillAndShipAddress(order.billingEmail, invoiceInfo, user, contactNo, address);
        }
        order.invoiceInfo = invoiceInfo;
        order.calculateFinalBillAmount();
        OrderDAO.INSTANCE.save(order);
    }

    private static void populateBillAndShipAddress(String billingEmail, InvoiceInfo invoiceInfo,
            User user, String contactNo, String address) throws VedantuException {

        // for now process it only for org and user
        if (invoiceInfo.customer.type != EntityType.ORGANIZATION
                && invoiceInfo.customer.type != EntityType.USER) {
            return;
        }

        if (StringUtils.isEmpty(billingEmail)) {
            billingEmail = user.email;
        }
        AddressTo billAddress = new AddressTo(user._getFullName(), contactNo, billingEmail, address);

        AddressTo shipAddress = new AddressTo(user._getFullName(), contactNo, billingEmail, address);
        invoiceInfo.billTo = billAddress;
        invoiceInfo.shipTo = shipAddress;
    }

    public static Transaction updateTransactionStatus(long orderId, String transactionId,
            String paymentChannelTransactionId, String paymentInstrument, String paymentMethod,
            TransactionStatus transactionStatus, String transactionTime,
            Map<String, Object> transactionInfo, int amountPaid) throws VedantuException {

        Transaction transaction = StringUtils.isEmpty(transactionId) ? null
                : TransactionDAO.INSTANCE.getById(transactionId);
        if (transaction == null || transaction.orderId != orderId) {
            throw new VedantuException(VedantuErrorCode.INVALID_TRANSACTION, "transactionId["
                    + transactionId + "] on callback url is not valid");
        }

        if (transaction.status == TransactionStatus.SUCCESS) {
            throw new VedantuException(VedantuErrorCode.TRANSACTION_ALREADY_COMPLETED,
                    "transaction already completed");
        }
        transaction.paymentChannel = "Instamojo";
        transaction.paymentChannelTransactionId = paymentChannelTransactionId;
        transaction.paymentInstrument = paymentInstrument;
        transaction.paymentMethod = paymentMethod;
        transaction.status = transactionStatus;
        transaction.transactionTime = transactionTime;
        transaction.transactionInfo = transactionInfo;
        transaction.amountPaid = amountPaid;
        TransactionDAO.INSTANCE.save(transaction);
        return transaction;
    }

    private static String getInstamojoPaymentDetails(String payment_request_id, String orgId) throws JSONException, IOException, VedantuException {
        LOGGER.debug("Entered getInstamojoPaymentDetails ");
        String url = Play.application().configuration().getString("instamojo.charging.url")+payment_request_id+"/";
        if(StringUtils.isEmpty(orgId)){
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }
        Organization org = OrganizationDAO.INSTANCE.getById(orgId);
        String apiKey = org.instaMojoApiKey;
        String authToken = org.instaMojoAuthToken;
        if(StringUtils.isEmpty(apiKey) || StringUtils.isEmpty(authToken)){
            apiKey = Play.application().configuration().getString("instamojo.apikey");
            authToken = Play.application().configuration().getString("instamojo.authtoken");
        }
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Setting basic get request
        con.setRequestMethod("GET");
        con.setRequestProperty("Content-Type","application/json");
        con.setRequestProperty("X-Api-Key", apiKey);
        con.setRequestProperty("X-Auth-Token", authToken);

        int responseCode = con.getResponseCode();
        LOGGER.debug("Divesh nSending 'GET' request to URL : " + url);
        LOGGER.debug("Divesh Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String output;
        StringBuffer response = new StringBuffer();

        while ((output = in.readLine()) != null) {
         response.append(output);
        }
        in.close();
        LOGGER.debug("Divesh response : "+response.toString());
        return response.toString();
    }

    private static String getInstamojoPaymentDetailsFromPHP(String payment_request_id, String orgId) throws JSONException, IOException, VedantuException {
        LOGGER.debug("Entered getInstamojoPaymentDetails ");
        String instamojo_url = Play.application().configuration().getString("instamojo.charging.url")+payment_request_id+"/";
        if(StringUtils.isEmpty(orgId)){
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }
        Organization org = OrganizationDAO.INSTANCE.getById(orgId);
        String apiKey = org.instaMojoApiKey;
        String authToken = org.instaMojoAuthToken;
        if(StringUtils.isEmpty(apiKey) || StringUtils.isEmpty(authToken)){
            apiKey = Play.application().configuration().getString("instamojo.apikey");
            authToken = Play.application().configuration().getString("instamojo.authtoken");
        }
        String url = "http://apigateway.learnpedia.in/secure-api/index.php/get-payment-status";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Setting basic get request
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type","application/json");
        con.setRequestProperty("X-Api-Key", apiKey);
        con.setRequestProperty("X-Auth-Token", authToken);

        JSONObject payload = new JSONObject();
        payload.put("instamojo_url", instamojo_url);

        String postJsonData = payload.toString();

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(postJsonData);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        LOGGER.debug("Divesh nSending 'POST' request to URL : " + url);
        LOGGER.debug("Divesh Post Data : " + postJsonData);
        LOGGER.debug("Divesh Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String output;
        StringBuffer response = new StringBuffer();

        while ((output = in.readLine()) != null) {
         response.append(output);
        }
        in.close();
        LOGGER.debug("Divesh response : "+response.toString());
        return response.toString();
    }
}
