package controllers;

import org.apache.commons.lang.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.cmds.managers.CMDSQuestionManager;
import com.vedantu.cmds.pojos.requests.GetUsageReq;
import com.vedantu.cmds.pojos.requests.questions.AddQuestionReq;
import com.vedantu.cmds.pojos.requests.questions.AddSolutionReq;
import com.vedantu.cmds.pojos.requests.questions.EditQuestionReq;
import com.vedantu.cmds.pojos.requests.questions.GetCMDSQuestionReq;
import com.vedantu.cmds.pojos.requests.questions.PublishQuestionAsChallengeReq;
import com.vedantu.cmds.pojos.responses.GetMultiUsageRes;
import com.vedantu.cmds.pojos.responses.questions.AddQuestionRes;
import com.vedantu.cmds.pojos.responses.questions.AddSolutionRes;
import com.vedantu.cmds.pojos.responses.questions.EditContentRes;
import com.vedantu.cmds.pojos.responses.questions.GetCMDSQuestionRes;
import com.vedantu.cmds.pojos.responses.questions.GetCMDSQuestionSearchRes;
import com.vedantu.cmds.pojos.responses.questions.GetSolutionsRes;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.content.pojos.requests.questions.GetQuestionsReq;
import com.vedantu.content.pojos.requests.questions.GetSolutionsReq;
import com.vedantu.content.pojos.responses.challenges.AddChallengeRes;

public class CMDSQuestions extends AbstractVedantuController {

    private static ALogger LOGGER = Logger.of(CMDSQuestions.class);

    // /**
    // * Given question info & folderId , question is created and added to folder
    // * {@link AddQuestionReq} {@link AddQuestionRes}
    // *
    // * @return
    // */
    // @Deprecated
    // public static Result publishQuestion() {
    //
    // LOGGER.debug(" Called createDirectory");
    //
    // PublishQuestionReq request = null;
    // PublishQuestionRes reponse = null;
    //
    // try {
    // Form<PublishQuestionReq> requestForm = Form.form(PublishQuestionReq.class)
    // .bindFromRequest();
    //
    // LOGGER.debug("Request " + requestForm.data());
    //
    // if (requestForm.hasErrors()) {
    //
    // return ok(getErrorResponse(
    // new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
    //
    // }
    // request = requestForm.get();
    // reponse = CMDSQuestionManager.INSTANCE.publishCMDSQuestion(request);
    //
    // } catch (VedantuException e) {
    //
    // return ok((new JSONResponse(e)).toObjectNode());
    // }
    // return ok(getResultResponse(reponse).toObjectNode());
    // }

    /**
     * CMDS addition of questions { {@link AddQuestionReq } {@link AddQuestionRes}
     *
     * @return
     */

    public static Result addQuestion() {

        LOGGER.debug(" Called createDirectory");

        AddQuestionReq addQuestionReq = null;
        AddQuestionRes addQuestionReqRes = null;

        try {
            Form<AddQuestionReq> requestForm = Form.form(AddQuestionReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {

                LOGGER.debug(" Request error" + requestForm.errors());

                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            addQuestionReq = requestForm.get();
            addQuestionReqRes = CMDSQuestionManager.INSTANCE.addQuestion(addQuestionReq);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(addQuestionReqRes).toObjectNode());
    }


    public static Result getSolutions() {

        LOGGER.debug(" Called createDirectory");

        GetSolutionsReq getSolutionsReq = null;
        GetSolutionsRes getSolutionsRes = null;

        try {
            Form<GetSolutionsReq> requestForm = Form.form(GetSolutionsReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {

                LOGGER.debug(" Request error" + requestForm.errors());

                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            getSolutionsReq = requestForm.get();
            getSolutionsRes = CMDSQuestionManager.INSTANCE.getSolutions(getSolutionsReq);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(getSolutionsRes).toObjectNode());
    }

    /**
     * Add solution to existing question {@link AddSolutionReq} { {@link AddSolutionRes}
     *
     * @return
     */
    public static Result addSolution() {

        LOGGER.debug(" Called addsolution");

        AddSolutionReq request = null;
        AddSolutionRes reponse = null;

        try {
            Form<AddSolutionReq> requestForm = Form.form(AddSolutionReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            reponse = CMDSQuestionManager.INSTANCE.addSolution(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(reponse).toObjectNode());

    }

    /**
     * Edit existing question {@link EditQuestionReq}
     *
     * @return { {@link EditContentRes}
     */
    public static Result update() {

        LOGGER.debug(" Called createDirectory");

        EditQuestionReq request = null;
       // QuestionUpdateRes questionUpdateRes = new QuestionUpdateRes();
        EditContentRes response = new EditContentRes();

        try {
            Form<EditQuestionReq> requestForm = Form.form(EditQuestionReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = CMDSQuestionManager.INSTANCE.update(request);
            if(response.isUpdated == false)
            {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.CONTENT_ASSOCIATED_WITH_QUESTION),response).toObjectNode());
            }
            //response.isUpdated = CMDSQuestionManager.INSTANCE.update(request).isUpdated;
        } catch (VedantuException e) {
        	LOGGER.error(e.getMessage(),e);
//            return ok(getErrorResponse(
//                    new VedantuException(VedantuErrorCode.SERVICE_ERROR)).toObjectNode());
//
            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result fixBoards(String orgId, String boardId) {

        LOGGER.debug(" Called fixBoards");
        EditContentRes response = new EditContentRes();
        CMDSQuestionManager.INSTANCE.fixBoards(orgId, boardId);
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result fixBoardMapping(String orgId) {

        LOGGER.debug(" Called fixBoardMapping");
        EditContentRes response = new EditContentRes();
        CMDSQuestionManager.INSTANCE.fixBoardMappings(orgId);
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result fixMissingSolutions(String orgId, String scope) {

        LOGGER.debug(" Called fixMissingSolutions");
        EditContentRes response = new EditContentRes();
        try {
            CMDSQuestionManager.INSTANCE.fixMissingSolutions(orgId, scope, StringUtils.EMPTY);
        } catch (VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result fixMissingSolution(String orgId, String scope, String cmdsQId) {

        LOGGER.debug(" Called fixMissingSolutions");
        EditContentRes response = new EditContentRes();
        try {
            CMDSQuestionManager.INSTANCE.fixMissingSolutions(orgId, scope, cmdsQId);
        } catch (VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    /**
     * Edit existing question {@link EditQuestionReq}
     *
     * @return { {@link EditQuestionRes}
     */
    public static Result getQuestion() {

        LOGGER.debug(" Called getQuestion");

        GetCMDSQuestionReq request = null;
        GetCMDSQuestionRes response = null;

        try {
            Form<GetCMDSQuestionReq> requestForm = Form.form(GetCMDSQuestionReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = CMDSQuestionManager.INSTANCE.getQuestion(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result publishQuestionAsChallenge() {

        LOGGER.debug(" Called publishQuestionAsChallenge");

        PublishQuestionAsChallengeReq request = null;
        AddChallengeRes reponse = null;

        Form<PublishQuestionAsChallengeReq> requestForm = Form.form(
                PublishQuestionAsChallengeReq.class).bindFromRequest();

        LOGGER.debug("Request " + requestForm.data());

        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, requestForm.errors()
                            .toString())).toObjectNode());
        }
        request = requestForm.get();
        try {
            reponse = CMDSQuestionManager.INSTANCE.publishCMDSQuestionAsChallenge(request);
        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(reponse).toObjectNode());
    }

    public static Result getQuestions() {

        Form<GetQuestionsReq> requestForm = Form.form(GetQuestionsReq.class).bindFromRequest();
        GetCMDSQuestionSearchRes response = null;
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GetQuestionsReq request = requestForm.get();

        try {
            response = CMDSQuestionManager.getQuestions(request);
        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    /**
     * Edit existing question {@link EditQuestionReq}
     *
     * @return { {@link EditContentRes}
     */
    public static Result getUsages() {

        LOGGER.debug(" Called createDirectory");

        GetUsageReq request = null;
        GetMultiUsageRes response = new GetMultiUsageRes();

        try {
            Form<GetUsageReq> requestForm = Form.form(GetUsageReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = CMDSQuestionManager.INSTANCE.getUsages(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }
}
