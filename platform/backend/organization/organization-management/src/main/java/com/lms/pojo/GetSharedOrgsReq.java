package com.lms.pojo;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetSharedOrgsReq {

    public String providerOrgId;
    public String programId;
    public String subscriberOrgId;

}
