package com.vedantu.content.models;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.google.code.morphia.annotations.Entity;
import com.google.code.morphia.annotations.Indexed;
import com.vedantu.content.enums.QuestionType;
import com.vedantu.mongo.VedantuBaseMongoModel;

@Entity(value = "answers", noClassnameStored = true)
public class Answer extends VedantuBaseMongoModel {

	@Indexed(unique = true)
	public String qId;
	public String userId;
	public List<String> answer;
	public Map<Integer, List<String>> optionalCorrectAnswers;// not applicable
																// for matrix
																// type question
	public Map<String, List<String>> matrixAnswer;
	public QuestionType qType;

	public Answer() {
		super();
	}

	public Answer(String qId, String userId, QuestionType qType) {
		super();
		this.qId = qId;
		this.userId = userId;
		this.qType = qType;
		this.answer = new ArrayList<String>();
		this.matrixAnswer = new HashMap<String, List<String>>();

	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("Answer [qId:").append(qId).append(", userId:")
				.append(userId).append(", answer:").append(answer)
				.append(", matrixAnswer:").append(matrixAnswer).append("]");
		return builder.toString();
	}

}
