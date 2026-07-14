package com.lms.pojo.responce;

import com.lms.billing.pojo.SaleDetailsInfo;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateSaleDetailsRes {
    public boolean done;
    public SaleDetailsInfo saleDetailsInfo;
}
