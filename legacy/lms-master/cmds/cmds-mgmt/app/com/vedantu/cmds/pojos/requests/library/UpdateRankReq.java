package com.vedantu.cmds.pojos.requests.library;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class UpdateRankReq extends AbstractOrgScopeReq {

    public SrcEntity target;
    public SrcEntity entity;
    public long      moveFrom;
    public long      moveTo;
}
