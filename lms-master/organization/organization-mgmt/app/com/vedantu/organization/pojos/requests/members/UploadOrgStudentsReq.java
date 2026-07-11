package com.vedantu.organization.pojos.requests.members;

import java.util.Map;

import play.mvc.Http.MultipartFormData;

import com.vedantu.commons.pojos.requests.AbstractFileUploadReq;

public class UploadOrgStudentsReq extends AbstractFileUploadReq {

	public String orgId;
	public String orgMemberId;

	public String programId;
	// If there should be no error in case a user exists
	public boolean merge;

	public UploadOrgStudentsReq(MultipartFormData body) {
		super(body);

		Map<String, String[]> form = body.asFormUrlEncoded();
		orgId = _getValueFromMultipart(form, "orgId");
		orgMemberId = _getValueFromMultipart(form, "orgMemberId");
		programId = _getValueFromMultipart(form, "programId");
		merge = Boolean.parseBoolean(_getValueFromMultipart(form, "merge"));
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
		if (null == programId) {
			return "programId missing";
		}
		return null;
	}

}
