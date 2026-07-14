package com.vedantu.content.pojos.responses.analytics;

import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.content.pojos.tests.MarkDistribution;

public class GetEntityMarkDistributionRes extends
		ListResponse<MarkDistribution> {

	public float avgScore;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{avgScore:");
		builder.append(avgScore);
		builder.append(", totalHits:");
		builder.append(totalHits);
		builder.append(", list:");
		builder.append(list);
		builder.append("}");
		return builder.toString();
	}

}
