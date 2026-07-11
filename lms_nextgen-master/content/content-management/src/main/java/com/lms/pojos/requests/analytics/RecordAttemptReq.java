package com.lms.pojos.requests.analytics;

import com.lms.common.vedantu.enums.EntityType;
import com.lms.pojos.requests.StartAttemptReq;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;


@Getter
@Setter
public class RecordAttemptReq extends StartAttemptReq {

	// Not required in case of Question
	// Required in case of test, assignment, challenge etc
	public String attemptId = "";
	@NotBlank(message = "qId should not be empty")
	public String qId;
	@NotNull
	public List<String> answerGiven;
	@NotNull
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
