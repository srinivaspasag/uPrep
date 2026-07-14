package com.lms.pojo.request;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class UpdateOrgProgramReq extends AddOrgProgramReq {

	@NotBlank(message = "programId should not be null")
	public String programId;

}
