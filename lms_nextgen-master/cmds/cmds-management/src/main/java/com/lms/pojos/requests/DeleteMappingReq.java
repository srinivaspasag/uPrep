package com.lms.pojos.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class DeleteMappingReq extends GetSharedQuestionsBasicInfoReq {
	@NotBlank(message = "parentOrgId should not be empty")
	public String parentOrgId;
	@NotBlank(message = "sharedToOrgId should not be empty")
	public String sharedToOrgId;
	@NotBlank(message = "parentBoardId should not be empty")
	public String parentBoardId;
	@NotBlank(message = "sharedToBoardId should not be empty")
	public String sharedToBoardId;

	public boolean visible;

	public boolean reSync;
	// This boolean is used when adding new para question to text question. This is
	// not used while sharing questions
	public boolean addNewPara;
}
