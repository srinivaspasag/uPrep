package com.lms.pojos.requests;

import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetChallengeUserInfoReq extends AbstractOrgScopeReq {

    public String rankType;

}
