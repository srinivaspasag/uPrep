package com.vedantu.cmds.pojos.content.question;

import com.vedantu.commons.enums.EntityType;
import com.vedantu.content.enums.QuestionStatus;
import com.vedantu.content.pojos.tests.Metadata;
import com.vedantu.mongo.VedantuRecordState;

public class CMDSQuestionSetInfo extends CMDSResourceInfo {

	public QuestionStatus	status;

	public Metadata			metadata;
	public int				numberOfQuestionsCompleted;

	public CMDSQuestionSetInfo(String id, String name, EntityType type,
			String orgId, long timeCreated, long lastUpdated, String addedBy,
			long programsAddedTo, boolean published, boolean completed, boolean converted,
			VedantuRecordState recordState, QuestionStatus status,
			int numberOfQuestionsCompleted, Metadata metadata) {
		super(id, name, type, orgId, timeCreated, lastUpdated, addedBy,
				programsAddedTo, published, completed, converted, null, recordState);
		this.numberOfQuestionsCompleted = numberOfQuestionsCompleted;
		this.status = status;
		this.metadata = metadata;
		// TODO Auto-generated constructor stub
	}

}
