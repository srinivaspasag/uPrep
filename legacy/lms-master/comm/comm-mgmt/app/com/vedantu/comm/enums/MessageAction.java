package com.vedantu.comm.enums;

import play.Logger;
import play.Logger.ALogger;

public enum MessageAction {

	SEND, REPLY, FWD;
	private static final ALogger LOGGER = Logger.of(MessageAction.class);

	public static MessageAction getValueOfKey(String key) {
		MessageAction action = MessageAction.SEND;
		try {
			MessageAction.valueOf(key);
		} catch (Exception exception) {
			LOGGER.debug("Could not find enum for " + key, exception);
		}
		return action;
	}
}
