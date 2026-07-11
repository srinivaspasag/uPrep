package com.vedantu.content.enums.challenges;

public enum BidType {

	BIDDABLE, NON_BIDDABLE;

	public static BidType valueOfKey(String key) {
		BidType bidType = NON_BIDDABLE;
		try {
			bidType = valueOf(key.trim().toUpperCase());
		} catch (Exception e) {
		}
		return bidType;
	}
}
