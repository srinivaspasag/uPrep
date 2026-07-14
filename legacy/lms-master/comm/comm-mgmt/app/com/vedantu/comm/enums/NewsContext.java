package com.vedantu.comm.enums;

import play.Logger;
import play.Logger.ALogger;

public enum NewsContext {
	NEWSFEED, NOTIFICATION, EMAIL, ACIVITY_FEEDS;
	private static final ALogger LOGGER = Logger.of(NewsContext.class);

	public static NewsContext getValueOfKey(String key) {
		NewsContext status = NewsContext.NEWSFEED;
		try {
			status = NewsContext.valueOf(key);
		} catch (Exception exception) {
			LOGGER.debug("Could not find enum for " + key, exception);
		}
		return status;
	}
}
