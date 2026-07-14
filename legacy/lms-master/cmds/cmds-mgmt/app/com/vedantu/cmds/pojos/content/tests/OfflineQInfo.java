package com.vedantu.cmds.pojos.content.tests;

import java.util.List;
import java.util.Map;

import com.vedantu.content.enums.QuestionType;
import com.vedantu.content.pojos.tests.QuestionResultStatus;

public class OfflineQInfo {

	public String id;
	public String qCode;
	public List<String> correctAnswer;
	public Map<Integer, List<String>> optionalCorrectAnswers;// not applicable of
														// matrixType question
	public Map<String, List<String>> matrixAnswer;
	public QuestionType type;

	public String courseBrdName;
	public String courseBrdId;

	public int totalMarks;
	public int negativeMarks;

	public QuestionResultStatus status;

	public OfflineQInfo(String qId, String qCode, List<String> correctAnswer,
			QuestionType type, String courseBrdName, String courseBrdId,
			int negativeMarks, int totalMarks) {
		this.qCode = qCode;
		this.id = qId;
		this.correctAnswer = correctAnswer;
		this.type = type;
		this.courseBrdName = courseBrdName;
		this.courseBrdId = courseBrdId;
		this.totalMarks = totalMarks;
		this.status = QuestionResultStatus.ACTIVE;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((id == null) ? 0 : id.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		OfflineQInfo other = (OfflineQInfo) obj;
		if (id == null) {
			if (other.id != null)
				return false;
		} else if (!id.equals(other.id))
			return false;
		return true;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{id:").append(id).append(", qCode:").append(qCode)
				.append(", correctAnswer:").append(correctAnswer)
				.append(", matrixAnswer:").append(matrixAnswer)
				.append(", type:").append(type).append(", courseBrdName:")
				.append(courseBrdName).append(", courseBrdId:")
				.append(courseBrdId).append(", totalMarks:").append(totalMarks)
				.append(", negativeMarks:").append(negativeMarks).append("}");
		return builder.toString();
	}

}
