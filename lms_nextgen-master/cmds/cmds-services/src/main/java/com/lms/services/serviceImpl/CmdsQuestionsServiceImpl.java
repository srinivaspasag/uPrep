package com.lms.services.serviceImpl;

import com.lms.common.exception.VedantuErrorCode;
import com.lms.common.exception.VedantuException;
import com.lms.common.vedantu.constants.HardCodedConstants;
import com.lms.common.vedantu.dto.response.VedantuResponse;
import com.lms.component.CMDSQuestionManager;
import com.lms.pojos.requests.AddQuestionReq;
import com.lms.pojos.requests.GetCMDSQuestionReq;
import com.lms.pojos.requests.GetSolutionsReq;
import com.lms.pojos.requests.PublishQuestionAsChallengeReq;
import com.lms.pojos.requests.questions.GetQuestionsReq;
import com.lms.pojos.responce.*;
import com.lms.pojos.responce.questions.GetSolutionsRes;
import com.lms.services.CmdsQuestionsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class CmdsQuestionsServiceImpl implements CmdsQuestionsService {
    @Autowired
    private CMDSQuestionManager cmdsQuestionManager;

    @Override
    public VedantuResponse getCMDSmoduleInfo(AddQuestionReq addQuestionReq) {
        AddQuestionRes addQuestionReqRes = cmdsQuestionManager.addQuestion(addQuestionReq);
        return new VedantuResponse(addQuestionReqRes);
    }

    @Override
    public VedantuResponse update(EditQuestionReq request) {

        EditContentRes response = cmdsQuestionManager.update(request);
        if (response.isUpdated == false) {
            throw new VedantuException(VedantuErrorCode.CONTENT_ASSOCIATED_WITH_QUESTION);
        }

        return new VedantuResponse(response);

    }

    @Override
    public VedantuResponse publishQuestionAsChallenge(PublishQuestionAsChallengeReq publishQuestionAsChallengeReq) {
        AddChallengeRes reponse = cmdsQuestionManager.publishCMDSQuestionAsChallenge(publishQuestionAsChallengeReq);
        return new VedantuResponse(publishQuestionAsChallengeReq);
    }

    @Override
    public VedantuResponse getQuestion(GetCMDSQuestionReq getCMDSQuestionReq) {
        GetCMDSQuestionRes response = cmdsQuestionManager.getQuestion(getCMDSQuestionReq);
        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse getQuestions(GetQuestionsReq getQuestionsReq) {

        GetCMDSQuestionSearchRes response = cmdsQuestionManager.getQuestions(getQuestionsReq);

        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse getUsages(GetUsageReq getUsageReq) {

        GetMultiUsageRes response = cmdsQuestionManager.getUsages(getUsageReq);

        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse getSolutions(GetSolutionsReq getSolutionsReq) {

        GetSolutionsRes getSolutionsRes = cmdsQuestionManager.getSolutions(getSolutionsReq);

        return new VedantuResponse(getSolutionsRes);
    }

    @Override
    public VedantuResponse fixMissingSolutions(String orgId, String scope) {
        EditContentRes response = new EditContentRes();

        cmdsQuestionManager.fixMissingSolutions(orgId, scope, HardCodedConstants.emptyString);

        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse fixMissingSolution(String orgId, String scope, String cmdsQId) {
        EditContentRes response = new EditContentRes();
        cmdsQuestionManager.fixMissingSolutions(orgId, scope, cmdsQId);
        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse fixBoards(String orgId, String boardId) {

        EditContentRes response = new EditContentRes();
        cmdsQuestionManager.fixBoards(orgId, boardId);
        return new VedantuResponse(response);
    }

    @Override
    public VedantuResponse fixBoardMapping(String orgId) {
        EditContentRes response = new EditContentRes();
        cmdsQuestionManager.fixBoardMappings(orgId);
        return new VedantuResponse(response);
    }

}
