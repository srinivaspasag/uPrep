package com.vedantu.organization.pojos.requests.members;

import java.util.Map;

import play.mvc.Http.MultipartFormData;

public class UploadProfilePicReq extends
		com.vedantu.user.pojos.requests.UploadProfilePicReq {

	public String orgId;
	public String targetUserId;
	public String targetOrgMemberId;

	public UploadProfilePicReq(MultipartFormData body) {
		super(body);
		Map<String, String[]> form = body.asFormUrlEncoded();
		orgId = _getValueFromMultipart(form, "orgId");
		targetUserId = _getValueFromMultipart(form, "targetUserId");
		targetOrgMemberId = _getValueFromMultipart(form, "targetOrgMemberId");
	}
	
	public String validate() {
		String superValidate = super.validate();
		if (null != superValidate) {
			return superValidate;
		}
		if (null == orgId) {
			return "orgId missing";
		}
		if (null == targetUserId) {
			return "targetUserId missing";
		}
		if (null == targetOrgMemberId) {
			return "targetOrgMemberId missing";
		}
		return null;
	}

}
