package com.lms.pojos.requests;

import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetModuleReq extends AbstractOrgScopeReq {
    @NotBlank(message = "id should not be null")
    public String id;

}
