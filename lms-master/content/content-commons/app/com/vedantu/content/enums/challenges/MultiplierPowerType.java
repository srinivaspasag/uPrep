package com.vedantu.content.enums.challenges;

public enum MultiplierPowerType {
	SINGLE(1), DOUBLE(2), TRIPLE(3);
	private int multiplier;

	private MultiplierPowerType(int multiplier) {
		this.multiplier = multiplier;
	}

	public int getMultiplier() {
		return multiplier;
	}
}
