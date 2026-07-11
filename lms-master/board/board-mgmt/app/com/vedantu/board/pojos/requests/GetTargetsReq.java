package com.vedantu.board.pojos.requests;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;

public class GetTargetsReq extends AbstractAuthCheckReq {

	@Required
	public String ownerId;

	public String validate() {
		String superValidate = super.validate();
		if (StringUtils.isNotEmpty(superValidate)) {
			return superValidate;
		}
		if (StringUtils.isEmpty(ownerId)) {
			return "ownerId missing";
		}
		return null;
	}

}
