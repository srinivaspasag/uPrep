package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetUserMailBoxInfoReq extends AbstractAuthCheckReq {
    public String orgId;

}
