package com.lms.pojo.request;


import com.lms.common.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetAllUserDataReq extends AbstractAppCheckReq {
    @NotBlank(message = "orgId Should not be null")
    public String orgId;
    public String targetUserId;
    public long lastUpdated = Long.MIN_VALUE;
}
