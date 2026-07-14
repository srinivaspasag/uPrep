package com.vedantu.board.enums;

import play.Logger;
import play.Logger.ALogger;

public enum BoardContextType {

	UNKNOWN, GLOBAL, CONSUMER, ORG;

	private static final ALogger LOGGER = Logger.of(BoardContextType.class);

	public static BoardContextType valueOfKey(String name) {
		BoardContextType type = UNKNOWN;
		try {
			type = valueOf(name.trim().toUpperCase());
		} catch (Exception e) {
			LOGGER.error("unknown enum string: " + name);
		}
		return type;
	}

	public static BoardContextType getParentContextType(
			BoardContextType contextType) {
		BoardContextType parentContextType = ORG == contextType ? CONSUMER
				: (CONSUMER == contextType ? GLOBAL : null);
		return parentContextType;
	}

	public static boolean isTreeNameNeeded(BoardContextType contextType) {
		return CONSUMER == contextType || ORG == contextType;
	}
	
	public static boolean isGradeNeeded(BoardContextType contextType) {
		return CONSUMER == contextType || ORG == contextType;
	}
}
