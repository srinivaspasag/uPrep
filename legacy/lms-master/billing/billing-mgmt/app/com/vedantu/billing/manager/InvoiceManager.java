package com.vedantu.billing.manager;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;

import play.Logger;
import play.Logger.ALogger;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import com.mongodb.QueryBuilder;
import com.vedantu.billing.dao.OrderDAO;
import com.vedantu.billing.enums.ItemCategory;
import com.vedantu.billing.enums.OrderState;
import com.vedantu.billing.managers.OrderManager;
import com.vedantu.billing.models.Order;
import com.vedantu.billing.payment.managers.VedantuTransactionManager;
import com.vedantu.billing.pojos.OrderItemDetails;
import com.vedantu.billing.pojos.requests.GenerateInvoiceReq;
import com.vedantu.billing.pojos.responses.GenerateInvoiceRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.commons.enums.DeviceType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.Interval;
import com.vedantu.commons.pojos.Location;
import com.vedantu.mongo.MongoManager;
import com.vedantu.mongo.MongoManager.SortOrder;
import com.vedantu.organization.daos.LicensingPlanDAO;
import com.vedantu.organization.daos.OrgMemberDAO;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.daos.UserStateLogDAO;
import com.vedantu.organization.models.Organization;
import com.vedantu.organization.models.licensing.LicensingPlan;
import com.vedantu.user.daos.UserDAO;
import com.vedantu.user.models.User;

public class InvoiceManager {

    private static final ALogger LOGGER = Logger.of(InvoiceManager.class);

    /**
     * for now invoices are only generated for org
     * 
     * @param customer
     * @param till
     * @throws VedantuException
     */
    public static GenerateInvoiceRes generateInvoices(GenerateInvoiceReq req)
            throws VedantuException {

        GenerateInvoiceRes res = new GenerateInvoiceRes();
        // get recently generated order
        DBObject query = new BasicDBObject("customer.id", req.customer.id);
        query.put("customer.type", req.customer.type.name());
        query.put("orderState",
                new BasicDBObject(MongoManager.NE_QUERY, OrderState.CANCELLED.name()));
        Order previousOrder = OrderDAO.INSTANCE.findOne(query, null,
                MongoManager.getSortQuery(ConstantsGlobal.TIME_CREATED, SortOrder.DESC.name()));

        Organization org = OrganizationDAO.INSTANCE.getById(req.customer.id);
        if (org.subscription == null || StringUtils.isEmpty(org.subscription.planId)
                || org.subscription.validity == null) {
            LOGGER.error("organization is not subscribed to any active plan");
            throw new VedantuException(VedantuErrorCode.INVALID_LICENSING_PLAN,
                    "organization is not subscribed to any active plan");
        }

        long billStartTime = 0;

        if (previousOrder == null || previousOrder.orderState == OrderState.DRAFT) {
            // no order was generated previously
            billStartTime = org.timeCreated;
        } else {
            billStartTime = previousOrder.period.getTill() + DateUtils.MILLIS_PER_SECOND;
        }

        billStartTime = Math.max(org.subscription.validity.getFrom(), billStartTime);

        long till = req.till;

        if (billStartTime > till) {
            throw new VedantuException(VedantuErrorCode.INVALID_DATE_TIME, "Start date "
                    + new Date(billStartTime)
                    + " can not be greater than End date "
                    + new Date(till)
                    + (previousOrder == null || previousOrder.period == null ? ""
                            : ", latest generated invoice from "
                                    + new Date(previousOrder.period.getFrom())) + " to "
                    + new Date(previousOrder.period.getTill()));
        }

        List<Interval> intervals = getMonthlyInterval(billStartTime, till);

        Location shitToLocation = org.locations.get(0);

        for (Interval interval : intervals) {

            if (previousOrder != null && previousOrder.orderState != OrderState.DRAFT
                    && previousOrder.period.equals(interval)) {
                LOGGER.warn("invoice is already generated for interval: " + interval);
                continue;
            }

            if (org.subscription.validity.getTill() > 0
                    && interval.getFrom() > org.subscription.validity.getTill()) {
                LOGGER.error("licensing plan is not active for : " + interval);
                break;
            }

            Order order = OrderManager.createOrder(req.userId, DeviceType.WEB, req.customer);
            addPlanToOrder(order, org.subscription.planId, interval, shitToLocation);
            order.orderState = OrderState.CONFIRMED;
            order.orderTime = System.currentTimeMillis();
            generateInvoiceForOrganization(order);
            res.orderInfos.add(order.toOrderInfo());
        }
        return res;
    }

    public static void
            updatePayment(long orderId, String paymentMode, String refNo, int amountPaid)
                    throws VedantuException {

        Order order = OrderDAO.INSTANCE.getOrderById(orderId);
        order.updatePaymentStatus(paymentMode, refNo, amountPaid);
        OrderDAO.INSTANCE.save(order);

    }

    private static List<Interval> getMonthlyInterval(long from, long till) {

        List<Interval> intervals = new ArrayList<Interval>();
        Calendar fromCal = Calendar.getInstance();
        fromCal.setTimeInMillis(from);

        Calendar tillCal = Calendar.getInstance();
        tillCal.setTimeInMillis(till);

        if (fromCal.get(Calendar.MONTH) == tillCal.get(Calendar.MONTH)
                && fromCal.get(Calendar.YEAR) == tillCal.get(Calendar.YEAR)) {
            intervals.add(getMonthInterval(fromCal));
        } else {

            Calendar curr = Calendar.getInstance();

            while (true) {

                if (fromCal.get(Calendar.YEAR) == curr.get(Calendar.YEAR)
                        && fromCal.get(Calendar.MONTH) > curr.get(Calendar.MONTH)) {
                    break;
                }

                if (fromCal.get(Calendar.YEAR) <= tillCal.get(Calendar.YEAR)
                        && fromCal.get(Calendar.YEAR) <= curr.get(Calendar.YEAR)) {

                    if (fromCal.get(Calendar.YEAR) < tillCal.get(Calendar.YEAR)) {
                        intervals.add(getMonthInterval(fromCal));
                        fromCal.add(Calendar.MONTH, 1);
                    } else if (fromCal.get(Calendar.YEAR) == tillCal.get(Calendar.YEAR)) {

                        if (fromCal.get(Calendar.MONTH) <= tillCal.get(Calendar.MONTH)) {
                            intervals.add(getMonthInterval(fromCal));
                            fromCal.add(Calendar.MONTH, 1);
                        } else {
                            break;
                        }
                    }
                } else {
                    break;
                }
            }

        }

        return intervals;

    }

    private static Interval getMonthInterval(Calendar dateCal) {

        System.out.println("geting monthly interval");

        Calendar fDate = Calendar.getInstance();
        fDate.set(dateCal.get(Calendar.YEAR), dateCal.get(Calendar.MONTH), 1, 0, 0, 0);
        Calendar tDate = Calendar.getInstance();
        tDate.set(dateCal.get(Calendar.YEAR), dateCal.get(Calendar.MONTH),
                dateCal.getActualMaximum(Calendar.DAY_OF_MONTH) + 1, 0, 0, 0);
        return new Interval(fDate.getTimeInMillis(), tDate.getTimeInMillis()
                - DateUtils.MILLIS_PER_SECOND);
    }

    public static void generateInvoiceForOrganization(Order order) throws VedantuException {

        if (CollectionUtils.isEmpty(order.items) || order.orderState != OrderState.CONFIRMED) {
            throw new VedantuException(VedantuErrorCode.INVALID_ORDER,
                    "no items or invalid orderState in order[orderId: " + +order.orderId
                            + ", orderState:" + order.orderState + "]");
        }

        Organization org = order.customer.type != EntityType.ORGANIZATION ? null
                : OrganizationDAO.INSTANCE.getOrganizationById(order.customer.id);

        // bill and ship to org admin
        User user = UserDAO.INSTANCE.getById(org.adminUserId);
        VedantuTransactionManager.getInstance().generateInvoice(order, user, org.contactNumber,
                org.address);
    }

    public static void addPlanToOrder(long orderId, String planId, Interval period,
            Location shippedToLocation) throws VedantuException {

        Order order = OrderDAO.INSTANCE.getOrderById(orderId);

        addPlanToOrder(order, planId, period, shippedToLocation);
    }

    public static void addPlanToOrder(Order order, String planId, Interval period,
            Location shippedToLocation) throws VedantuException {

        LicensingPlan plan = LicensingPlanDAO.INSTANCE.getById(planId);
        if (plan == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_LICENSING_PLAN,
                    "no plan found for planId: " + planId);
        }

        // calculate active number of user for the provided interval
        DBObject userFilterQuery = QueryBuilder
                .start()
                .put(ConstantsGlobal.ORG_ID)
                .is(order.customer.id)
                .or(QueryBuilder.start().put("interval.from").greaterThanEquals(period.getFrom())
                        .lessThan(period.getTill()).get(),

                        QueryBuilder.start().put("interval.from").lessThan(period.getFrom())
                                .put("interval.till").greaterThanEquals(period.getFrom())
                                .lessThan(period.getTill()).get(),

                        QueryBuilder.start().put("interval.from").lessThan(period.getTill())
                                .put("interval.till").is(-1).get()).get();

        LOGGER.debug("query orgMember collection : " + userFilterQuery);

        List<Object> userIds = OrgMemberDAO.INSTANCE.getDistinct(ConstantsGlobal.USER_ID,
                userFilterQuery);

        // fetch deactivated user from history table
        userFilterQuery.putAll(QueryBuilder.start().put(ConstantsGlobal.USER_ID).notIn(userIds)
                .get());

        LOGGER.debug("query UserStateLog collection : " + userFilterQuery);

        long previouslyActiveUserCount = UserStateLogDAO.INSTANCE.count(userFilterQuery);

        int count = userIds.size() + (int) previouslyActiveUserCount;

        OrderItemDetails orderItemDetails = new OrderItemDetails();
        orderItemDetails.period = period;
        if (order.period == null) {
            order.period = period;
        } else {
            if (period.getFrom() < order.period.getFrom()) {
                order.period.setFrom(period.getFrom());
            }

            if (period.getTill() > order.period.getTill()) {
                order.period.setTill(period.getTill());
            }
        }

        OrderManager.addItemToOrder(order, plan._getSellableItemDetails(), ItemCategory.PLAN,
                count, plan.desc, orderItemDetails, shippedToLocation);

    }
}
