package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetSaleDetailsReq extends AbstractAuthCheckReq {

    @NotBlank(message = "orgId should be mandatory")
    public String orgId;
    @NotBlank(message = "targetUserId should be mandatory")
    public String targetUserId;
    @NotBlank(message = "sectionId should be mandatory")
    public String sectionId;

}
