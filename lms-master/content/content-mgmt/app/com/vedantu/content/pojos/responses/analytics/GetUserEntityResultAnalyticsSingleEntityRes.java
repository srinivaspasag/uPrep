package com.vedantu.content.pojos.responses.analytics;

public class GetUserEntityResultAnalyticsSingleEntityRes extends
		GetUserEntityResultAnalyticsRes {

	public long totalAttempts;
	public long AIAttempts;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{totalAttempts:");
		builder.append(totalAttempts);
		builder.append(", rank:");
		builder.append(rank);
		builder.append(", info:");
		builder.append(info);
		builder.append(", id:");
		builder.append(id);
		builder.append(", user:");
		builder.append(user);
		builder.append(", timeCreated:");
		builder.append(timeCreated);
		builder.append(", lastUpdated:");
		builder.append(lastUpdated);
		builder.append("}");
		return builder.toString();
	}

}
