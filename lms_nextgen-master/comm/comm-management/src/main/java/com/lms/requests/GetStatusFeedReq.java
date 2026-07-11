package com.lms.requests;

import javax.validation.constraints.NotBlank;

import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

import lombok.Getter;
import lombok.Setter;
@Getter
@Setter
public class GetStatusFeedReq extends AbstractAuthCheckReq {
	@NotBlank(message = "feedId should not be empty")
	public String feedId;
	@NotBlank(message = "orgId should not be empty")
	public String orgId;
}
