package com.lms.pojos.requests;


import com.lms.common.vedantu.commons.pojos.requests.AbstractListReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetChannelsReq extends AbstractListReq {

    @NotBlank(message = "orgId should not be empty")
    public String orgId;

    @Override
    public String toString() {
        return " [orgId=" + orgId + ", toString()=" + super.toString() + "]";
    }
}
