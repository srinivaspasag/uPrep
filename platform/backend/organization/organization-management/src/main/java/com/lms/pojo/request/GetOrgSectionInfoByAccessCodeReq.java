package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
@Setter
@Getter
public class GetOrgSectionInfoByAccessCodeReq extends AbstractAppCheckReq {

    @NotBlank(message = "accesscode is not null")
    public String  accessCode;
    public boolean getOrgKey;
}
