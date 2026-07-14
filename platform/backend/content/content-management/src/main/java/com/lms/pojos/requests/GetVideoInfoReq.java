package com.lms.pojos.requests;

import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
public class GetVideoInfoReq {

	@NotBlank(message = "url should not be empty")
	public String url;
}
