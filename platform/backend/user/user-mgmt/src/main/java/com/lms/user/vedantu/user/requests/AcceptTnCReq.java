package com.lms.user.vedantu.user.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Setter
@Getter
public class AcceptTnCReq extends AbstractAuthCheckReq {

    @NotNull(message = "Agrees is required")
    public boolean agrees;
    @NotBlank(message = "Version is required")
    public String version;
}
