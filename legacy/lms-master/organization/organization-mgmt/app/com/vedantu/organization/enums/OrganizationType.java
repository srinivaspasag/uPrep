package com.vedantu.organization.enums;

import play.Logger;
import play.Logger.ALogger;

public enum OrganizationType {

	UNKNOWN, COLLEGE, SCHOOL, UNIVERSITY, INSTITUTE, COMPANY;

	private static final ALogger LOGGER = Logger.of(OrganizationType.class);

	public static OrganizationType valueOfKey(String name) {
		OrganizationType orgType = UNKNOWN;
		try {
			orgType = valueOf(name);
		} catch (Throwable t) {
			LOGGER.error("unknown enum string: " + name);
		}
		return orgType;
	}

}
