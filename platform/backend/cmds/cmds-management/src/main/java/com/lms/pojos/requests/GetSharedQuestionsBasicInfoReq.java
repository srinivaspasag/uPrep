package com.lms.pojos.requests;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetSharedQuestionsBasicInfoReq extends AbstractAuthCheckReq {
	@NotBlank(message = "orgId should not be empty")
	public String orgId;

}
