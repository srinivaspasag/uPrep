package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class GetOrgMicrositeConfigReq extends AbstractOrgScopeReq {
    @NotBlank(message = "slug should not be null")
    public String slug;


}
