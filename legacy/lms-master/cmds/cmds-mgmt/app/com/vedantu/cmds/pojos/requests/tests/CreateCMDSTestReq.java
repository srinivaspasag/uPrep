package com.vedantu.cmds.pojos.requests.tests;

import java.util.List;

import play.data.validation.Constraints.Required;

import com.vedantu.content.enums.EnumBasket.TestType;
import com.vedantu.content.enums.TestResultVisibility;
import com.vedantu.content.pojos.tests.TestMetadata;
import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class CreateCMDSTestReq extends AbstractOrgScopeReq {

    @Required
    public String                name;
    public String                code;
    @Required
    public TestType              type;
    public String                targetId;
    public String                desc;
    @Required
    public List<TestMetadata>    metadata;
    public long                  duration;
    public String                folderId;
    public boolean               autoGenerateFlag;
    public boolean               subjectiveTest;
    public boolean               showAIR;
    public boolean               isNTAPattern;

    private TestResultVisibility resultVisibility;
    public String                resultVisibilityMessage;

    public TestResultVisibility getResultVisibility() {

        return resultVisibility == null ? TestResultVisibility.VISIBLE : resultVisibility;
    }

    public void setResultVisibility(TestResultVisibility resultVisibility) {

        this.resultVisibility = resultVisibility == null ? TestResultVisibility.VISIBLE
                : resultVisibility;
    }

}
