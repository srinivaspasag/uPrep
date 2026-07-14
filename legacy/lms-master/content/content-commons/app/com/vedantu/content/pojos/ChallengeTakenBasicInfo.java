package com.vedantu.content.pojos;

import java.util.List;

import com.vedantu.commons.pojos.responses.ModelBasicInfo;
import com.vedantu.content.enums.challenges.MultiplierPowerType;
import com.vedantu.mongo.VedantuRecordState;

public class ChallengeTakenBasicInfo extends ModelBasicInfo {

	public String challengeId;
	public String userId;
	// startTime will be timeCreated
	public long endTime;
	public int bid;
	public boolean bidded;
	public int hint;
	public long answerTime;
	public int timeTaken;
	public boolean success;
	public boolean processed;
	public MultiplierPowerType multiplierPower;
	public int basePoint;
	public int totalPoint;
	public List<String> answer;

	public ChallengeTakenBasicInfo(String id, VedantuRecordState recordState,
			String challengeId, String userId, long endTime, int bid,
			boolean bidded, int hint, long answerTime, int timeTaken,
			boolean success, boolean processed,
			MultiplierPowerType multiplierPower, int basePoint, int totalPoint,
			List<String> answer) {
		super(id, recordState);
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
	}

	public ChallengeTakenBasicInfo(String id, VedantuRecordState recordState) {
		super(id, recordState);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{challengeId:");
		builder.append(challengeId);
		builder.append(", userId:");
		builder.append(userId);
		builder.append(", endTime:");
		builder.append(endTime);
		builder.append(", bid:");
		builder.append(bid);
		builder.append(", bidded:");
		builder.append(bidded);
		builder.append(", hint:");
		builder.append(hint);
		builder.append(", answerTime:");
		builder.append(answerTime);
		builder.append(", timeTaken:");
		builder.append(timeTaken);
		builder.append(", success:");
		builder.append(success);
		builder.append(", processed:");
		builder.append(processed);
		builder.append(", multiplierPower:");
		builder.append(multiplierPower);
		builder.append(", basePoint:");
		builder.append(basePoint);
		builder.append(", totalPoint:");
		builder.append(totalPoint);
		builder.append(", answer:");
		builder.append(answer);
		builder.append(", id:");
		builder.append(id);
		builder.append(", recordState:");
		builder.append(recordState);
		builder.append("}");
		return builder.toString();
	}

}
