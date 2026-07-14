package com.vedantu.billing.dao;

import org.bson.types.ObjectId;

import play.Logger;
import play.Logger.ALogger;

import com.google.code.morphia.query.Query;
import com.google.code.morphia.query.UpdateOperations;
import com.vedantu.billing.enums.OrderState;
import com.vedantu.billing.models.Order;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.constants.ConstantsGlobal;
import com.vedantu.mongo.VedantuBasicDAO;
import com.vedantu.mongo.VedantuRecordState;

public class OrderDAO extends VedantuBasicDAO<Order, ObjectId> {

    private static final ALogger LOGGER   = Logger.of(OrderDAO.class);

    public static final OrderDAO INSTANCE = new OrderDAO();

    private OrderDAO() {

        super(Order.class);
    }

    public Order confirmOrder(long orderId) {

        LOGGER.debug("confurming order[" + orderId + "]");
        UpdateOperations<Order> updateSequence = getDS().createUpdateOperations(Order.class);
        updateSequence.set(Order.ORDER_STATE, OrderState.CONFIRMED);
        updateSequence.set(Order.ORDER_TIME, System.currentTimeMillis());
        Query<Order> findQuery = getDS().createQuery(Order.class);
        findQuery.field(Order.ORDER_ID).equal(orderId);

        Order order = getDS().findAndModify(findQuery, updateSequence, false, true);
        return order;
    }

    public Order deleteOrder(long orderId) {

        LOGGER.debug("deleting order[" + orderId + "]");
        UpdateOperations<Order> updateSequence = getDS().createUpdateOperations(Order.class);
        updateSequence.set(Order.ORDER_STATE, OrderState.CANCELLED);

        updateSequence.set(Order.ORDER_TIME, System.currentTimeMillis());
        updateSequence.set(ConstantsGlobal.RECORD_STATE, VedantuRecordState.DELETED);

        Query<Order> findQuery = getDS().createQuery(Order.class);
        findQuery.field(Order.ORDER_ID).equal(orderId);

        findQuery.field(ConstantsGlobal.RECORD_STATE).equal(VedantuRecordState.ACTIVE);
        Order order = getDS().findAndModify(findQuery, updateSequence, false, true);
        return order;
    }

    public Order getOrderById(long orderId) throws VedantuException {

        Order order = getQuery().filter(Order.ORDER_ID, orderId).get();

        if (order == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_ORDER,
                    "no order found with orderId: " + orderId);
        }
        return order;
    }

    public Order getOrderByPaymentChannelTransactionId(String paymentChannelTransactionId) throws VedantuException{
        Order order = getQuery().filter("paymentChannelTransactionId",paymentChannelTransactionId).get();

        if (order == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_ORDER,
                    "no order found with paymentChannelTransactionId : " + paymentChannelTransactionId);
        }
        return order;
    }

}