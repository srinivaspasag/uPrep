package com.lms.pojos.requests;

import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class MoveModuleEntryReq extends AbstractOrgScopeReq {
    @NotBlank(message = "moduleId should not be null")
    public String moduleId;
    @NotBlank(message = "oldPos should not be null")
    public int oldPos;
    @NotBlank(message = "pos should not be null")
    public int pos;
}


