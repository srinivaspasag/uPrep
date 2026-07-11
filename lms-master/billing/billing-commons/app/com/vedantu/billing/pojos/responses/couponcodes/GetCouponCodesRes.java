package com.vedantu.billing.pojos.responses.couponcodes;

import com.vedantu.billing.pojos.CouponCodeInfo;
import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.commons.pojos.responses.ListResponse;

public class GetCouponCodesRes implements IListResponseObj {
    public ListResponse<CouponCodeInfo> couponCodes;
}
