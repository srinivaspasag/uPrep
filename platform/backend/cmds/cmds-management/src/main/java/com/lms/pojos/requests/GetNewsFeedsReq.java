package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetNewsFeedsReq extends AbstractAuthCheckReq {
    public int		size;
    public boolean	needClustered;
    public String	orgId;
}
