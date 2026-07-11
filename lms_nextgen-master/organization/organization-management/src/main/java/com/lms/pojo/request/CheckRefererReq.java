package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Setter
@Getter
public class CheckRefererReq extends AbstractAppCheckReq {

    @NotBlank(message = "referer should not be null")
    public String referer;
}
