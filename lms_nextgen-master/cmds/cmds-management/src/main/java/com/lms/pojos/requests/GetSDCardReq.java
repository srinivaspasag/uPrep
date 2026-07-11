package com.lms.pojos.requests;

import com.lms.pojo.request.AbstractOrgScopeReq;

import javax.validation.constraints.NotBlank;

public class GetSDCardReq extends AbstractOrgScopeReq {
    @NotBlank
    public String id;
    @NotBlank
    public String groupId;
}
