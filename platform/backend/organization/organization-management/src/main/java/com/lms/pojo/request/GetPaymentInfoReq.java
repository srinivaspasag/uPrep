package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetPaymentInfoReq extends AbstractAuthCheckReq {

    @NotBlank(message = "orgId should be mandatory")
    public String orgId;
    @NotBlank(message = "sectionId should be mandatory")
    public String sectionId;

}
