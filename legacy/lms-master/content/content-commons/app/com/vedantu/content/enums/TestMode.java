package com.vedantu.content.enums;

public enum TestMode {
	ONLINE, OFFLINE;

	public static TestMode valueOfKey(String key) {
		TestMode mode = ONLINE;
		try {
			mode = valueOf(key.trim().toUpperCase());
		} catch (Exception e) {
		}
		return mode;
	}
}
