package com.vedantu.content.enums;

import org.apache.commons.lang3.StringUtils;

public enum Difficulty {
	UNKNOWN, EASY, MODERATE, TOUGH;

	public static Difficulty valueOfKey(String value) {
		Difficulty difficulty = UNKNOWN;
		for (Difficulty diff : values()) {
			if (StringUtils.equalsIgnoreCase(value, diff.name())) {
				difficulty = diff;
				break;
			}
		}
		return difficulty;
	}
}
