package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.organization.enums.OrganizationStatus;

public class UpdateOrgStatusReq extends AbstractAuthCheckReq {

    @Required
    public String status;

    @Required
    public String orgId;

    @Required
    public String type;
}
