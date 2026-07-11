package com.vedantu.billing.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GenerateInvoiceReq extends AbstractOrgScopeReq {

    @Required
    public SrcEntity customer;

    @Required
    public long      till;

}
