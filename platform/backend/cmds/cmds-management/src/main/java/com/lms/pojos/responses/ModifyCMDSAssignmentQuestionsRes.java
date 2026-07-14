package com.lms.pojos.responses;

import com.lms.board.pojos.test.BoardSearchEntity;
import com.lms.common.vedantu.enums.BoardType;
import com.lms.enums.QuestionType;

public class ModifyCMDSAssignmentQuestionsRes extends BoardSearchEntity{

    public String qId;
    public QuestionType qType;
    public boolean success;
    public com.lms.pojos.search.details.BoardSearchEntity child;

    public ModifyCMDSAssignmentQuestionsRes() {
        super();
    }

    public ModifyCMDSAssignmentQuestionsRes(String name, String id, BoardType type,
                                            String qId, QuestionType qType) {
        super(name, id, type);
        this.qId = qId;
        this.qType = qType;
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("{qId:");
        builder.append(qId);
        builder.append(", qType:");
        builder.append(qType);
        builder.append(", success:");
        builder.append(success);
        builder.append(", child:");
        builder.append(child);
        builder.append("}");
        return builder.toString();
    }
}
