package com.vedantu.commons.enums.boards;

import play.Logger;
import play.Logger.ALogger;

public enum GradeType {

	UNKNOWN, K12, UND_GRAD, GRAD, POST_GRAD;

	private static final ALogger LOGGER = Logger.of(GradeType.class);

	public static GradeType valueOfKey(String key) {
		GradeType gradeType = UNKNOWN;
		try {
			gradeType = valueOf(key.trim().toUpperCase());
		} catch (Exception e) {
			LOGGER.error("unknown enum string: " + key);
		}
		return gradeType;
	}
}
