package com.vedantu.cmds.pojos.requests.questions;

import java.util.List;
import java.util.Map;

import play.data.validation.Constraints.Required;

import com.vedantu.cmds.pojos.content.question.BasicSolutionInfo;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.content.enums.Difficulty;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.ei.utils.StringUtils;

public class AddQuestionReq extends AbstractAuthCheckReq {

	@Required
	public String						orgId;

	@Required
	public String						content;
	@Required
	public QuestionType					type;

	public List<String>					options;
	public List<String>					tags;
	public List<String>					columnA;
	public List<String>					columnB;
	public BasicSolutionInfo           solution;
	// @Required
	public List<String>					answers;
	public String						questionSetName;
	public String						questionSetId;

	public Map<String, List<String>>	gridAnswer;

	@Required
	public Difficulty					difficulty;
	public List<String>					hints;

	public List<String>					brdIds;
	public List<String>					targetIds;

	public String						origRefNo;

	public String						status;

	@Required
	public String						folderId;

	public String                       paraId;

}
