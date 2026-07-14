package com.vedantu.commons.enums;

import play.Logger;
import play.Logger.ALogger;

public enum Scope {

	UNKNOWN, PUBLIC, PRIVATE, ORG,LIBRARY;

	private static final ALogger LOGGER = Logger.of(Scope.class);

	public static Scope valueOfKey(String name) {
		Scope scope = UNKNOWN;
		try {
			scope = valueOf(name);
		} catch (Throwable t) {
			LOGGER.error("unknown enum string: " + name);
		}
		return scope;
	}

}
