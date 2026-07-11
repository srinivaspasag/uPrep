package com.vedantu.content.pojos.requests;

import play.data.validation.Constraints.Required;

public class GetVideoInfoReq {

	@Required
	public String url;
}
