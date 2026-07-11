package com.vedantu.content.pojos.responses.analytics.answers;

import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.content.models.analytics.QuestionMeasures;
import com.vedantu.content.search.details.QuestionSearchIndexDetails;

public class QuestionAttemptStatsInfoDetail implements IListResponseObj {
	public int qusNo;
	public QuestionSearchIndexDetails info;
	public QuestionMeasures measures;

	public QuestionAttemptStatsInfoDetail() {
		super();
	}

	public QuestionAttemptStatsInfoDetail(int qusNo, QuestionSearchIndexDetails info,
			QuestionMeasures measures) {
		this.qusNo = qusNo;
		this.info = info;
		this.measures = measures;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{qusNo:");
		builder.append(qusNo);
		builder.append(", info:");
		builder.append(info);
		builder.append(", measures:");
		builder.append(measures);
		builder.append("}");
		return builder.toString();
	}

}
