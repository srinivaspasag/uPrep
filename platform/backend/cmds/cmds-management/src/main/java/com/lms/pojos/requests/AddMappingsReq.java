package com.lms.pojos.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class AddMappingsReq extends GetSharedQuestionsBasicInfoReq {
	@NotBlank(message = "parentOrgId should not be empty")
	public String parentOrgId;
	@NotBlank(message = "targetOrgId should not be empty")
	public String targetOrgId;
}
