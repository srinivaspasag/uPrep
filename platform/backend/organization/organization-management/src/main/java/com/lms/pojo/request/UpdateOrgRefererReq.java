package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Required;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Setter
@Getter
public class UpdateOrgRefererReq extends AbstractAuthCheckReq {

    public String referer;
    public boolean remove;

    @NotBlank(message = "orgId should not be null")
    public String orgId;
}
