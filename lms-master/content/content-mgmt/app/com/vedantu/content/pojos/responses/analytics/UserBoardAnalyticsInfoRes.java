package com.vedantu.content.pojos.responses.analytics;

import java.util.ArrayList;
import java.util.List;

import com.vedantu.content.models.analytics.EntityMeasures;
import com.vedantu.content.pojos.BoardAnalyticsInfo;

public class UserBoardAnalyticsInfoRes {

	public int totalMarks;// this will only be used when returning info for a
	// single user, (for group of users the info is
	// returned in entity metadata info)
	public int rank; // this will be used when showing analytics data for
	// individual user

	public int qusCount;
	public BoardAnalyticsInfo entity;
	public EntityMeasures measures;
	public List<UserBoardAnalyticsInfoRes> children;

	public UserBoardAnalyticsInfoRes(BoardAnalyticsInfo entity,
			EntityMeasures measures) {
		this.entity = entity;
		this.measures = measures;
	}

	public void addChildAnalytics(UserBoardAnalyticsInfoRes child) {
		if (child == null) {
			return;
		}
		if (this.children == null) {
			this.children = new ArrayList<UserBoardAnalyticsInfoRes>();
		}
		this.children.add(child);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{entity:");
		builder.append(entity);
		builder.append(", measures:");
		builder.append(measures);
		builder.append(", children:");
		builder.append(children);
		builder.append("}");
		return builder.toString();
	}

}
