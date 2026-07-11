package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
@Setter
@Getter
public class CheckSlugReq extends AbstractAppCheckReq {

    @NotBlank(message = " slug should not be null")
    public String slug;
}