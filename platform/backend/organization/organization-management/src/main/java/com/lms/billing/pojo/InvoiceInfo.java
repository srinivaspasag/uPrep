package com.lms.billing.pojo;

import com.lms.billing.enums.PaymentState;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.index.Indexed;
@Setter
@Getter
public class InvoiceInfo {
    @Value("${billing.tin}")
    private String billingTin;
    @Value("${billing.pan}")
    private String billingPan;
    @Indexed
    public String       invoiceNo;
    public long         issueTime;
    public SrcEntity customer;

    public String       tin;         // TIN
    public String       pan;         // PAN

    public AddressTo    shipTo;
    public AddressTo    billTo;

    public PaymentState paymentState;
    public PaymentInfo  paymentInfo;

    public String       currencyCode;

    public int          subTotal;    // in paisa (currencyCode==INR)
    public int          tax;
    public int          total;
    public int          discount;

    public int          amountPaid;
    public int          amountDue;

    public InvoiceInfo() {

        super();
        this.paymentState = PaymentState.UNPAID;
        this.issueTime = System.currentTimeMillis();
        this.tin = billingTin;
        this.pan =billingPan;
    }

    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{invoiceNo:").append(invoiceNo).append(", issueTime:").append(issueTime)
                .append(", customer:").append(customer).append(", tin:").append(tin)
                .append(", pan:").append(pan).append(", shipTo:").append(shipTo)
                .append(", billTo:").append(billTo).append(", paymentState:").append(paymentState)
                .append(", paymentInfo:").append(paymentInfo).append(", currencyCode:")
                .append(currencyCode).append(", subTotal:").append(subTotal).append(", tax:")
                .append(tax).append(", total:").append(total).append(", amountPaid:")
                .append(amountPaid).append(", amountDue:").append(amountDue).append("}");
        return builder.toString();
    }

}
