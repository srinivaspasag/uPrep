package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetOrgProgramCentersReq extends AbstractAuthCheckReq {

	@NotBlank(message = "orgId should not be null")
	public String orgId;
	@NotBlank(message = "programId should not be null")
	public String programId;

}
