package com.vedantu.organization.pojos.requests.members;

import java.util.Map;

import play.data.validation.Constraints.Required;
import play.mvc.Http.MultipartFormData;

import com.vedantu.commons.pojos.requests.AbstractFileUploadReq;

public class BulkUploadProfilePicsReq extends AbstractFileUploadReq {

	@Required
	public String orgId;

	public BulkUploadProfilePicsReq(MultipartFormData body) {
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
