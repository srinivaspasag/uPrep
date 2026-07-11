package com.lms.pojos.requests;

import com.lms.enums.EnumBasket;
import com.lms.enums.TestResultVisibility;
import com.lms.pojo.request.AbstractOrgScopeReq;
import com.lms.pojos.TestMetadata;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
public class CreateCMDSTestReq extends AbstractOrgScopeReq {
    @NotBlank
    public String name;
    public String code;
    @NotBlank
    public EnumBasket.TestType type;
    public String targetId;
    public String desc;
    @NotBlank
    public List<TestMetadata> metadata;
    public long duration;
    public String folderId;
    public boolean autoGenerateFlag;
    public boolean showAIR;
    public String resultVisibilityMessage;
    private TestResultVisibility resultVisibility;

    public TestResultVisibility getResultVisibility() {

        return resultVisibility == null ? TestResultVisibility.VISIBLE : resultVisibility;
    }

    public void setResultVisibility(TestResultVisibility resultVisibility) {

        this.resultVisibility = resultVisibility == null ? TestResultVisibility.VISIBLE
                : resultVisibility;
    }

}
