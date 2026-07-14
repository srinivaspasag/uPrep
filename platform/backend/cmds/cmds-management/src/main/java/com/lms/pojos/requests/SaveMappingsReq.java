package com.lms.pojos.requests;

import com.lms.pojo.BoardMappings;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.List;

@Getter
@Setter
public class SaveMappingsReq extends GetSharedQuestionsBasicInfoReq {
	@NotBlank(message = "parentOrgId should not be empty")
	public String parentOrgId;
	@NotBlank(message = "sharedToOrgId should not be empty")
	public String sharedToOrgId;
	@NotNull(message = "boardMappings should not be empty")
	public List<BoardMappings> boardMappings;
}
