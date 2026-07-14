package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.pojo.request.AbstractOrgScopeReq;

public class UpdateRankReq extends AbstractOrgScopeReq {

    public SrcEntity target;
    public SrcEntity entity;
    public long moveFrom;
    public long moveTo;
}
