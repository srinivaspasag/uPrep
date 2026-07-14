package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.enums.PlanState;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class MarkStateReq extends AbstractAuthCheckReq {

    @NotBlank(message = "planId should not be null")
    public String planId;
    @NotBlank(message = "planstate should not be null")
    public PlanState state;


}
