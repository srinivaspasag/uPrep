package com.vedantu.cmds.pojos.content.tests;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractOrgListReq;

public class GetCMDSAssignmentQuestionsReq extends AbstractOrgListReq {

	@Required
	public String assignmentId;
	public String brdId;
}
