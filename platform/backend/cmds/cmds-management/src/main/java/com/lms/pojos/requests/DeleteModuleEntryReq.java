package com.lms.pojos.requests;

import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class DeleteModuleEntryReq extends AbstractOrgScopeReq {
    @NotBlank(message = "moduleId should not be null")
    public String moduleId;
    public int pos;
}
