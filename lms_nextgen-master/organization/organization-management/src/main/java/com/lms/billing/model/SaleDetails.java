package com.lms.billing.model;

import com.lms.billing.enums.PaymentType;
import com.lms.billing.pojo.SaleDetailsInfo;
import com.lms.common.vedantu.commons.pojos.requests.responces.ModelBasicInfo;
import com.lms.common.vedantu.mongo.VedantuBaseMongoModel;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;


@Document(value = "saledetails")
@Setter
@Getter
public class SaleDetails extends VedantuBaseMongoModel {

    public String orgId;
    public String orgMemberId;
    public String orderId;
    public String pointOfSale;
    public String salesPersonId;
    public String sectionId;

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
            pItem.isReceived = !pItem.paymentType.equals(PaymentType.CHEQUE);
        }
    }

    @Override
    public ModelBasicInfo toBasicInfo() {

        SaleDetailsInfo saleDetailsInfo = new SaleDetailsInfo(this);
        return saleDetailsInfo;
    }

}
