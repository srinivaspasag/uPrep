package com.lms.api;

import com.lms.pojos.tests.BoardAnalyticsInfo;

import java.util.List;

public interface IAnalyticsBoardMember {

    int _getQusCount();

    int _getTotalMarks();

    BoardAnalyticsInfo _getEntity();

    List<? extends IAnalyticsBoardMember> _getChildrenBoards();
}
