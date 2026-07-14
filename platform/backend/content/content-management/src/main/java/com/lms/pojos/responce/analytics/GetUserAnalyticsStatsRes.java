package com.lms.pojos.responce.analytics;

import com.lms.board.pojos.test.BoardBasicInfo;
import com.lms.models.EntityMeasures;
import com.lms.pojos.tests.UserBoardsStatsInfo;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
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
