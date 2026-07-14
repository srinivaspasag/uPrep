package com.lms.pojos.requests;


import com.lms.common.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import com.lms.models.ModuleEntry;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class UpdateUserModuleReq extends AbstractAppCheckReq {
    @NotBlank(message = "userid should not be empty")
    public String userId;
    @NotBlank(message = "moduleid should not be empty")
    public String moduleId;
    @NotBlank(message = "moduleentry should not be empty")
    public ModuleEntry moduleEntry;
}
