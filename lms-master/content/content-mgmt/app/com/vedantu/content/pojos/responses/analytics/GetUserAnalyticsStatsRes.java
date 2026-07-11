package com.vedantu.content.pojos.responses.analytics;

import java.util.ArrayList;
import java.util.List;

import com.vedantu.board.pojos.BoardBasicInfo;
import com.vedantu.content.models.analytics.EntityMeasures;
import com.vedantu.content.pojos.tests.UserBoardsStatsInfo;

public class GetUserAnalyticsStatsRes {

	public EntityMeasures measures;//
	public long totalAttempts;
	public double percentage;
	public List<UserBoardsStatsInfo> boards;

	public void addBoardAnalytics(BoardBasicInfo boardInfo,
			EntityMeasures measures, double percentage) {
		if (boards == null) {
			boards = new ArrayList<UserBoardsStatsInfo>();
		}
		UserBoardsStatsInfo boardEntityInfo = new UserBoardsStatsInfo(
				boardInfo.name, boardInfo.id, boardInfo.type, percentage);
		boardEntityInfo.code = boardInfo.code;
		boardEntityInfo.measures = measures;
		boards.add(boardEntityInfo);
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("{measures:");
		builder.append(measures);
		builder.append(", totalAttempts:");
		builder.append(totalAttempts);
		builder.append(", percentage:");
		builder.append(percentage);
		builder.append(", boards:");
		builder.append(boards);
		builder.append("}");
		return builder.toString();
	}

}
