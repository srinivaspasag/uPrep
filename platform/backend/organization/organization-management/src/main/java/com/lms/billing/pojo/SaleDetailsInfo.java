package com.lms.billing.pojo;


import com.lms.billing.model.PaymentItem;
import com.lms.billing.model.SaleDetails;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Setter
@Getter
public class SaleDetailsInfo extends ModelBasicInfo {

    public String            orgId;
    public String            orgMemberId;
    public String            orderId;
    public String            pointOfSale;
    public String            salesPersonId;
    public String            sectionId;

    public long              origSaleAmount;
    public double            discountPercentage;
    public long              roundOff;
    public long              totalSaleAmount;

    public List<PaymentItem> paymentItems;

    public SaleDetailsInfo(int origSaleAmount, double discountPercentage, long roundOff,
                           long totalSaleAmount, List<PaymentItem> paymentItems) {
        super();
        this.origSaleAmount = origSaleAmount;
        this.discountPercentage = discountPercentage;
        this.roundOff = roundOff;
        this.totalSaleAmount = totalSaleAmount;
        this.paymentItems = paymentItems;
    }

    public SaleDetailsInfo(SaleDetails saleDetails) {
        super(saleDetails._getStringId(), saleDetails.recordState);
        this.orgId = saleDetails.orgId;
        this.orgMemberId = saleDetails.orgMemberId;
        this.orderId = saleDetails.orderId;
        this.pointOfSale = saleDetails.pointOfSale;
        this.salesPersonId = saleDetails.salesPersonId;
        this.sectionId = saleDetails.sectionId;
        this.origSaleAmount = saleDetails.origSaleAmount;
        this.discountPercentage = saleDetails.discountPercentage;
        this.roundOff = saleDetails.roundOff;
        this.totalSaleAmount = saleDetails.totalSaleAmount;
        this.paymentItems = saleDetails.paymentItems;
    }

    public SaleDetailsInfo() {
    }



    @Override
    public String toString() {

        StringBuilder builder = new StringBuilder();
        builder.append("{id:").append(id)
                .append(", totalAmount:").append(totalSaleAmount)
                .append(", paymentItems:").append(paymentItems)
                .append("}");
        return builder.toString();
    }

}

