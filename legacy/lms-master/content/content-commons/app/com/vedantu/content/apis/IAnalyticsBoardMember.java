package com.vedantu.content.apis;

import java.util.List;

import com.vedantu.content.pojos.BoardAnalyticsInfo;

public interface IAnalyticsBoardMember {

	public int _getQusCount();

	public int _getTotalMarks();

	public BoardAnalyticsInfo _getEntity();

	public List<? extends IAnalyticsBoardMember> _getChildrenBoards();
}
