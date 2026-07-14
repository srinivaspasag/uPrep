package com.vedantu.cmds.pojos.requests.accesscodes;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class VerifyAccessCodeReq extends AbstractOrgScopeReq {

    @Required
    public String    email;
    @Required
    public String    code;
    @Required
    public String    deviceId;
    @Required
    public SrcEntity entity;
}
