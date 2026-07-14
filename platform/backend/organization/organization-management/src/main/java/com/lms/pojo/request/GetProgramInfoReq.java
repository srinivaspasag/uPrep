package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetProgramInfoReq extends AbstractOrgScopeReq {

    @NotBlank(message = "programId should be mandatory")
    public String programId;
}
