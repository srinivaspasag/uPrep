package com.vedantu.organization.pojos.requests.organizations;

import play.data.validation.Constraints.Required;

public class GetOrgReq {

    public String  userId;
    public boolean checkForTNC;

    @Required
    public String  orgId;
    public boolean getKey;

}
