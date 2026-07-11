package com.vedantu.organization.enums;

import play.Logger;
import play.Logger.ALogger;

public enum OrganizationStatus {

	UNKNOWN, REQUESTED, UNAPPROVED, APPROVED, BLOCKED, REMOVED;

	private static final ALogger LOGGER = Logger.of(OrganizationStatus.class);

	public static OrganizationStatus valueOfKey(String name) {
		OrganizationStatus status = UNKNOWN;
		try {
			status = valueOf(name);
		} catch (Throwable t) {
			LOGGER.error("unknown enum string: " + name);
		}
		return status;
	}

}
