package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

import com.lms.common.vedantu.enums.AccessScope;
import com.lms.common.vedantu.enums.RevenueModel;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class GetOrgSectionsReq extends AbstractAuthCheckReq {

    @NotBlank(message = "orgId should not be null")
    public String orgId;
    @NotBlank(message = "programId should not be null")
    public String programId;
    public String centerId;

    public AccessScope accessScope;
    public RevenueModel revenueModel;

}