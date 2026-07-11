package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class AddContentToChannelReq extends AbstractOrgScopeReq {
    @NotBlank(message = "id should not be empty")
    public String id;// channel id

    @NotBlank(message = "entity should not be empty")
    public SrcEntity entity;
}
