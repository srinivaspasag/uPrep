package com.vedantu.content.pojos.responses.challenges;

import java.util.ArrayList;
import java.util.List;

import com.vedantu.commons.pojos.responses.IListResponseObj;
import com.vedantu.content.pojos.ChallengeTakenBasicInfo;
import com.vedantu.content.search.details.ChallengeSearchIndexDetails;
import com.vedantu.user.pojos.UserInfo;

public class GetChallengeRes extends ChallengeSearchIndexDetails implements
		IListResponseObj {

	public boolean attempted;
	public ChallengeTakenBasicInfo info;
	public List<UserInfo> toppers;

	public void addTopper(UserInfo topper) {
		if (toppers == null) {
			toppers = new ArrayList<UserInfo>();
		}
		if (topper != null) {
			toppers.add(topper);
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{attempted:");
		builder.append(attempted);
		builder.append(",info:");
		builder.append(info);
		builder.append(",toppers:");
		builder.append(toppers);
		builder.append(",");
		builder.append(super.toString());
		builder.append("}");
		return builder.toString();
	}

}
