package com.vedantu.content.pojos.responses.analytics;

import com.vedantu.content.enums.AnswerCorrectness;

import java.util.ArrayList;
import java.util.List;

public class RecordAttemptRes {

	public List<String> userAnswer;
	public boolean isJudgeable;
	public List<String> correctAnswer;
	public AnswerCorrectness isUserAnswerCorrect;
	public boolean isOnline; // Used for other cases
	public List<String> unattemptedQIds=new ArrayList<String>();
	public boolean quotaExpired;
	@Override
	public String toString() {
		return "RecordAttemptRes{" +
				"userAnswer=" + userAnswer +
				", isJudgeable=" + isJudgeable +
				", correctAnswer=" + correctAnswer +
				", isUserAnswerCorrect=" + isUserAnswerCorrect +
				", quotaExpired="+quotaExpired+
				'}';
	}
}
