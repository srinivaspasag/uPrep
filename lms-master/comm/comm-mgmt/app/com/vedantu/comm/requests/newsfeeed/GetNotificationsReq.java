package com.vedantu.comm.requests.newsfeeed;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetNotificationsReq extends AbstractAuthCheckReq {

	public int		size;
	public boolean	needClustered;
	public String  	orgId;

}
