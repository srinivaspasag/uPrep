package com.vedantu.board.pojos.requests;

import play.mvc.Http.MultipartFormData;

import com.vedantu.commons.pojos.requests.AbstractFileUploadReq;

public class UploadGlobalBoardReq extends AbstractFileUploadReq {

	public UploadGlobalBoardReq(MultipartFormData body) {
		super(body);
	}

}
