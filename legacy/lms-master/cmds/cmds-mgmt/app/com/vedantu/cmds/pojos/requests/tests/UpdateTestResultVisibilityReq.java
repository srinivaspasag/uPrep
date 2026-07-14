package com.vedantu.cmds.pojos.requests.tests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.SrcEntity;
import com.vedantu.content.enums.TestResultVisibility;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class UpdateTestResultVisibilityReq extends AbstractOrgScopeReq {

    @Required
    private TestResultVisibility resultVisibility;

    @Required
    public SrcEntity             entity;

    public String                resultVisibilityMessage;

    public TestResultVisibility getResultVisibility() {

        return resultVisibility;
    }

    public void setResultVisibility(TestResultVisibility resultVisibility) {

        this.resultVisibility = resultVisibility == null ? TestResultVisibility.VISIBLE
                : resultVisibility;
    }

}
