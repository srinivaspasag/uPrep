package com.vedantu.comm.requests.newsfeeed;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetNewsFeedsReq extends AbstractAuthCheckReq {
	public int		size;
	public boolean	needClustered;
	public String	orgId;
}
