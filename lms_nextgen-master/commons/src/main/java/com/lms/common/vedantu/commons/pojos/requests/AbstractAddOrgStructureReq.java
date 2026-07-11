package com.lms.common.vedantu.commons.pojos.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public abstract class AbstractAddOrgStructureReq extends AbstractAuthCheckReq {

    @NotBlank(message = "orgId is required")
    public String orgId;
    @NotBlank(message = "name is required")
    public String name;
    @NotBlank(message = "code is required")
    public String code;

    public String desc;

}
