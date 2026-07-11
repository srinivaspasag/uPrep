package com.lms.pojos.requests.analytics;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetUserEntityRankReq extends AbstractOrgScopeReq {
    @NotBlank(message = "entity cannot be empty")
    public SrcEntity entity;

    @NotBlank(message = "entity cannot be null")
    public String targetUserId;
}
