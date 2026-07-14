package com.vedantu.content.pojos.tests;

import com.vedantu.commons.enums.boards.BoardType;
import com.vedantu.content.pojos.BoardAnalyticsInfo;

public class UserBoardsStatsInfo extends BoardAnalyticsInfo {

	public double percentage;

	public UserBoardsStatsInfo(String name, String id, BoardType type,
							   double percentage) {
		super(name, id, type);
		this.percentage = percentage;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{percentage:");
		builder.append(percentage);
		builder.append(", measures:");
		builder.append(measures);
		builder.append(", lastRank:");
		builder.append(lastRank);
		builder.append(", name:");
		builder.append(name);
		builder.append(", code:");
		builder.append(code);
		builder.append(", id:");
		builder.append(id);
		builder.append(", type:");
		builder.append(type);
		builder.append(", grades:");
		builder.append(grades);
		builder.append("}");
		return builder.toString();
	}

}
