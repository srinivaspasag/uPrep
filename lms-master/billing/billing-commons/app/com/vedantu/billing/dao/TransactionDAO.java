package com.vedantu.billing.dao;

import java.util.Arrays;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.bson.types.ObjectId;

import com.vedantu.billing.enums.TransactionStatus;
import com.vedantu.billing.models.CouponCode;
import com.vedantu.billing.models.Order;
import com.vedantu.billing.models.Transaction;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.mongo.VedantuBasicDAO;

public class TransactionDAO extends VedantuBasicDAO<Transaction, ObjectId> {

    public static TransactionDAO INSTANCE = new TransactionDAO();

    private TransactionDAO() {

        super(Transaction.class);
    }

    public Transaction getTransaction(String transactionId) throws VedantuException {

        if (StringUtils.isEmpty(transactionId)) {
            throw new VedantuException(VedantuErrorCode.INVALID_TRANSACTION,
                    "invalid transactionId");
        }
        Transaction transaction = getById(transactionId);
        if (transaction == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_TRANSACTION,
                    "invalid transactionId");
        }
        return transaction;
    }

    public Transaction getTransactionByOrderId(long orderId) throws VedantuException {

        Transaction transaction = getQuery().filter(Order.ORDER_ID, orderId).get();
        if (transaction == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_TRANSACTION,
                    "invalid transactionId for orderId: " + orderId);
        }
        return transaction;
    }

    public Transaction verifyTransactionInfo(SrcEntity item, String transactionId)
            throws VedantuException {

        Transaction transaction = TransactionDAO.INSTANCE.getTransaction(transactionId);
        if (transaction == null) {
            throw new VedantuException(VedantuErrorCode.INVALID_TRANSACTION,
                    "invalid transactionId");
        }

        Order order = OrderDAO.INSTANCE.getOrderById(transaction.orderId);

        if (!order.__isValidItem(item)) {
            throw new VedantuException(VedantuErrorCode.INVALID_TRANSACTION, "transaction["
                    + transactionId + "] is not associated to  : " + item);
        }
        return transaction;
    }

    public Transaction markTransactionConmpleted(SrcEntity item, String transactionId)
            throws VedantuException {

        Transaction transaction = TransactionDAO.INSTANCE.getTransaction(transactionId);

        Order order = OrderDAO.INSTANCE.getOrderById(transaction.orderId);

        if (transaction.consumed) {
            throw new VedantuException(VedantuErrorCode.INVALID_TRANSACTION, "transaction["
                    + transactionId + "] is already completed");
        }

        if (transaction.status != TransactionStatus.SUCCESS) {
            throw new VedantuException(VedantuErrorCode.INCOMPLETE_TRANSACTION, "transaction["
                    + transactionId + "] was not completed, status: " + transaction.status);
        }

        if (item != null && !order.__isValidItem(item)) {
            throw new VedantuException(VedantuErrorCode.INVALID_TRANSACTION, "transaction["
                    + transactionId + "] is not associated to  : " + item);
        }
        transaction.consumed = true;
        order.consumed = true;
        List<String> updateFields = Arrays.asList(new String[] { "consumed" });
        updateModel(transaction, updateFields);
        OrderDAO.INSTANCE.updateModel(order, updateFields);
        if (order.couponCode != null && order.couponCode != "") {
            CouponCode couponCode = CouponCodeDAO.INSTANCE.getByCode(order.couponCode);
            couponCode.usageCount += 1;
            CouponCodeDAO.INSTANCE.save(couponCode);
        }
        return transaction;
    }

}
