package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.content.managers.TestManager;
import com.vedantu.content.pojos.requests.tests.GetSubjectiveQuestionUserAttemptsReq;
import com.vedantu.content.pojos.requests.tests.UpdateMarksStatusReq;
import com.vedantu.content.pojos.responses.tests.UpdateMarksStatusRes;
import com.vedantu.content.pojos.requests.tests.GetTestDetailsReq;
import com.vedantu.content.pojos.requests.tests.GetTestInfoReq;
import com.vedantu.content.pojos.requests.tests.GetTestsReq;
import com.vedantu.content.pojos.responses.tests.GetSubjectiveQuestionUserAttemptsRes;
import com.vedantu.content.pojos.responses.tests.GetTestInfoRes;
import com.vedantu.content.pojos.responses.tests.GetTestQuestionsRes;
import com.vedantu.content.pojos.responses.tests.GetTestRes;

public class Tests extends AbstractVedantuController {

	private static ALogger LOGGER = Logger.of(Tests.class);
	public static Result getTestInfo() {
		Form<GetTestInfoReq> getTestForm = Form.form(GetTestInfoReq.class)
				.bindFromRequest();
		if (getTestForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(getTestForm))).toObjectNode());
		}
		GetTestInfoReq getTestReq = getTestForm.get();
		GetTestInfoRes getTestRes = null;
		try {
			getTestRes = TestManager.getTestInfo(getTestReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		LOGGER.info("inside getTestInfo() : "+getResultResponse(getTestRes).toObjectNode());
		return ok(getResultResponse(getTestRes).toObjectNode());
	}

	/**
	 * @return board wise questions for the test
	 */
	public static Result getTestQuestions() {
		Form<GetTestDetailsReq> getTestDetailForm = Form.form(
				GetTestDetailsReq.class).bindFromRequest();
		if (getTestDetailForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(getTestDetailForm))).toObjectNode());
		}
		GetTestDetailsReq getTestDetailReq = getTestDetailForm.get();
		GetTestQuestionsRes getTestDetailRes = null;
		try {
			getTestDetailRes = TestManager.getTestQuestions(getTestDetailReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(getTestDetailRes).toObjectNode());
	}

	public static Result getTestSubjectiveQuestions() {
		Form<GetTestDetailsReq> getTestSubjectiveDetailForm = Form.form(
				GetTestDetailsReq.class).bindFromRequest();
		if (getTestSubjectiveDetailForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(getTestSubjectiveDetailForm))).toObjectNode());
		}
		GetTestDetailsReq getTestSubjectiveDetailReq = getTestSubjectiveDetailForm.get();
		GetTestQuestionsRes getTestSubjectiveDetailRes = null;
		try {
			getTestSubjectiveDetailRes = TestManager.getTestSubjectiveQuestions(getTestSubjectiveDetailReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(getTestSubjectiveDetailRes).toObjectNode());
	}

	public static Result getSubjectiveQuestionUserAttempts(){
		Form<GetSubjectiveQuestionUserAttemptsReq> getSubjectiveQuestionUserAttemptsForm = Form.form(
				GetSubjectiveQuestionUserAttemptsReq.class).bindFromRequest();
		if (getSubjectiveQuestionUserAttemptsForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(getSubjectiveQuestionUserAttemptsForm))).toObjectNode());
		}
		GetSubjectiveQuestionUserAttemptsReq getSubjectiveTestDetailReq = getSubjectiveQuestionUserAttemptsForm.get();
		GetSubjectiveQuestionUserAttemptsRes getSubjectiveTestDetailRes = null;
		try {
			getSubjectiveTestDetailRes = TestManager.getSubjectiveQuestionUserAttempts(getSubjectiveTestDetailReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(getSubjectiveTestDetailRes).toObjectNode());
	}

	public static Result getTests() {
		Form<GetTestsReq> getTestsForm = Form.form(GetTestsReq.class)
				.bindFromRequest();
		if (getTestsForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(getTestsForm))).toObjectNode());
		}
		GetTestsReq getTestsReq = getTestsForm.get();
		SearchListResponse<GetTestRes> getTestsRes = null;
		try {
			getTestsRes = TestManager.getTests(getTestsReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(getTestsRes).toObjectNode());
	}

	public static Result updateMarksStatus() {

		Form<UpdateMarksStatusReq> getAddBonusReqForm = Form.form(
				UpdateMarksStatusReq.class).bindFromRequest();
		if (getAddBonusReqForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(getAddBonusReqForm)))
					.toObjectNode());
		}
		UpdateMarksStatusReq getAddBonusReq = getAddBonusReqForm.get();
		UpdateMarksStatusRes updateMarksStatusRes = null;
		try {
			updateMarksStatusRes = TestManager
					.updateMarksStatus(getAddBonusReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}

		return ok(getResultResponse(updateMarksStatusRes).toObjectNode());

	}
}
