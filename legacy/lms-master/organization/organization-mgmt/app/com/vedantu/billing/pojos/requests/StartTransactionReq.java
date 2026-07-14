package com.vedantu.billing.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.enums.DeviceType;
import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class StartTransactionReq extends AbstractAuthCheckReq {

    @Required
    public SrcEntity  customer;

    @Required
    public SrcEntity  item;

    @Required
    public String     itemName;

    public String     paymentChannel;

    @Required
    public DeviceType deviceType;

    // This is required for orgId specific payment channel.
    public String     orgId;
    public String     packageOrgId;
    public int        packageDays;
}
