package com.vedantu.content.pojos.responses.analytics;

import com.vedantu.content.enums.TestResultVisibility;
import com.vedantu.content.pojos.responses.AbstractContentRes;

public class GetUserEntityResultAnalyticsRes extends AbstractContentRes {

	public int rank;// rank in the test
	public int AIR; // rank All India wise
    public boolean showAIR;
	public UserAnalyticsInfoRes info;
    public TestResultVisibility resultVisibility;

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{rank:");
		builder.append(rank);
		builder.append(", info:");
		builder.append(info);
		builder.append(", ");
		builder.append(super.toString());
		builder.append("}");
		return builder.toString();
	}

}
