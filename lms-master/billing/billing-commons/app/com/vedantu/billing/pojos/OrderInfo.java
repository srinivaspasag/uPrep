package com.vedantu.billing.pojos;

import java.util.List;

import com.vedantu.billing.enums.OrderState;
import com.vedantu.billing.models.OrderedItem;
import com.vedantu.commons.enums.DeviceType;
import com.vedantu.commons.pojos.Interval;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.responses.IListResponseObj;

public class OrderInfo implements IListResponseObj {

    public String            id;
    public CustomerInfo      customer;

    public long              orderId;

    public long              orderTime;
    public OrderState        orderState;

    public List<OrderedItem> items;

    public InvoiceInfo       invoiceInfo;
    public Interval          period;
    public boolean           consumed;
    public DeviceType        deviceType;

    public String            pointOfSale;
    public String            sellerReferenceNo;

    public OrderInfo(String id, SrcEntity customer, long orderId, long orderTime,
            OrderState orderState, List<OrderedItem> items, InvoiceInfo invoiceInfo,
            Interval period, boolean consumed, DeviceType deviceType, String pointOfSale,
            String sellerReferenceNo) {

        super();
        this.id = id;
        this.customer = new CustomerInfo();
        this.customer.id = customer.id;
        this.customer.type = customer.type;
        this.orderId = orderId;
        this.orderTime = orderTime;
        this.orderState = orderState;
        this.items = items;
        this.invoiceInfo = invoiceInfo;
        this.period = period;
        this.consumed = consumed;
        this.deviceType = deviceType == null ? DeviceType.UNKNOWN : deviceType;
        this.pointOfSale = pointOfSale;
        this.sellerReferenceNo = sellerReferenceNo;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{id:").append(id).append(", customer:").append(customer)
                .append(", orderId:").append(orderId).append(", orderTime:").append(orderTime)
                .append(", orderState:").append(orderState).append(", items:").append(items)
                .append(", invoiceInfo:").append(invoiceInfo).append(", period:").append(period)
                .append(", consumed:").append(consumed).append(", deviceType:").append(deviceType)
                .append("}");
        return builder.toString();
    }

}
