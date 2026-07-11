package com.lms.pojos.responce.tests;

import com.lms.common.vedantu.mongo.VedantuRecordState;
import com.lms.enums.EnumBasket.TestType;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GetAssignmentQuestionsRes extends GetTestQuestionsRes {

    public GetAssignmentQuestionsRes(String id, VedantuRecordState recordState) {

        super(id, recordState);
    }

    public GetAssignmentQuestionsRes(String id, VedantuRecordState recordState, String name,
                                     long duration, TestType type, String code) {

        super(id, recordState, name, duration, type, code);
    }

}
