package controllers;

import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.content.managers.QuestionManager;
import com.vedantu.content.pojos.requests.GetSimilarEntities;
import com.vedantu.content.pojos.requests.questions.AddQuestionReq;
import com.vedantu.content.pojos.requests.questions.AddSolutionReq;
import com.vedantu.content.pojos.requests.questions.GetQuestionReq;
import com.vedantu.content.pojos.requests.questions.GetQuestionsReq;
import com.vedantu.content.pojos.requests.questions.GetQuestionsSolutionsReq;
import com.vedantu.content.pojos.requests.questions.GetSolutionsReq;
import com.vedantu.content.pojos.responses.questions.AddQuestionRes;
import com.vedantu.content.pojos.responses.questions.AddSolutionRes;
import com.vedantu.content.pojos.responses.questions.GetQuestionRes;
import com.vedantu.content.pojos.responses.questions.GetQuestionsSolutionRes;
import com.vedantu.content.pojos.responses.questions.GetSolutionsRes;

public class Questions extends AbstractVedantuController {

    public static Result addQuestion() {

        Form<AddQuestionReq> addQusForm = Form.form(AddQuestionReq.class).bindFromRequest();
        if (addQusForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(addQusForm))).toObjectNode());
        }
        AddQuestionReq addQusReq = addQusForm.get();
        AddQuestionRes addQusRes = null;
        try {
            addQusRes = QuestionManager.addQuestion(addQusReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(addQusRes).toObjectNode());
    }

    public static Result addSolution() {

        Form<AddSolutionReq> addQusForm = Form.form(AddSolutionReq.class).bindFromRequest();
        if (addQusForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(addQusForm))).toObjectNode());
        }
        AddSolutionReq addSolutionReq = addQusForm.get();
        AddSolutionRes addSolutionRes = null;
        try {
            addSolutionRes = QuestionManager.addSolution(addSolutionReq, true);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(addSolutionRes).toObjectNode());
    }

    public static Result getQuestionInfo() {

        Form<GetQuestionReq> getQusForm = Form.form(GetQuestionReq.class).bindFromRequest();
        if (getQusForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getQusForm))).toObjectNode());
        }
        GetQuestionReq getQusReq = getQusForm.get();
        GetQuestionRes getQusRes = null;
        try {
            getQusRes = QuestionManager.getQuestionInfo(getQusReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getQusRes).toObjectNode());
    }

    public static Result getQuestions() {

        Form<GetQuestionsReq> getQuestionsForm = Form.form(GetQuestionsReq.class).bindFromRequest();
        if (getQuestionsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getQuestionsForm))).toObjectNode());
        }
        GetQuestionsReq getQuestionListReq = getQuestionsForm.get();
        ListResponse<GetQuestionRes> getQuestionsListRes = QuestionManager
                .getQuestions(getQuestionListReq);
        return ok(getResultResponse(getQuestionsListRes).toObjectNode());
    }

    public static Result getSimilarQuestions() {

        Form<GetSimilarEntities> getSimilarQuestionsForm = Form.form(GetSimilarEntities.class)
                .bindFromRequest();
        if (getSimilarQuestionsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getSimilarQuestionsForm))).toObjectNode());
        }
        GetSimilarEntities getSimilarQuestionsListReq = getSimilarQuestionsForm.get();
        SearchListResponse<GetQuestionRes> getQuestionsListRes = QuestionManager
                .getSimilarQuestion(getSimilarQuestionsListReq);
        return ok(getResultResponse(getQuestionsListRes).toObjectNode());
    }

    public static Result getSolutions() {

        Form<GetSolutionsReq> getSolForm = Form.form(GetSolutionsReq.class).bindFromRequest();
        if (getSolForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getSolForm))).toObjectNode());
        }
        GetSolutionsReq getSolutionsReq = getSolForm.get();
        GetSolutionsRes getSolutionsRes = null;
        try {
            getSolutionsRes = QuestionManager.getSolutions(getSolutionsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getSolutionsRes).toObjectNode());
    }

    /**
     * will return map of(key=qId) list of solution for multiple question
     * 
     * @return
     */
    public static Result getQuestionsSolutions() {

        Form<GetQuestionsSolutionsReq> getQusSolForm = Form.form(GetQuestionsSolutionsReq.class)
                .bindFromRequest();
        if (getQusSolForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getQusSolForm))).toObjectNode());
        }
        GetQuestionsSolutionsReq getQusSolutionsReq = getQusSolForm.get();
        GetQuestionsSolutionRes getQusSolutionsRes = null;
        try {
            getQusSolutionsRes = QuestionManager.getSolutionsMap(getQusSolutionsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getQusSolutionsRes).toObjectNode());
    }

}
