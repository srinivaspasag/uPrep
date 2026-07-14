package com.vedantu.cmds.pojos.requests.exports;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;


public class MoveSDContentReq extends AbstractOrgScopeReq {

    public SrcEntity content;
    public String moveFromSDCardId;
    public String moveToSDCardId;

}
