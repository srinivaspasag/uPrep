package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAddOrgStructureReq;
import com.lms.enums.ProgramCategory;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class AddOrgProgramReq extends AbstractAddOrgStructureReq {

    @NotBlank(message = "departmentId is required")
    public String departmentId;
    public String description;
    public long periodStart;
    public long periodEnd;
    public boolean isOffline;
    public ProgramCategory category;
    public boolean sharedProgramAccess;
}
