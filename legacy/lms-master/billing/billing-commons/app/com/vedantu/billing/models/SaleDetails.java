package com.vedantu.billing.models;

import java.util.List;

import com.google.code.morphia.annotations.Entity;
import com.vedantu.billing.enums.PaymentType;
import com.vedantu.billing.pojos.SaleDetailsInfo;
import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "saledetails", noClassnameStored = true)
public class SaleDetails extends VedantuBaseMongoModel {

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

    public SaleDetails() {
    }

    public SaleDetails(String orgId, String orgMemberId, String orderId, String pointOfSale,
            String salesPersonId, String sectionId, long origSaleAmount, double discountPercentage,
            long roundOff, long totalSaleAmount, List<PaymentItem> paymentItems) {
        super();
        this.orgId = orgId;
        this.orgMemberId = orgMemberId;
        this.orderId = orderId;
        this.pointOfSale = pointOfSale;
        this.salesPersonId = salesPersonId;
        this.sectionId = sectionId;
        this.origSaleAmount = origSaleAmount;
        this.discountPercentage = discountPercentage;
        this.roundOff = roundOff;
        this.totalSaleAmount = totalSaleAmount;
        this.paymentItems = paymentItems;
        setPaymentItemsStatus();
    }

    public void calculateTotalSaleAmount() {
        this.totalSaleAmount = (long) (this.origSaleAmount
                - (this.origSaleAmount * this.discountPercentage / 100.0) - this.roundOff);
        // Round off the sale amount
        this.totalSaleAmount = (this.totalSaleAmount / 100) * 100;
    }

    private void setPaymentItemsStatus() {
        if (this.paymentItems == null) {
            return;
        }
        for (PaymentItem pItem : this.paymentItems) {
            if (pItem.paymentType.equals(PaymentType.CHEQUE)) {
                pItem.isReceived = false;
            } else {
                pItem.isReceived = true;
            }
        }
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        SaleDetailsInfo saleDetailsInfo = new SaleDetailsInfo(this);
        return saleDetailsInfo;
    }

}
