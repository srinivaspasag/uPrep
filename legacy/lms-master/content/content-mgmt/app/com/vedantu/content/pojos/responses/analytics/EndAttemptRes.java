package com.vedantu.content.pojos.responses.analytics;

import com.vedantu.commons.pojos.responses.ModelBasicInfo;

public class EndAttemptRes {

	public ModelBasicInfo info;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{info:");
		builder.append(info);
		builder.append("}");
		return builder.toString();
	}

}
