package com.vedantu.content.pojos.responses.analytics;

import java.util.List;

import com.vedantu.commons.pojos.responses.ModelBasicInfo;

public class StartAttemptRes {

	public ModelBasicInfo info;
	public long startTime;
	public boolean isReattempt;
	public List<String> qIds;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{info:").append(info).append(", timeCreated:")
				.append(startTime).append(", isReattempt:")
				.append(isReattempt).append(", qIds:").append(qIds).append("}");
		return builder.toString();
	}

}
