package com.lms.services;

import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.pojos.requests.AddQuestionReq;
import com.lms.pojos.requests.GetCMDSQuestionReq;
import com.lms.pojos.requests.GetSolutionsReq;
import com.lms.pojos.requests.PublishQuestionAsChallengeReq;
import com.lms.pojos.requests.questions.GetQuestionsReq;
import com.lms.pojos.responce.EditQuestionReq;
import com.lms.pojos.responce.GetUsageReq;

public interface CmdsQuestionsService {
    VedantuResponse getCMDSmoduleInfo(AddQuestionReq addQuestionReq);

    VedantuResponse update(EditQuestionReq editQuestionReq);

    VedantuResponse publishQuestionAsChallenge(PublishQuestionAsChallengeReq publishQuestionAsChallengeReq);

    VedantuResponse getQuestion(GetCMDSQuestionReq getCMDSQuestionReq);

    VedantuResponse getQuestions(GetQuestionsReq getQuestionsReq);

    VedantuResponse getUsages(GetUsageReq getUsageReq);

    VedantuResponse getSolutions(GetSolutionsReq getSolutionsReq);

    VedantuResponse fixMissingSolutions(String orgId, String scope);

    VedantuResponse fixMissingSolution(String orgId, String scope, String cmdsQId);

    VedantuResponse fixBoards(String orgId, String boardId);

    VedantuResponse fixBoardMapping(String orgId);
}
