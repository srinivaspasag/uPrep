package com.lms.pojos.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class VisibleMappingReq extends GetSharedQuestionsBasicInfoReq {
	@NotBlank(message = "parentOrgId should not be empty")
	public String parentOrgId;
	@NotBlank(message = "sharedToOrgId should not be empty")
	public String sharedToOrgId;

	public boolean visible;
	public boolean isSelfVisible;
}
