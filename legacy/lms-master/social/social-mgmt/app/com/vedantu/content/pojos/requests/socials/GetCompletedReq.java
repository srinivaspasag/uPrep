package com.vedantu.content.pojos.requests.socials;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.commons.pojos.requests.AbstractOrgListReq;


public class GetCompletedReq extends AbstractOrgListReq{
    @Required
    public SrcEntity context;
}
