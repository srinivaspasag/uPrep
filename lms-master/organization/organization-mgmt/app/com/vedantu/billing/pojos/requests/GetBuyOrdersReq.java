package com.vedantu.billing.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.Interval;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GetBuyOrdersReq extends AbstractOrgScopeReq {

    @Required
    public SrcEntity customer;

    public int       start;
    public int       size;

    public Interval  period;
    public String    orderId;
    public String    invoiceNo;

}
