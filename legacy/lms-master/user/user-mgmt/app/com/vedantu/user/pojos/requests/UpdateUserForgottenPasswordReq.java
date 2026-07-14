package com.vedantu.user.pojos.requests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;

public class UpdateUserForgottenPasswordReq extends AbstractAppCheckReq {

	@Required
	public String userId;
	@Required
	public String code;
	@Required
	public String newPassword;

}
