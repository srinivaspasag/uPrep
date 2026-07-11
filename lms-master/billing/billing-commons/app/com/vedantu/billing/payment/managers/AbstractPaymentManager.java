package com.vedantu.billing.payment.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.mvc.Http;

import com.vedantu.billing.dao.OrderDAO;
import com.vedantu.billing.dao.TransactionDAO;
import com.vedantu.billing.enums.OrderState;
import com.vedantu.billing.enums.TransactionStatus;
import com.vedantu.billing.enums.TransactionType;
import com.vedantu.billing.models.Order;
import com.vedantu.billing.models.Transaction;
import com.vedantu.billing.pojos.AddressTo;
import com.vedantu.billing.pojos.InvoiceInfo;
import com.vedantu.billing.pojos.responses.OnPaymentReceivedRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.daos.CounterDAO;
import com.vedantu.commons.enums.DeviceType;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.user.daos.UserDAO;
import com.vedantu.user.models.User;

public abstract class AbstractPaymentManager implements IPaymentManager {

    private static final ALogger LOGGER = Logger.of(AbstractPaymentManager.class);

    private String updateIfNotEmpty(String field, String existingValue, String newValue,
            List<String> updateFields) {

        if (StringUtils.isNotEmpty(newValue)) {
            updateFields.add(field);
            return newValue;
        }
        return existingValue;
    }

    @Override
    public String getChargingRequestUrl(String transactionId, String userId, String item_sku,
            String callbackUrl, String billingEmail, String billingPhone) throws VedantuException {

        User user = UserDAO.INSTANCE.getById(userId);
        Transaction transaction = TransactionDAO.INSTANCE.getTransaction(transactionId);
        if (billingEmail.contains("+")) {
            billingEmail = StringUtils.substringBefore(billingEmail, "+") + "@"
                    + StringUtils.substringAfter(billingEmail, "@");
        }
        Order order = OrderDAO.INSTANCE.getOrderById(transaction.orderId);

        List<String> updateFields = new ArrayList<String>();
        String ipAddress = Http.Context.current().request().getHeader("X-Real-IP");

        transaction.ipAddress = updateIfNotEmpty("ipAddress", transaction.ipAddress, ipAddress,
                updateFields);
        transaction.item_sku = updateIfNotEmpty("item_sku", transaction.item_sku, item_sku,
                updateFields);
        transaction.callbackUrl = updateIfNotEmpty("callbackUrl", transaction.callbackUrl,
                callbackUrl, updateFields);

        if (!updateFields.isEmpty()) {
            TransactionDAO.INSTANCE.updateModel(transaction, updateFields);
        }

        String chargingUrl = getChargingRequestUrl(transactionId, transaction.orderId, user,
                transaction.amount, transaction.currencyCode, transaction.deviceType, billingEmail,billingPhone);
        order.orderState = OrderState.AWAITING_PAYMENT;
        order.billingEmail = billingEmail;
        OrderDAO.INSTANCE.updateModel(order,
                Arrays.asList(new String[] { Order.ORDER_STATE, "billingEmail" }));
        return chargingUrl;
    }

    @Override
    public String getVedantuTransactionId(String userId, long orderId, String paymentChannel,
            DeviceType deviceType, int amount, String currencyCode) {

        Transaction transaction = new Transaction(userId, orderId, paymentChannel, deviceType,
                TransactionType.CREDIT, amount, currencyCode);
        TransactionDAO.INSTANCE.save(transaction);
        return transaction._getStringId();
    }

    public Transaction updateTransactionStatus(long orderId, String transactionId,
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

    public OnPaymentReceivedRes updateOrderAndGetPaymentReceivedRes(Order order,
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

    public void generateInvoice(Order order) throws VedantuException {

        generateInvoice(order, UserDAO.INSTANCE.getById(order.customer.id), null, null);
    }

    public void generateInvoice(Order order, User user, String contactNo, String address)
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

    private void populateBillAndShipAddress(String billingEmail, InvoiceInfo invoiceInfo,
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

    protected abstract String getChargingRequestUrl(String transactionId, long orderId, User user,
            int amount/* amount in paisa */, String currencyCode, DeviceType deviceType,
            String billingEmail, String billingPhone) throws VedantuException;

}
