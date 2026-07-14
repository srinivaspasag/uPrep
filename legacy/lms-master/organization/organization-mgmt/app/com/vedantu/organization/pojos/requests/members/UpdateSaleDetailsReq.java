package com.vedantu.organization.pojos.requests.members;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.billing.models.PaymentItem;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class UpdateSaleDetailsReq extends AbstractAuthCheckReq {

    @Required
    public String            orgId;

    @Required
    public String            saleDetailsId;

    @Required
    public String            targetUserId;

    @Required
    public String            targetOrgMemberId;

    public List<PaymentItem> paymentItems;

}
