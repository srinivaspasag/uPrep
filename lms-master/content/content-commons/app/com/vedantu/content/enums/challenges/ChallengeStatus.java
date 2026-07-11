package com.vedantu.content.enums.challenges;

public enum ChallengeStatus {
	ACTIVE, ENDED;

	public static ChallengeStatus valueOfKey(String key) {
		ChallengeStatus status = ENDED;
		try {
			status = ChallengeStatus.valueOf(key.trim().toUpperCase());
		} catch (Exception e) {
		}
		return status;
	}
}
