package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class GetEntityScheduleInfoReq extends AbstractOrgScopeReq {

    @NotBlank(message = "entity should not be null")
    public SrcEntity entity;
}
