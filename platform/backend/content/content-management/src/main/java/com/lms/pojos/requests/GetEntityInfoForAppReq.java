package com.lms.pojos.requests;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class GetEntityInfoForAppReq extends AbstractAuthCheckReq {

    @NotNull
    public SrcEntity entity;
    @NotBlank(message = "orgId should not be empty")
    public String orgId;

}
