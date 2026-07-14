package com.lms.pojos.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetFoldersReq {

	public String folderId;
	@NotBlank(message = "orgId should not be empty")
	public String orgId;
	@NotBlank(message = "userId should not be empty")
	public String userId;
	public int start;
	public int size;
}
