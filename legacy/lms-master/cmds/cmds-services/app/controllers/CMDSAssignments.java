package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.cmds.managers.CMDSAssignmentManager;
import com.vedantu.cmds.pojos.content.tests.GetCMDSAssignmentQuestionsReq;
import com.vedantu.cmds.pojos.requests.tests.CreateCMDSTestReq;
import com.vedantu.cmds.pojos.requests.tests.FinishCMDSAssignmentEditReq;
import com.vedantu.cmds.pojos.requests.tests.GetCMDSTestReq;
import com.vedantu.cmds.pojos.requests.tests.ModifyCMDSAssignmentQuestionsReq;
import com.vedantu.cmds.pojos.responses.questions.EditContentRes;
import com.vedantu.cmds.pojos.responses.tests.CreateCMDSTestRes;
import com.vedantu.cmds.pojos.responses.tests.FinishCMDSTestEditRes;
import com.vedantu.cmds.pojos.responses.tests.GetCMDSAssignmentRes;
import com.vedantu.cmds.pojos.responses.tests.GetCMDSAssignmentsRes;
import com.vedantu.cmds.pojos.responses.tests.GetCMDSTestQuestionsRes;
import com.vedantu.cmds.pojos.responses.tests.ModifyCMDSAssignmentQuestionsRes;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.content.pojos.requests.EditContentReq;
import com.vedantu.content.pojos.requests.tests.GetTestsReq;

public class CMDSAssignments extends AbstractVedantuController {

    private static ALogger LOGGER = Logger.of(CMDSAssignments.class);
    
    public static Result createAssignment() {

        Form<CreateCMDSTestReq> createAssignmentForm = Form.form(CreateCMDSTestReq.class)
                .bindFromRequest();
        LOGGER.debug("request params : " + createAssignmentForm.data());
        if (createAssignmentForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(createAssignmentForm))).toObjectNode());
        }
        CreateCMDSTestReq createAssignmentReq = createAssignmentForm.get();
        CreateCMDSTestRes createAssignmentRes = null;
        try {
            createAssignmentRes = CMDSAssignmentManager.createAssignment(createAssignmentReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(createAssignmentRes).toObjectNode());
    }

    public static Result addQuestion() {

        Form<ModifyCMDSAssignmentQuestionsReq> addQuestionToAssignmentForm = Form.form(
                ModifyCMDSAssignmentQuestionsReq.class).bindFromRequest();
        if (addQuestionToAssignmentForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(addQuestionToAssignmentForm))).toObjectNode());
        }
        ModifyCMDSAssignmentQuestionsReq addQuestionToAssignmentReq = addQuestionToAssignmentForm
                .get();
        ModifyCMDSAssignmentQuestionsRes addQuestionToAssignmentRes = null;
        try {
            addQuestionToAssignmentRes = CMDSAssignmentManager
                    .addQuestion(addQuestionToAssignmentReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(addQuestionToAssignmentRes).toObjectNode());
    }

    public static Result removeQuestion() {

        Form<ModifyCMDSAssignmentQuestionsReq> removeQuestionToTestForm = Form.form(
                ModifyCMDSAssignmentQuestionsReq.class).bindFromRequest();
        if (removeQuestionToTestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(removeQuestionToTestForm))).toObjectNode());
        }
        ModifyCMDSAssignmentQuestionsReq removeQuestionToAssignmentReq = removeQuestionToTestForm
                .get();
        ModifyCMDSAssignmentQuestionsRes removeQuestionToAssignmentRes = null;
        try {
            removeQuestionToAssignmentRes = CMDSAssignmentManager
                    .removeQuestion(removeQuestionToAssignmentReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(removeQuestionToAssignmentRes).toObjectNode());
    }

    public static Result finishAssignmentEditing() {

        Form<FinishCMDSAssignmentEditReq> finishAssignmentEditingForm = Form.form(
                FinishCMDSAssignmentEditReq.class).bindFromRequest();
        if (finishAssignmentEditingForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(finishAssignmentEditingForm))).toObjectNode());
        }
        FinishCMDSAssignmentEditReq finishAssignmentEditingReq = finishAssignmentEditingForm.get();
        FinishCMDSTestEditRes finishAssignmentEditingRes = null;
        try {
            finishAssignmentEditingRes = CMDSAssignmentManager
                    .finishAssignmentEditing(finishAssignmentEditingReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(finishAssignmentEditingRes).toObjectNode());
    }

    public static Result getAssignmentInfo() {

        Form<GetCMDSTestReq> getAssignmentForm = Form.form(GetCMDSTestReq.class).bindFromRequest();
        if (getAssignmentForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getAssignmentForm))).toObjectNode());
        }
        GetCMDSTestReq getAssignmentReq = getAssignmentForm.get();
        GetCMDSAssignmentRes getAssignmentRes = null;
        try {
            getAssignmentRes = CMDSAssignmentManager.getAssignmentInfo(getAssignmentReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getAssignmentRes).toObjectNode());
    }

    /**
     * 
     * @return the list of questions of the Assignment
     */
    public static Result getAssignmentQuestions() {

        Form<GetCMDSAssignmentQuestionsReq> getAssignmentQuestionsForm = Form.form(
                GetCMDSAssignmentQuestionsReq.class).bindFromRequest();
        if (getAssignmentQuestionsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getAssignmentQuestionsForm))).toObjectNode());
        }
        GetCMDSAssignmentQuestionsReq getAssignmentQuestionsReq = getAssignmentQuestionsForm.get();
        GetCMDSTestQuestionsRes getTestQuestionsRes = null;
        try {
            getTestQuestionsRes = CMDSAssignmentManager
                    .getAssignmentQuestions(getAssignmentQuestionsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getTestQuestionsRes).toObjectNode());
    }

    public static Result getAssignments() {

        Form<GetTestsReq> requestForm = Form.form(GetTestsReq.class).bindFromRequest();
        GetCMDSAssignmentsRes response = null;
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GetTestsReq request = requestForm.get();

        try {
            response = CMDSAssignmentManager.getAssignments(request);
        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    /**
     * Edit existing question {@link EditContentReq}
     * 
     * @return { {@link EditContentRes}
     */
    public static Result update() {

        LOGGER.debug(" Called update");

        EditContentReq request = null;
        EditContentRes response = new EditContentRes();

        try {
            Form<EditContentReq> requestForm = Form.form(EditContentReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response.isUpdated = CMDSAssignmentManager.INSTANCE.update(request);
            response.id= request.entity.id;

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

}
