package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.pojos.ModuleEntryCompletionRule;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class UpdateModuleEntryReq extends AbstractAuthCheckReq {
    @NotBlank(message = "moduleId should not be null")
    public String moduleId;
    public int pos;
    public String name;
    public ModuleEntryCompletionRule completionRule;
}
