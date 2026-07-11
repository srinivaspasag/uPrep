package controllers;

import org.json.JSONException;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Http.MultipartFormData;
import play.mvc.Result;

import com.vedantu.cmds.managers.CMDSQuestionSetManager;
import com.vedantu.cmds.pojos.requests.ConfirmQuestionSetUploadNewReq;
import com.vedantu.cmds.pojos.requests.questions.GetQuestionQSReq;
import com.vedantu.cmds.pojos.requests.questions.UploadQuestionSetFileReq;
import com.vedantu.cmds.pojos.responses.questions.ConfirmQuestionSetUploadRes;
import com.vedantu.cmds.pojos.responses.questions.EditContentRes;
import com.vedantu.cmds.pojos.responses.questions.GetCMDSQuestionsRes;
import com.vedantu.cmds.pojos.responses.questions.UploadQuestionSetFileRes;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.content.pojos.requests.EditContentReq;

public class CMDSQuestionSets extends AbstractVedantuController {
	private static ALogger	LOGGER	= Logger.of(CMDSQuestions.class);

	public static Result uploadQuestionFile() {
		LOGGER.debug(" Called uploadingQuestionFile");
		LOGGER.debug(" Called uploadTest");
		MultipartFormData body = request().body().asMultipartFormData();
		UploadQuestionSetFileReq uploadTempFileReq = new UploadQuestionSetFileReq(
				body);
		UploadQuestionSetFileRes response = null;

		try {

			if (uploadTempFileReq.validate() != null) {
				return ok(getErrorResponse(
						new VedantuException(
								VedantuErrorCode.MISSING_PARAMETERS))
						.toObjectNode());
			}
			// uploadTempFileReq = uploadTempFileResForm.get();
			response = CMDSQuestionSetManager.INSTANCE
					.uploadQuestionFile(uploadTempFileReq);

		} catch (VedantuException e) {

			return ok((new JSONResponse(e)).toObjectNode());
		}
		return ok(getResultResponse(response).toObjectNode());

	}

	

	public static Result getQuestions() throws JSONException, Exception {
		LOGGER.debug(" Getting  questionSet questions");
		LOGGER.debug(" Called uploadTest");

		GetQuestionQSReq request = null;

		GetCMDSQuestionsRes response = null;
		Form<GetQuestionQSReq> requestForm = Form.form(GetQuestionQSReq.class)
				.bindFromRequest();

		try {

			if (requestForm.hasErrors()) {
				return ok(getErrorResponse(
						new VedantuException(
								VedantuErrorCode.MISSING_PARAMETERS,
								requestForm.errors().toString()))
						.toObjectNode());
			}

			request = requestForm.get();
			response = CMDSQuestionSetManager.INSTANCE.getQuestions(request);

		} catch (VedantuException e) {

			return ok((new JSONResponse(e)).toObjectNode());
		}
		return ok(getResultResponse(response).toObjectNode());

	}

	public static Result confirmQuestionSetUpload() throws JSONException,
			Exception {
		LOGGER.debug(" Confirming questionSet");
		LOGGER.debug(" Called uploadTest");

		ConfirmQuestionSetUploadNewReq request = null;

		ConfirmQuestionSetUploadRes response = null;
		Form<ConfirmQuestionSetUploadNewReq> requestForm = Form.form(
				ConfirmQuestionSetUploadNewReq.class).bindFromRequest();

		try {

			if (requestForm.hasErrors()) {
				return ok(getErrorResponse(
						new VedantuException(
								VedantuErrorCode.MISSING_PARAMETERS,
								requestForm.errors().toString()))
						.toObjectNode());
			}

			request = requestForm.get();
			response = CMDSQuestionSetManager.INSTANCE.confirmQuestions(
					request.orgId, request.questionIds, request.userId,
					request.filePrefix, request.questionsSetName,
					request.questionSetId, request.folderId,
					request.shouldConfirm);

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
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response.isUpdated = CMDSQuestionSetManager.INSTANCE.update(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

}
