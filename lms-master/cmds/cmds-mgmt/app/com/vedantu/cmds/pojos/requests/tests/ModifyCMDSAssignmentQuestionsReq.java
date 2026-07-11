package com.vedantu.cmds.pojos.requests.tests;

import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class ModifyCMDSAssignmentQuestionsReq extends AbstractOrgScopeReq {

    @Required
    public String assignmentId;
    @Required
    public String qId;
}
