package com.lms.pojo.request;

import com.lms.common.vedantu.enums.AccessScope;
import com.lms.common.vedantu.enums.RevenueModel;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;
@Setter
@Getter
public class AddOrgSectionReq extends AbstractAddOrgStructureReq {

    @NotBlank
    public String programId;
    @NotBlank(message = "centerId should not be null")
    public String centerId;

    public AccessScope scope;
    public RevenueModel revenueModel;
    public List<String> descriptionPoints;
    public String thumbnail;
    public String imageNameWithExtension;
}

