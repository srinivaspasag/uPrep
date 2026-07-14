package com.vedantu.content.pojos.requests.questions;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.requests.AbstractAuthCheckReq;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.content.enums.SolutionType;
import com.vedantu.content.managers.QuestionManager;
import com.vedantu.content.pojos.Attachment;

public class AddSolutionReq extends AbstractAuthCheckReq implements
		IReverseImageMapperProcessor {
	@Required
	public String qId;
	@Required
	public String content;
	public List<String> answers;
	public Map<String, List<String>> gridAnswer;
	public SolutionType type;
	public List<Attachment> attachments;

	public AddSolutionReq() {
	}

	public AddSolutionReq(String userId, String qid, String content,
			List<String> answers, Map<String, List<String>> gridAnswer,
			SolutionType type) {
		this.userId = userId;
		this.qId = qid;
		this.content = content;
		this.answers = answers;
		this.gridAnswer = gridAnswer;
		this.type = type;
	}

	@Override
	public void addImageSrcUrl() {

	}

	@Override
	public void removeImageSrc(boolean moveImages) throws IOException,
			EntityFileStorageException {
		content = QuestionManager.removeTempImageSrcAndSaveToFS(
				EntityType.SOLUTION, content, true, "content");
	}

}
