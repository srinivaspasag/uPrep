package com.lms.pojos.requests.analytics;

import com.lms.common.vedantu.commons.pojos.requests.SrcEntity;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetUserEntityAnalyticsBySubjectReq {
    public SrcEntity test;
    public String orgId;
}
