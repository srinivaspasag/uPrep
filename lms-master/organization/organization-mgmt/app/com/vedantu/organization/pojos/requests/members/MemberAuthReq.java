package com.vedantu.organization.pojos.requests.members;

import play.data.validation.Constraints.Required;

public class MemberAuthReq {

	@Required
	public String orgId;
	@Required
	private String memberId;
	@Required
	public String password;

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId;
	}

}
