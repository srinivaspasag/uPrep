package com.vedantu.cmds.pojos.requests.tests;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.cmds.pojos.content.tests.Metadata;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class CreateCMDSTestAutoReq extends AbstractOrgScopeReq {
    @Required
    public String                   testId;
    @Required
    public List<Metadata>           metadata;

}
