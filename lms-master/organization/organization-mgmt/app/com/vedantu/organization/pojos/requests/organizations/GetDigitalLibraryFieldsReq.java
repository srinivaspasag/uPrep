package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

public class GetDigitalLibraryFieldsReq {

    @Required
    public String           orgId;
}
