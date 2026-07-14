package com.vedantu.content.pojos.requests.questions;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractOrgListReq;

public class GetSolutionsReq extends AbstractOrgListReq {

	@Required
	public String qId;
}
