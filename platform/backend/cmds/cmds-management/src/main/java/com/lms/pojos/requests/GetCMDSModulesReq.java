package com.lms.pojos.requests;

import com.lms.enums.PublishedStatus;
import com.lms.pojo.request.AbstractOrgScopeReq;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class GetCMDSModulesReq extends AbstractOrgScopeReq {
    public PublishedStatus publishedStatus;
    public int start;
    public int size;
}
