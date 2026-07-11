package com.vedantu.user.enums;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;

public enum Gender {

	UNKNOWN, MALE, FEMALE;

	private static final ALogger LOGGER = Logger.of(Gender.class);

	public static Gender valueOfKey(String name) {
		name = StringUtils.upperCase(name);

		Gender gender = UNKNOWN;
		try {
			gender = valueOf(name);
		} catch (Throwable t) {
			if (StringUtils.equals("M", name)) {
				gender = MALE;
			} else if (StringUtils.equals("F", name)) {
				gender = FEMALE;
			} else {
				LOGGER.error("unknown enum string: " + name);
			}
		}
		return gender;
	}

}
