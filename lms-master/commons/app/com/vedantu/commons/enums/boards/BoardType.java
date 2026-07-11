package com.vedantu.commons.enums.boards;

import play.Logger;
import play.Logger.ALogger;

public enum BoardType {
	ORGANIZATION, EXAM, COURSE, TOPIC, SUBTOPIC, SUBSUBTOPIC, FORMULA, CAREER, GENERAL;

	private static final ALogger LOGGER = Logger.of(BoardType.class);

	public static BoardType valueOfKey(String key) {
		BoardType boardType = GENERAL;
		try {
			boardType = valueOf(key.trim().toUpperCase());
		} catch (Exception e) {
			LOGGER.error("unknown enum string: " + key);
		}
		return boardType;
	}
}
