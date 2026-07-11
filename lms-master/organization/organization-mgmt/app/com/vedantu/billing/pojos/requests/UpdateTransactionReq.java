package com.vedantu.billing.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.billing.enums.OrderState;
import com.vedantu.billing.enums.TransactionStatus;
import com.vedantu.billing.payment.managers.VedantuTransactionManager;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class UpdateTransactionReq extends AbstractAuthCheckReq {

    @Required
    public String            orderId;

    @Required
    public String            transactionId;

    @Required
    public SrcEntity         customer;

    @Required
    public TransactionStatus transactionStatus;

    @Required
    public OrderState        orderState;

    public long              transactionTime;

    @Required
    public int               amountPaid;                 // 100th unit

    @Required
    public String            paymentChannelTransactionId;
    public String            paymentInstrument;
    public String            paymentMethod;

    public String            transactionInfo;

    private String           paymentChannel;

    public String getPaymentChannel() {

        return paymentChannel == null ? VedantuTransactionManager.PAYMENT_CHANNEL : paymentChannel;
    }

    public void setPaymentChannel(String paymentChannel) {

        this.paymentChannel = paymentChannel;
    }

}
