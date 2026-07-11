package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class ValidateExternalURLReq extends AbstractAppCheckReq {

    public String url;
}
