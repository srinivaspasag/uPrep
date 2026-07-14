package com.vedantu.content.models.analytics;

public class QuestionMeasures {

	// overall question attempts of entity in the given acadDim
	public long attempts;

	public long correct;
	public long incorrect;
	public long partial;
	public long left;

	public long timeTaken;

	public QuestionMeasures() {
		super();
	}

	public QuestionMeasures(long attempts, long correct, long partial, long incorrect,
			long left, long timeTaken) {
		super();
		this.attempts = attempts;
		this.correct = correct;
		this.partial = partial;
		this.incorrect = incorrect;
		this.left = left;
		this.timeTaken = timeTaken;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("QuestionMeasures [attempts=");
		builder.append(attempts);
		builder.append(", correct=");
		builder.append(correct);
		builder.append(", incorrect=");
		builder.append(incorrect);
		builder.append(", left=");
		builder.append(left);
		builder.append(", timeTaken=");
		builder.append(timeTaken);
		builder.append("]");
		return builder.toString();
	}

}
