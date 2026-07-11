package com.lms.common.vedantu.commons.pojos.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public abstract class AbstractOrgListReq extends AbstractListReq {

    @NotBlank(message = "orgId is required")
    public String orgId;

}
