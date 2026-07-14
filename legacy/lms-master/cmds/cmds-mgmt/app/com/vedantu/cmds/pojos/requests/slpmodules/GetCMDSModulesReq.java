package com.vedantu.cmds.pojos.requests.slpmodules;

import com.vedantu.cmds.enums.PublishedStatus;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;


public class GetCMDSModulesReq extends AbstractOrgScopeReq{
    public PublishedStatus publishedStatus;
    public int start;
    public int size;
}
