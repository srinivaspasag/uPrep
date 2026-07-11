package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.*;

public interface CmdsAssignmentsService {
    VedantuResponse createAssignment(CreateCMDSTestReq createCMDSTestReq);

    VedantuResponse addQuestion(ModifyCMDSAssignmentQuestionsReq modifyCMDSAssignmentQuestionsReq);

    VedantuResponse removeQuestion(ModifyCMDSAssignmentQuestionsReq modifyCMDSAssignmentQuestionsReq);

    VedantuResponse getAssignmentInfos(GetCMDSTestReq getCMDSTestReq);

    VedantuResponse getAssignmentQuestions(GetCMDSAssignmentQuestionsReq getCMDSAssignmentQuestionsReq);

    VedantuResponse finishAssignmentEditing(FinishCMDSAssignmentEditReq finishCMDSAssignmentEditReq);

    VedantuResponse getAssignments(GetTestsReq getTestsReq);

}
