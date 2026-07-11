package com.vedantu.user.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class UpdateUserPasswordReq extends AbstractAuthCheckReq {
	
	@Required
	public String targetUserId;
	@Required
	public String newPassword;

}
