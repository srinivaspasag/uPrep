package com.vedantu.content.pojos.responses.analytics;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

import com.vedantu.content.pojos.responses.analytics.answers.BoardWiseQuestionsAttemptInfos;

public class GetUserEntityQuestionAttemptInfoListRes {

	public List<BoardWiseQuestionsAttemptInfos> boards;

	public void addBoardWiseQuestions(BoardWiseQuestionsAttemptInfos boardWiseQuestions) {
		if (boardWiseQuestions == null) {
			return;
		}
		if (boards == null) {
			boards = new ArrayList<BoardWiseQuestionsAttemptInfos>();
		}
		boards.add(boardWiseQuestions);
	}

	public BoardWiseQuestionsAttemptInfos __getBoardWiseQuestions(String id) {
		for (BoardWiseQuestionsAttemptInfos boardWiseQuestions : boards) {
			if (StringUtils.equals(id, boardWiseQuestions.id)) {
				return boardWiseQuestions;
			}
		}
		return null;
	}
}
