package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public abstract class AbstractAddOrgStructureReq extends AbstractAuthCheckReq {

    @Required
    public String orgId;
    @Required
    public String name;
    @Required
    public String code;

    public String desc;

}
