package com.vedantu.content.pojos.responses.challenges;

import java.util.List;

import com.vedantu.content.enums.challenges.MultiplierPowerType;

public class GetChallengeAttemptInfoRes {

	public String challengeId;
	public String userId;
	// startTime will be timeCreated
	public long endTime;
	public int bid;
	public boolean bidded;
	public int hint;
	public long answerTime;
	public long timeTaken;
	public boolean success;
	public boolean processed;
	public MultiplierPowerType multiplierPower;
	public int basePoint;
	public int totalPoint;
	public List<String> answer;
	public long timeCreated;
	public long lastUpdated;

	public GetChallengeAttemptInfoRes() {
		super();
	}

	public GetChallengeAttemptInfoRes(String challengeId, String userId,
			long endTime, int bid, boolean bidded, int hint, long answerTime,
			long timeTaken, boolean success, boolean processed,
			MultiplierPowerType multiplierPower, int basePoint, int totalPoint,
			List<String> answer, long timeCreated, long lastUpdated) {
		this.challengeId = challengeId;
		this.userId = userId;
		this.endTime = endTime;
		this.bid = bid;
		this.bidded = bidded;
		this.hint = hint;
		this.answerTime = answerTime;
		this.timeTaken = timeTaken;
		this.success = success;
		this.processed = processed;
		this.multiplierPower = multiplierPower;
		this.basePoint = basePoint;
		this.totalPoint = totalPoint;
		this.answer = answer;
		this.timeCreated = timeCreated;
		this.lastUpdated = lastUpdated;
	}

}
