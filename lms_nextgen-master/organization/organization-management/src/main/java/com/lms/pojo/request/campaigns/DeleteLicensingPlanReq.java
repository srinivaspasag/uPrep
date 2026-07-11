package com.lms.pojo.request.campaigns;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class DeleteLicensingPlanReq extends AbstractAuthCheckReq {
    @NotBlank(message = "planId should not be null")
    public String id;
}
