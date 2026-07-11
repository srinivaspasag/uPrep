package com.lms.pojo.request;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;

@Getter
@Setter
public class OrgProgramCoursesReq extends AbstractAuthCheckReq {
	@NotBlank(message = "orgId should not be null")
	public String orgId;

	@NotBlank(message = "programId should not be null")
	public String programId;

	public List<String> courseIds;
}
