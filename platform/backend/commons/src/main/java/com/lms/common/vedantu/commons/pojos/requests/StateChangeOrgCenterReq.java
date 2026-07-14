package com.lms.common.vedantu.commons.pojos.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class StateChangeOrgCenterReq extends AbstractAuthCheckReq {

	@NotBlank(message = "orgId is required")
	public String orgId;
	@NotBlank(message = "centerId is required")
	public String centerId;
}
