package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class UpdateOrgStatusReq extends AbstractAppCheckReq {

    @NotBlank(message = "Status should not be null")
    public String status;

    @NotBlank(message = "orgId should not be null")
    public String orgId;

    @NotBlank(message = "type should not be null")
    public String type;



}
