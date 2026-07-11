package com.lms.billing.model;

import com.lms.billing.enums.OrderState;
import com.lms.billing.enums.PaymentState;
import com.lms.billing.pojo.InvoiceInfo;
import com.lms.billing.pojo.OrderInfo;
import com.lms.billing.pojo.PaymentInfo;
import com.lms.common.vedantu.commons.pojos.requests.Interval;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.common.vedantu.enums.DeviceType;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.CollectionUtils;

import java.util.List;


/**
 * Customer creates an order but he is not sure what to order so he can put it in <b>draft</b>
 * states Customer adds items to content mainly plans and orders will be separate Customer finalises
 * order. Here vedantu will confirm order and then put it in processing state. Once in processing
 * state vedantu employee will work on gathering required items and ship those to customer probably
 * vedantu employee will be able to fulfil request partially and create new sub-order for order
 * containing partial items ( this order will be initiated from previous order state )
 *
 * @author vikram
 */
@Document(value = "orders")
@CompoundIndexes({@CompoundIndex(name = "orderId", unique = true) // can have multiple invoices with
// single order if order splits
})
@Setter
@Getter
public class Order extends VedantuBaseMongoModel {

    @Transient
    public final static String CUSTOMER = "customer";
    @Transient
    public final static String ORDER_ID    = "orderId";
    @Transient
    public final static String ITEMS       = "items";
    @Transient
    public final static String ORDER_TIME  = "orderTime";
    @Transient
    public final static String ORDER_STATE = "orderState";

    @Indexed
    public SrcEntity customer;                  // buyer

    public long orderId;

    public long parentOrderId;             // only in case splitt order

    public long orderTime;
    public OrderState orderState;
    public boolean consumed;                  // this will be update when the action
    // for which transaction was performed is
    // completed

    public List<OrderedItem> items;

    public InvoiceInfo invoiceInfo;

    public Interval period;

    public String billingEmail;              // this is the email to which we have
    // send to payment gateway

    public DeviceType deviceType;

    public String              ipAddress;

    // below two fields will be used when a payment is not made through vedantu system and the user
    // is added to a paid program directly, it can be
    // made through some external seller (i.e Flipkart,Amazon, Sanpdeal etc)
    public String              pointOfSale;
    public String              sellerReferenceNo;
    public String              paymentChannel;
    public String              paymentChannelTransactionId;

    public boolean             tempUser;

    // user who started this order
    public String              userId;

    public int                 totalAmount;
    public int                 discount;
    public int                 lpCreditsRedeemed;

    public String              couponCode;

    public String              item_sku;
    public Order() {

        super();
    }

    public Order(String userId, DeviceType deviceType, SrcEntity customer, long orderId) {

        super();
        this.userId = userId;
        this.deviceType = deviceType;
        this.customer = customer;
        this.orderId = orderId;
        this.orderState = OrderState.DRAFT;
        this.orderTime = System.currentTimeMillis();

    }

    public void calculateFinalBillAmount() {

        int subTotal = 0;
        int taxes = 0;
        int total = 0;
        if (items == null) {
            return;
        }

        for (OrderedItem item : items) {
            subTotal += item.costInfo.base;
            total += item.costInfo.total;
            if (item.costInfo.taxes != null) {
                for (Tax tax : item.getCostInfo().getTaxes()) {
                    taxes += tax.calculatedTax;
                }
            }
        }

        invoiceInfo.subTotal = subTotal;
        invoiceInfo.tax = taxes;
        invoiceInfo.total = total;
        invoiceInfo.discount = discount;
        invoiceInfo.amountDue = total - discount;
    }

    public void updateOrderTotal() {
        int total = 0;
        for (OrderedItem item : items) {
            total += item.costInfo.total;
        }
        this.totalAmount = total;
    }

    public boolean updatePaymentStatus(String paymentMode, String refNo, int amountPaid) {

        PaymentInfo paymentInfo = new PaymentInfo(paymentMode, refNo);
        this.invoiceInfo.amountPaid += amountPaid;
        this.invoiceInfo.amountDue -= amountPaid;
        this.invoiceInfo.paymentInfo = paymentInfo;
        this.invoiceInfo.paymentState = this.invoiceInfo.amountDue > 0 ? PaymentState.PARTIALLY_PAID
                : (amountPaid < invoiceInfo.total - invoiceInfo.discount ? PaymentState.UNPAID : PaymentState.PAID);
        if (this.invoiceInfo.paymentState == PaymentState.PAID) {
            this.orderState = OrderState.FINALIZED;
            return true;
        }
        return false;
    }

    public OrderInfo toOrderInfo() {

        OrderInfo orderInfo = new OrderInfo(_getStringId(), customer, orderId, orderTime,
                orderState, items, invoiceInfo, period, consumed, deviceType, pointOfSale,
                sellerReferenceNo);
        return orderInfo;
    }

    public String toStringOrderId() {

        return String.valueOf(orderId);
    }

    public String toStringOrderTotal() {
        return String.valueOf(totalAmount / 100);
    }

    public String toStringDiscountedAmount() {
        return String.valueOf((totalAmount - discount - lpCreditsRedeemed)/100);
    }

    public boolean __isValidItem(SrcEntity item) {

        if (CollectionUtils.isEmpty(items)) {
            return false;
        }

        for (OrderedItem oItem : items) {
            if (oItem.item != null && oItem.item.equals(item)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{customer:").append(customer).append(", orderId:").append(orderId)
                .append(", parentOrderId:").append(parentOrderId).append(", orderTime:")
                .append(orderTime).append(", orderState:").append(orderState).append(", consumed:")
                .append(consumed).append(", items:").append(items).append(", invoiceInfo:")
                .append(invoiceInfo).append(", period:").append(period).append(", id:").append(id)
                .append(", timeCreated:").append(timeCreated).append(", lastUpdated:")
                .append(lastUpdated).append(", recordState:").append(recordState).append("}");
        return builder.toString();
    }

}
