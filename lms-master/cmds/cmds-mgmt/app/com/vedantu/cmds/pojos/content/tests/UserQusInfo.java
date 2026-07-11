package com.vedantu.cmds.pojos.content.tests;

import com.vedantu.content.pojos.tests.QuestionResultStatus;

public class UserQusInfo {
	public int qusNo;
	public String answerGiven;
	public String qId;
	public QuestionResultStatus status;
	public long timeTaken;

	public UserQusInfo(int qusNo, String answerGiven, long timeTaken) {
		this.qusNo = qusNo;
		this.answerGiven = answerGiven;
		this.timeTaken = timeTaken;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{qusNo:").append(qusNo).append(", answerGiven:")
				.append(answerGiven).append(", qId:").append(qId)
				.append(", status:").append(status).append(", timeTaken:")
				.append(timeTaken).append("}");
		return builder.toString();
	}

}
