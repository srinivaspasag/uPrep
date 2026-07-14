package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
@Setter
@Getter
public class AbstractAddOrgStructureReq extends AbstractAuthCheckReq {

    @NotBlank(message = "orgId should not be null")
    public String orgId;
    @NotBlank(message = "name should not be null")
    public String name;
    @NotBlank(message = "code should not be null")
    public String code;

    public String desc;

}