package com.vedantu.organization.pojos.requests.microsite;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GetOrgMicrositeConfigReq extends AbstractOrgScopeReq {

    @Required
    public String slug;

    public GetOrgMicrositeConfigReq() {

        callingUserId = "PUBLIC";
        userId = "PUBLIC";

    }
}
