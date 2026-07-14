package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;

import com.vedantu.cmds.managers.CMDSTestManager;
import com.vedantu.cmds.managers.OfflineTestManager;
import com.vedantu.cmds.pojos.content.tests.GetCMDSTestQuestionsReq;
import com.vedantu.cmds.pojos.requests.tests.CreateCMDSTestAutoReq;
import com.vedantu.cmds.pojos.requests.tests.CreateCMDSTestReq;
import com.vedantu.cmds.pojos.requests.tests.FinishCMDSTestEditReq;
import com.vedantu.cmds.pojos.requests.tests.GetCMDSTestReq;
import com.vedantu.cmds.pojos.requests.tests.ModifyCMDSTestQuestionsReq;
import com.vedantu.cmds.pojos.requests.tests.SetPasswordForTestReq;
import com.vedantu.cmds.pojos.requests.tests.UpdateTestResultVisibilityReq;
import com.vedantu.cmds.pojos.requests.tests.UploadOfflineTestResultReq;
import com.vedantu.cmds.pojos.requests.tests.simplifyBoardNamesReq;
import com.vedantu.cmds.pojos.responses.questions.EditContentRes;
import com.vedantu.cmds.pojos.responses.tests.CreateCMDSTestRes;
import com.vedantu.cmds.pojos.responses.tests.FinishCMDSTestEditRes;
import com.vedantu.cmds.pojos.responses.tests.GetCMDSTestQuestionsRes;
import com.vedantu.cmds.pojos.responses.tests.GetCMDSTestRes;
import com.vedantu.cmds.pojos.responses.tests.GetCMDSTestsRes;
import com.vedantu.cmds.pojos.responses.tests.GetReGenerateAnalyticsRes;
import com.vedantu.cmds.pojos.responses.tests.ModifyCMDSTestQuestionsRes;
import com.vedantu.cmds.pojos.responses.tests.SetPasswordForTestRes;
import com.vedantu.cmds.pojos.responses.tests.UpdateTestResultVisibilityRes;
import com.vedantu.cmds.pojos.responses.tests.UploadOfflineTestResultRes;
import com.vedantu.cmds.pojos.responses.tests.simplifyBoardNamesRes;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.content.pojos.requests.EditContentReq;
import com.vedantu.content.pojos.requests.tests.GetTestsReq;

public class CMDSTests extends AbstractVedantuController {

    private static ALogger LOGGER = Logger.of(CMDSTests.class);

    public static Result uploadTestResults() {

        Form<UploadOfflineTestResultReq> uploadResultForm = Form.form(
                UploadOfflineTestResultReq.class).bindFromRequest();
        if (uploadResultForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(uploadResultForm))).toObjectNode());
        }
        UploadOfflineTestResultReq uploadResultReq = getUploadOfflineTestResultReq();
        UploadOfflineTestResultRes uploadResultRes = null;
        try {
            uploadResultRes = OfflineTestManager.uploadOfflineTestResult(uploadResultReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        } finally {
            if (uploadResultReq.resultFile != null) {
                uploadResultReq.resultFile.delete();
            }
        }
        return ok(getResultResponse(uploadResultRes).toObjectNode());
    }

    public static Result uploadTestResults2() {

        Form<UploadOfflineTestResultReq> uploadResultForm = Form.form(
                UploadOfflineTestResultReq.class).bindFromRequest();
        if (uploadResultForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(uploadResultForm))).toObjectNode());
        }
        UploadOfflineTestResultReq uploadResultReq = getUploadOfflineTestResultReq();
        UploadOfflineTestResultRes uploadResultRes = null;
        try {
            uploadResultRes = OfflineTestManager.uploadOfflineTestResult2(uploadResultReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        } finally {
            if (uploadResultReq.resultFile != null) {
                uploadResultReq.resultFile.delete();
            }
        }
        return ok(getResultResponse(uploadResultRes).toObjectNode());
    }

    public static Result createTest() {

        Form<CreateCMDSTestReq> createTestForm = Form.form(CreateCMDSTestReq.class)
                .bindFromRequest();
        Logger.debug("request params : " + createTestForm.data());
        if (createTestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(createTestForm))).toObjectNode());
        }
        CreateCMDSTestReq createTestReq = createTestForm.get();
        CreateCMDSTestRes createTestRes = null;
        try {
            createTestRes = CMDSTestManager.createTest(createTestReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(createTestRes).toObjectNode());
    }

    public static Result createTestAuto() {

        Form<CreateCMDSTestAutoReq> createTestForm = Form.form(CreateCMDSTestAutoReq.class)
                .bindFromRequest();
        Logger.debug("request params : " + createTestForm.data());
        if (createTestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(createTestForm))).toObjectNode());
        }
        CreateCMDSTestAutoReq createTestReq = createTestForm.get();
        CreateCMDSTestRes createTestRes = null;
        try {
            createTestRes = CMDSTestManager.createTestAuto(createTestReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(createTestRes).toObjectNode());
    }

    public static Result addQuestion() {

        Form<ModifyCMDSTestQuestionsReq> addQuestionToTestForm = Form.form(
                ModifyCMDSTestQuestionsReq.class).bindFromRequest();
        if (addQuestionToTestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(addQuestionToTestForm))).toObjectNode());
        }
        ModifyCMDSTestQuestionsReq addQuestionToTestReq = addQuestionToTestForm.get();
        ModifyCMDSTestQuestionsRes addQuestionToTestRes = null;
        try {
            addQuestionToTestRes = CMDSTestManager.addQuestion(addQuestionToTestReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(addQuestionToTestRes).toObjectNode());
    }

    public static Result removeQuestion() {

        Form<ModifyCMDSTestQuestionsReq> removeQuestionToTestForm = Form.form(
                ModifyCMDSTestQuestionsReq.class).bindFromRequest();
        if (removeQuestionToTestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(removeQuestionToTestForm))).toObjectNode());
        }
        ModifyCMDSTestQuestionsReq removeQuestionToTestReq = removeQuestionToTestForm.get();
        ModifyCMDSTestQuestionsRes removeQuestionToTestRes = null;
        try {
            removeQuestionToTestRes = CMDSTestManager.removeQuestion(removeQuestionToTestReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(removeQuestionToTestRes).toObjectNode());
    }

    public static Result finishTestEditing() {

        Form<FinishCMDSTestEditReq> finishTestEditingForm = Form.form(FinishCMDSTestEditReq.class)
                .bindFromRequest();
        if (finishTestEditingForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(finishTestEditingForm))).toObjectNode());
        }
        FinishCMDSTestEditReq finishTestEditingReq = finishTestEditingForm.get();
        FinishCMDSTestEditRes finishTestEditingRes = null;
        try {
            finishTestEditingRes = CMDSTestManager.finishTestEditing(finishTestEditingReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(finishTestEditingRes).toObjectNode());
    }

    public static Result getTestInfo() {

        Form<GetCMDSTestReq> getTestForm = Form.form(GetCMDSTestReq.class).bindFromRequest();
        if (getTestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getTestForm))).toObjectNode());
        }
        GetCMDSTestReq getTestReq = getTestForm.get();
        GetCMDSTestRes getTestRes = null;
        try {
            getTestRes = CMDSTestManager.getTestInfo(getTestReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getTestRes).toObjectNode());
    }

    public static Result regenerateAnalytics() {

        Form<GetCMDSTestReq> getTestForm = Form.form(GetCMDSTestReq.class).bindFromRequest();
        if (getTestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getTestForm))).toObjectNode());
        }
        GetCMDSTestReq getTestReq = getTestForm.get();
        GetReGenerateAnalyticsRes getReGenerateAnalyticsRes = null;
        try {
            getReGenerateAnalyticsRes = CMDSTestManager.regenerateAnalytics(getTestReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getReGenerateAnalyticsRes).toObjectNode());
    }

    /**
     *
     * @return the list of questions of the test
     */
    public static Result getTestQuestions() {

        Form<GetCMDSTestQuestionsReq> getTestQuestionsForm = Form.form(
                GetCMDSTestQuestionsReq.class).bindFromRequest();
        if (getTestQuestionsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getTestQuestionsForm))).toObjectNode());
        }
        GetCMDSTestQuestionsReq getTestQuestionsReq = getTestQuestionsForm.get();
        GetCMDSTestQuestionsRes getTestQuestionsRes = null;
        try {
            getTestQuestionsRes = CMDSTestManager.getTestQuestions(getTestQuestionsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getTestQuestionsRes).toObjectNode());
    }

    private static UploadOfflineTestResultReq getUploadOfflineTestResultReq() {

        MultipartFormData body = request().body().asMultipartFormData();
        UploadOfflineTestResultReq uploadTestResultReq = new UploadOfflineTestResultReq(body);
        return uploadTestResultReq;
    }

    public static Result getTests() {

        Form<GetTestsReq> requestForm = Form.form(GetTestsReq.class).bindFromRequest();
        GetCMDSTestsRes response = null;
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GetTestsReq request = requestForm.get();

        try {
            response = CMDSTestManager.getTests(request);
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

        LOGGER.debug(" Called createDirectory");

        EditContentReq request = null;
        EditContentRes response = new EditContentRes();

        try {
            Form<EditContentReq> requestForm = Form.form(EditContentReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                                getErrorMessege(requestForm))).toObjectNode());
            }
            request = requestForm.get();
            response.isUpdated = CMDSTestManager.INSTANCE.update(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result updateTestResultVisibility() {

        Form<UpdateTestResultVisibilityReq> requestForm = Form.form(
                UpdateTestResultVisibilityReq.class).bindFromRequest();

        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        UpdateTestResultVisibilityReq request = requestForm.get();

        UpdateTestResultVisibilityRes response = null;

        try {
            response = CMDSTestManager.INSTANCE.updateTestResultVisibility(request);
        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result setPasswordForTest() {
        Form<SetPasswordForTestReq> requestForm = Form.form(SetPasswordForTestReq.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        SetPasswordForTestReq request = requestForm.get();
        SetPasswordForTestRes response = null;
        response = CMDSTestManager.INSTANCE.setPasswordForTest(request);
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result simplifyBoardNames() {
        Form<simplifyBoardNamesReq> requestForm = Form.form(simplifyBoardNamesReq.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        simplifyBoardNamesReq request = requestForm.get();
        simplifyBoardNamesRes response = null;
        response = CMDSTestManager.INSTANCE.simplifyBoardNames(request);
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result addSimplifiedBoardNames() {
        Form<simplifyBoardNamesReq> requestForm = Form.form(simplifyBoardNamesReq.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        simplifyBoardNamesReq request = requestForm.get();
        simplifyBoardNamesRes response = null;
        response = CMDSTestManager.INSTANCE.addSimplifiedBoardNames(request);
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result removeSimplifiedBoardNames() {
        Form<simplifyBoardNamesReq> requestForm = Form.form(simplifyBoardNamesReq.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        simplifyBoardNamesReq request = requestForm.get();
        simplifyBoardNamesRes response = null;
        try {
            response = CMDSTestManager.INSTANCE.removeSimplifiedBoardNames(request);
        } catch (VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result enableOrDisablePartialMarks(){
        Form<SetPasswordForTestReq> requestForm = Form.form(SetPasswordForTestReq.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        SetPasswordForTestReq request = requestForm.get();
        SetPasswordForTestRes response = null;
        try{
            response = CMDSTestManager.INSTANCE.enableOrDisablePartialMarks(request);
        } catch (VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result enableOrDisableSectionLocking(){
        Form<SetPasswordForTestReq> requestForm = Form.form(SetPasswordForTestReq.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        SetPasswordForTestReq request = requestForm.get();
        SetPasswordForTestRes response = null;
        try{
            response = CMDSTestManager.INSTANCE.enableOrDisableSectionLocking(request);
        } catch (VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }
    public static Result enableAutoResumeTest(){
        Form<SetPasswordForTestReq> requestForm = Form.form(SetPasswordForTestReq.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        SetPasswordForTestReq request = requestForm.get();
        SetPasswordForTestRes response = null;
        try{
            response = CMDSTestManager.INSTANCE.enableAutoResumeTest(request);
        } catch (VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }
}
