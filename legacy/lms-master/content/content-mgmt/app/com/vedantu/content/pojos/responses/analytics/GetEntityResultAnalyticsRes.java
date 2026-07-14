package com.vedantu.content.pojos.responses.analytics;

import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.content.pojos.tests.TestMiniInfo;

public class GetEntityResultAnalyticsRes extends
		ListResponse<GetUserEntityResultAnalyticsRes> {

	public TestMiniInfo info;// test info

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{info:");
		builder.append(info);
		builder.append(", totalHits:");
		builder.append(totalHits);
		builder.append(", list:");
		builder.append(list);
		builder.append("}");
		return builder.toString();
	}

}
