package com.vedantu.content.pojos;

import com.vedantu.commons.enums.boards.BoardType;
import com.vedantu.content.models.analytics.EntityMeasures;
import com.vedantu.content.search.details.boards.BoardSearchEntity;

public class BoardAnalyticsInfo extends BoardSearchEntity {

	public EntityMeasures measures;
	public int totalMarks;
	public int lastRank;

	public BoardAnalyticsInfo() {
		super();
	}

	public BoardAnalyticsInfo(String name, String id, BoardType type) {
		super(name, id, type);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{measures:");
		builder.append(measures);
		builder.append(", totalMarks:");
		builder.append(totalMarks);
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
