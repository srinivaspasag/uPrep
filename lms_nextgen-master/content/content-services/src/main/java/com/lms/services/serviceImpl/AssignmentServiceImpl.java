package com.lms.services.serviceImpl;

import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.components.AssignmentComponent;
import com.lms.pojos.requests.tests.GetAssignmentInfoReq;
import com.lms.pojos.requests.tests.GetAssignmentsReq;
import com.lms.pojos.requests.tests.GetTestDetailsReq;
import com.lms.pojos.responce.SearchListResponse;
import com.lms.pojos.responce.tests.GetAssignmentInfoRes;
import com.lms.pojos.responce.tests.GetAssignmentQuestionsRes;
import com.lms.services.AssignmentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class AssignmentServiceImpl implements AssignmentService {
    @Autowired
    AssignmentComponent assignmentComponent;

    @Override
    public VedantuResponse getAssignmentInfo(GetAssignmentInfoReq getAssignmentInfoReq) {
        GetAssignmentInfoRes getAssignmentRes = null;
        try {
            getAssignmentRes = assignmentComponent.getAssignmentInfo(getAssignmentInfoReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(getAssignmentRes);
    }

    @Override
    public VedantuResponse getAssignmentQuestions(GetTestDetailsReq getTestDetailsReq) {
        GetAssignmentQuestionsRes getAssignmentDetailRes = null;
        try {
            getAssignmentDetailRes = assignmentComponent
                    .getAssignmentQuestions(getTestDetailsReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(getAssignmentDetailRes);
    }

    @Override
    public VedantuResponse getAssignments(GetAssignmentsReq getAssignmentsReq) {
        SearchListResponse<GetAssignmentInfoRes> getAssignmentsRes = null;
        try {
            getAssignmentsRes = assignmentComponent.getAssignments(getAssignmentsReq);
        } catch (VedantuException e) {
            throw e;
        }
        return new VedantuResponse(getAssignmentsRes);
    }

}
