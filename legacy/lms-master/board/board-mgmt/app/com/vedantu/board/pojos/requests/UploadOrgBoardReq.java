package com.vedantu.board.pojos.requests;

import java.util.Map;

import play.mvc.Http.MultipartFormData;

public class UploadOrgBoardReq extends UploadConsumerBoardReq {

	public String orgId;

	public UploadOrgBoardReq(MultipartFormData body) {
		super(body);

		Map<String, String[]> form = body.asFormUrlEncoded();

		orgId = _getValueFromMultipart(form, "orgId");
	}

	public String validate() {
		String superValidate = super.validate();
		if (null != superValidate) {
			return superValidate;
		}
		if (null == orgId) {
			return "orgId missing";
		}
		return null;
	}
}
