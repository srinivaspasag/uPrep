package com.lms.pojos.requests;


import com.lms.common.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.lms.enums.Difficulty;
import com.lms.enums.QuestionType;
import lombok.Getter;
import lombok.Setter;

import javax.validation.constraints.NotBlank;
import java.util.List;
import java.util.Map;

@Setter
@Getter
public class AddQuestionReq extends AbstractAuthCheckReq {

	@NotBlank(message = "orgId should not be null")
	public String orgId;

	@NotBlank(message = "content should not be null")
	public String content;
	@NotBlank(message = "type should not be null")
	public QuestionType type;

	public List<String> options;
	public List<String> tags;
	public List<String> columnA;
	public List<String> columnB;
	public BasicSolutionInfo solution;
	// @Required
	public List<String> answers;
	public String questionSetName;
	public String questionSetId;

	public Map<String, List<String>> gridAnswer;

	@NotBlank(message = "difficulty should not be null")
	public Difficulty difficulty;
	public List<String> hints;

	public List<String> brdIds;
	public List<String> targetIds;

	public String origRefNo;

	public String status;

	@NotBlank(message = "folderId should not be null")
	public String folderId;

	public String paraId;

}
