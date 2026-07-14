package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetCMDSQuestionReq extends AbstractAuthCheckReq {

    public String id;
    public String orgId;

}
