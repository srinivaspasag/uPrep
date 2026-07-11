package com.lms.pojos.responce;

import com.lms.pojos.responce.analytics.answers.BoardWiseQuestionsAttemptInfos;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Setter
@Getter
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
            if (id.equals(boardWiseQuestions.id)) {
                return boardWiseQuestions;
            }
        }
        return null;
    }

}
