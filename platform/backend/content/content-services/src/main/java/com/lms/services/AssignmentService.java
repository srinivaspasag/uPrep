package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.tests.GetAssignmentInfoReq;
import com.lms.pojos.requests.tests.GetAssignmentsReq;
import com.lms.pojos.requests.tests.GetTestDetailsReq;

public interface AssignmentService {

    VedantuResponse getAssignmentInfo(GetAssignmentInfoReq getAssignmentInfoReq);

    VedantuResponse getAssignmentQuestions(GetTestDetailsReq getTestDetailsReq);

    VedantuResponse getAssignments(GetAssignmentsReq getAssignmentsReq);

}
