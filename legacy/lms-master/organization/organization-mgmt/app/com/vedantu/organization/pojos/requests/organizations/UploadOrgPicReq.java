package com.vedantu.organization.pojos.requests.organizations;

import java.util.Map;

import play.mvc.Http.MultipartFormData;

import com.vedantu.commons.pojos.requests.AbstractFileUploadReq;

public class UploadOrgPicReq extends AbstractFileUploadReq {

	public String orgId;
	public String orgMemberId;

	public UploadOrgPicReq(MultipartFormData body) {
		super(body);

		Map<String, String[]> form = body.asFormUrlEncoded();
		orgId = _getValueFromMultipart(form, "orgId");
		orgMemberId = _getValueFromMultipart(form, "orgMemberId");

	}

	public String validate() {
		String superValidate = super.validate();
		if (null != superValidate) {
			return superValidate;
		}
		if (null == orgId) {
			return "orgId missing";
		}
		if (null == orgMemberId) {
			return "orgMemberId missing";
		}
		return null;
	}
}
