package com.lms.pojos.requests;

import com.lms.enums.Difficulty;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetResourcesReq extends AbstractGetResourcesReq {

	public Difficulty diffculty;

	@NotBlank(message = "folderId should not be empty")
	public String folderId; // directoryId

	public String quesType = "";
	public String paraId = "";

}
