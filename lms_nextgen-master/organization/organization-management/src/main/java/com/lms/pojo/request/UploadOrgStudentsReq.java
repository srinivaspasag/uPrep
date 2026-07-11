package com.lms.pojo.request;


import javax.validation.constraints.NotBlank;

import org.springframework.web.multipart.MultipartFile;

import com.lms.common.vedantu.commons.pojos.requests.AbstractFileUploadReq;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class UploadOrgStudentsReq extends AbstractFileUploadReq {
	@NotBlank(message = "orgId should not be null")
	public String orgId;
	@NotBlank(message = "orgMemberId should not be null")
	public String orgMemberId;
	@NotBlank(message = "programId should not be null")
	public String programId;
	// If there should be no error in case a user exists
	public boolean merge;

	public UploadOrgStudentsReq(MultipartFile body) {
		super(body);

	}


}
