package com.lms.requests;

import javax.validation.constraints.NotBlank;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class DeleteStatusFeedReq extends AbstractAuthCheckReq {
	@NotBlank(message = "id should not be empty")
	public String id;
	@NotBlank(message = "orgId should not be empty")
	public String orgId;
}
