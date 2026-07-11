package com.lms.pojos.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class CreateFolderReq {
	@NotBlank(message = "userId should not be empty")
	public String userId;
	@NotBlank(message = "orgId should not be empty")
	public String orgId;
	@NotBlank(message = "name should not be empty")
	public String name;
	public String parentFolderId;

}
