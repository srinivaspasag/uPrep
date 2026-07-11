package com.lms.user.vedantu.user.enums;


public enum Gender {

	UNKNOWN, MALE, FEMALE;
	public static Gender valueOfKey(String name) {
		name = name.toUpperCase();

		Gender gender = UNKNOWN;
		try {
			gender = valueOf(name);
		} catch (Throwable t) {
			if (name.equals("M")) {
				gender = MALE;
			} else if (name.equals("F") ){
				gender = FEMALE;
			}
		}
		return gender;
	}
}
