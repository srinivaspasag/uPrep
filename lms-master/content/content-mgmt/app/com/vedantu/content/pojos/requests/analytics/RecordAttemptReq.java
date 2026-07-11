package com.vedantu.content.pojos.requests.analytics;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;

import play.data.validation.Constraints.Required;

import com.vedantu.commons.entity.storage.EntityFileStorageException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.utils.IReverseImageMapperProcessor;
import com.vedantu.content.managers.QuestionManager;
import com.vedantu.ei.utils.StringUtils;

public class RecordAttemptReq extends StartAttemptReq implements
IReverseImageMapperProcessor {

	// Not required in case of Question
	// Required in case of test, assignment, challenge etc
	public String attemptId = StringUtils.EMPTY;
	@Required
	public String qId;
	@Required
	public List<String> answerGiven;
	@Required
	public long timeTaken;
	public Map<String, List<String>> matrixAnswer;

	public RecordAttemptReq() {
		super();
	}

	public RecordAttemptReq(String callingUserId, String userId,
			String entityId, EntityType entityType, String setName,
			String attemptId, String qId, List<String> answerGiven,
			long timeTaken, Map<String, List<String>> matrixAnswer) {
		super(callingUserId, userId, entityId, entityType, setName);
		this.attemptId = attemptId;
		this.qId = qId;
		this.answerGiven = answerGiven;
		this.timeTaken = timeTaken;
		this.matrixAnswer = matrixAnswer;
	}

	public List<String> getAnswerGiven() {
		return answerGiven;
	}

	@Override
	public void addImageSrcUrl() {
	}

	@Override
	public void removeImageSrc(boolean moveImages)
			throws EntityFileStorageException, IOException {
		if (CollectionUtils.isNotEmpty(answerGiven)) {
			List<String> newAnswerGiven = new ArrayList<String>();
			for (String answer : answerGiven) {
				answer = QuestionManager.removeTempImageSrcAndSaveToFS(
						EntityType.SUBJECTIVEANSWER, answer, true, "content");
				newAnswerGiven.add(answer);
			}
			answerGiven = newAnswerGiven;
		}
	}

	public void setAnswerGiven(List<String> answerGiven) {
		this.answerGiven = answerGiven;
		if (CollectionUtils.isNotEmpty(this.answerGiven)) {
			Collections.sort(this.answerGiven);
		}
	}

	public Map<String, List<String>> getMatrixAnswer() {
		return matrixAnswer;
	}

	public void setMatrixAnswer(Map<String, List<String>> matrixAnswer) {
		this.matrixAnswer = matrixAnswer;
		if (MapUtils.isNotEmpty(this.matrixAnswer)) {
			for (Entry<String, List<String>> entry : this.matrixAnswer
					.entrySet()) {
				Collections.sort(entry.getValue());
			}
		}

	}

}
