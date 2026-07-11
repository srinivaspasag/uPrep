package com.vedantu.content.models.analytics;

import com.vedantu.commons.enums.boards.BoardType;

public enum AcademicDimensionType {

	UNKNOWN, OVERALL, COURSE, TOPIC, SUBTOPIC;

	public static AcademicDimensionType getType(BoardType boardType) {
		try {
			return valueOf(boardType.name());
		} catch (Throwable t) {
			// swallow
		}
		return UNKNOWN;
	}

}
