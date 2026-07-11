package com.vedantu.user.pojos.requests;

import play.mvc.Http.MultipartFormData;

import com.vedantu.commons.pojos.requests.AbstractFileUploadReq;

public class UploadProfilePicReq extends AbstractFileUploadReq {

	public UploadProfilePicReq(MultipartFormData body) {
		super(body);
	}

}
