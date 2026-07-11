package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetUserModuleReq extends AbstractAppCheckReq {
    @NotBlank
    public String userId;
    @NotBlank
    public String moduleId;
}
