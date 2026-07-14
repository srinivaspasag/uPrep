package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class GetOrgCourseProgramsReq extends AbstractAuthCheckReq {
    public String orgId;
    public String courseId;
}
