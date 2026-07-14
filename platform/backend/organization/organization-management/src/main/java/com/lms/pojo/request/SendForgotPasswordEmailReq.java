package com.lms.pojo.request;


import com.lms.common.vedantu.commons.pojos.requests.AbstractAppCheckReq;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SendForgotPasswordEmailReq extends AbstractAppCheckReq {


	public String orgId;

	private String memberId;

	public String getMemberId() {
		return memberId;
	}

	public void setMemberId(String memberId) {
		this.memberId = memberId.toUpperCase();
	}

}
