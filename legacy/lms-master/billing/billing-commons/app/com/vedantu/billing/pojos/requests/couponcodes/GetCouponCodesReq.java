package com.vedantu.billing.pojos.requests.couponcodes;

import com.vedantu.billing.enums.CouponStatus;
import com.vedantu.commons.pojos.requests.AbstractOrgListReq;

public class GetCouponCodesReq extends AbstractOrgListReq {
    public CouponStatus couponStatus;
}

