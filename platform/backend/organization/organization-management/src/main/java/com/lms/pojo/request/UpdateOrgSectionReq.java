package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAddOrgStructureReq;
import com.lms.common.vedantu.enums.AccessScope;
import com.lms.common.vedantu.enums.RevenueModel;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Setter
@Getter
public class UpdateOrgSectionReq extends AbstractAddOrgStructureReq {

    @NotBlank(message = "programId should not be null")
    public String       programId;
    @NotBlank(message = "sectionId should not be null")
    public String       sectionId;

    public AccessScope accessScope;

    public RevenueModel revenueModel;

    public Boolean      sdOnly;

    public List<String> descriptionPoints;

    public String       thumbnail;

    public String       imageNameWithExtension;

}
