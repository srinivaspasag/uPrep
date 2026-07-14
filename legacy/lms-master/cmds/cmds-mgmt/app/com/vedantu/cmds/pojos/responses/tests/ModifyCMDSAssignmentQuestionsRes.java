package com.vedantu.cmds.pojos.responses.tests;

import com.vedantu.commons.enums.boards.BoardType;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.search.details.boards.BoardSearchEntity;

public class ModifyCMDSAssignmentQuestionsRes extends BoardSearchEntity {

	public String qId;
	public QuestionType qType;
	public boolean success;
	public BoardSearchEntity child;

	public ModifyCMDSAssignmentQuestionsRes() {
		super();
	}

	public ModifyCMDSAssignmentQuestionsRes(String name, String id, BoardType type,
			String qId, QuestionType qType) {
		super(name, id, type);
		this.qId = qId;
		this.qType = qType;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{qId:");
		builder.append(qId);
		builder.append(", qType:");
		builder.append(qType);
		builder.append(", success:");
		builder.append(success);
		builder.append(", child:");
		builder.append(child);
		builder.append("}");
		return builder.toString();
	}

}
