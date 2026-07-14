package com.vedantu.content.pojos.responses.tests;

import com.vedantu.content.enums.EnumBasket.TestType;
import com.vedantu.mongo.VedantuRecordState;

public class GetAssignmentQuestionsRes extends GetTestQuestionsRes {

    public GetAssignmentQuestionsRes(String id, VedantuRecordState recordState) {

        super(id, recordState);
    }

    public GetAssignmentQuestionsRes(String id, VedantuRecordState recordState, String name,
            long duration, TestType type, String code) {

        super(id, recordState, name, duration, type, code);
    }

}
