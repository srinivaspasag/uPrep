package com.vedantu.cmds.pojos.requests.exports;


import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class GetSDCardGroupReq extends AbstractOrgScopeReq {

    @Required
    public String groupId;
}
