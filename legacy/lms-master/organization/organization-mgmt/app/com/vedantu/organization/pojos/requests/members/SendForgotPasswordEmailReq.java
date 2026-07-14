package com.vedantu.organization.pojos.requests.members;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAppCheckReq;

public class SendForgotPasswordEmailReq extends AbstractAppCheckReq {

	@Required
	public String orgId;
	@Required
	private String memberId;

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = StringUtils.upperCase(memberId);
	}

}
