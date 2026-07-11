package com.vedantu.comm.requests.remarks;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class AddRemarksReq extends AbstractAuthCheckReq {

	@Required
	public String	orgId;

	@Required
	public String	targetUserId;

	@Required
	public String	content;

	public AddRemarksReq() {

	}

	public String getOrgId() {
		return orgId;
	}

	public void setOrgId(String orgId) {
		this.orgId = orgId;
	}

	public String getTargetUserId() {
		return targetUserId;
	}

	public void setTargetUserId(String targetUserId) {
		this.targetUserId = targetUserId;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
