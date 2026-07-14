package com.vedantu.cmds.pojos.requests.questions;

import java.util.List;

import org.apache.commons.lang3.StringUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.content.pojos.Attachment;

public class AddSolutionReq extends AbstractAuthCheckReq {

	@Required
	public String questionId;
	@Required
	public String solution;
	public boolean delete;
	public String newSolution;
	public boolean edit;
	public List<Attachment> attachments;

	public String validate() {
		if (StringUtils.isEmpty(questionId)) {
			return "questionId missing";
		}
		if (edit && StringUtils.isEmpty(newSolution)) {
			return "new solution is missing";
		}
		if (StringUtils.isEmpty(solution)) {
			return "solution is missing";
		}

		if (delete && edit) {
			return "ambiguous input for update";
		}
		return null;
	}
}
