package com.vedantu.user.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class AcceptTnCReq extends AbstractAuthCheckReq {

	@Required
	public boolean agrees;
	@Required
	public String version;
}
