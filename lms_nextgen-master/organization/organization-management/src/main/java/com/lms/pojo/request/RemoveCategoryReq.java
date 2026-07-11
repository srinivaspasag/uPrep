package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class RemoveCategoryReq extends AbstractOrgScopeReq {
    @NotBlank(message = "id should not be null")
    public String id;
}
