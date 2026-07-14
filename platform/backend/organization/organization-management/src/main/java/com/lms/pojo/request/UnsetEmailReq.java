package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class UnsetEmailReq extends AbstractAuthCheckReq {

	@NotBlank(message = "orgId should not be null")
	public String orgId;
	@NotBlank(message = "targetUserId should not be null")
	public String targetUserId;
	@NotBlank(message = "targetOrgMemberId should not be null")
	public String targetOrgMemberId;

}
