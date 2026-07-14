package controllers;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.EntityType;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.content.managers.AnalyticsManager;
import com.vedantu.content.managers.LibraryManager;
import com.vedantu.content.models.analytics.EntityMeasures;
import com.vedantu.content.models.analytics.UserQuestionAnalytics;
import com.vedantu.content.pojos.requests.analytics.EndAttemptReq;
import com.vedantu.content.pojos.requests.analytics.GetAttemptedEntitiesReq;
import com.vedantu.content.pojos.requests.analytics.GetEntityLeaderBoardReq;
import com.vedantu.content.pojos.requests.analytics.GetEntityMarkDistributionReq;
import com.vedantu.content.pojos.requests.analytics.GetEntityMeasuresReq;
import com.vedantu.content.pojos.requests.analytics.GetEntityMeasuresRes;
import com.vedantu.content.pojos.requests.analytics.GetEntityQuestionsAttemptStatReq;
import com.vedantu.content.pojos.requests.analytics.GetEntityResultAnalyticsReq;
import com.vedantu.content.pojos.requests.analytics.GetEntityScheduleAnalyticsReq;
import com.vedantu.content.pojos.requests.analytics.GetQuestionAnalyticsReq;
import com.vedantu.content.pojos.requests.analytics.GetUserAnalyticsStatsReq;
import com.vedantu.content.pojos.requests.analytics.GetUserEntityAnalyticsBySubjectReq;
import com.vedantu.content.pojos.requests.analytics.GetUserEntityAnalyticsReq;
import com.vedantu.content.pojos.requests.analytics.GetUserEntityAttemptStatusInfoReq;
import com.vedantu.content.pojos.requests.analytics.GetUserEntityMeasuresReq;
import com.vedantu.content.pojos.requests.analytics.GetUserEntityQuestionAttemptStatsReq;
import com.vedantu.content.pojos.requests.analytics.GetUserEntityRankReq;
import com.vedantu.content.pojos.requests.analytics.GetUserEntityResultAnalyticsReq;
import com.vedantu.content.pojos.requests.analytics.GradeTestSubjectiveQuestionReq;
import com.vedantu.content.pojos.requests.analytics.RecordAttemptReq;
import com.vedantu.content.pojos.requests.analytics.ResetQuestionAttemptReq;
import com.vedantu.content.pojos.requests.analytics.StartAttemptReq;
import com.vedantu.content.pojos.requests.analytics.SyncTabletAnalyticsReq;
import com.vedantu.content.pojos.requests.schedules.GetEntityScheduleInfoReq;
import com.vedantu.content.pojos.requests.tests.GetTestInfoReq;
import com.vedantu.content.pojos.responses.analytics.EndAttemptRes;
import com.vedantu.content.pojos.responses.analytics.GetAttemptedEntitiesRes;
import com.vedantu.content.pojos.responses.analytics.GetEntityAttemptAnalyticsRes;
import com.vedantu.content.pojos.responses.analytics.GetEntityAttemptsStudentsListRes;
import com.vedantu.content.pojos.responses.analytics.GetEntityMarkDistributionRes;
import com.vedantu.content.pojos.responses.analytics.GetEntityQuestionAttemptInfoListRes;
import com.vedantu.content.pojos.responses.analytics.GetEntityResultAnalyticsRes;
import com.vedantu.content.pojos.responses.analytics.GetEntityScheduleAnalyticsRes;
import com.vedantu.content.pojos.responses.analytics.GetEntityTestStatusRes;
import com.vedantu.content.pojos.responses.analytics.GetQuestionAnalyticsRes;
import com.vedantu.content.pojos.responses.analytics.GetUserAnalyticsStatsRes;
import com.vedantu.content.pojos.responses.analytics.GetUserEntityAnalyticsBySubjectRes;
import com.vedantu.content.pojos.responses.analytics.GetUserEntityAttemptStatusInfoRes;
import com.vedantu.content.pojos.responses.analytics.GetUserEntityMeasuresRes;
import com.vedantu.content.pojos.responses.analytics.GetUserEntityQuestionAttemptInfoListRes;
import com.vedantu.content.pojos.responses.analytics.GetUserEntityQuestionAttemptStatInfoListRes;
import com.vedantu.content.pojos.responses.analytics.GetUserEntityRankRes;
import com.vedantu.content.pojos.responses.analytics.GetUserEntityResultAnalyticsListRes;
import com.vedantu.content.pojos.responses.analytics.GetUserEntityResultAnalyticsSingleEntityRes;
import com.vedantu.content.pojos.responses.analytics.GradeTestSubjectiveQuestionRes;
import com.vedantu.content.pojos.responses.analytics.RecordAttemptRes;
import com.vedantu.content.pojos.responses.analytics.ResetQuestionAttemptRes;
import com.vedantu.content.pojos.responses.analytics.StartAttemptRes;
import com.vedantu.content.pojos.responses.analytics.SyncTabletAnalyticsRes;
import com.vedantu.content.pojos.responses.tests.GetTestInfoRes;
import com.vedantu.content.pojos.tests.EntityScheduleInfo;
import com.vedantu.user.managers.UserManager;
import com.vedantu.user.models.User;
import com.vedantu.user.pojos.responses.GetUserSelfFullProfileRes;

public class Analytics extends AbstractVedantuController {

    public static Result startAttempt() {

        Form<StartAttemptReq> startAttemptForm = Form.form(StartAttemptReq.class).bindFromRequest();
        if (startAttemptForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(startAttemptForm))).toObjectNode());
        }
        StartAttemptReq startAttemptReq = startAttemptForm.get();
        if (ObjectIdUtils.hasInvalidId(startAttemptReq.entityId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        StartAttemptRes startAttemptRes = null;
        try {
            startAttemptRes = AnalyticsManager.startAttempt(startAttemptReq, true);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(startAttemptRes).toObjectNode());
    }

    public static Result endAttempt() {

        Form<EndAttemptReq> endAttemptForm = Form.form(EndAttemptReq.class).bindFromRequest();
        if (endAttemptForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(endAttemptForm))).toObjectNode());
        }
        EndAttemptReq endAttemptReq = endAttemptForm.get();
        if (ObjectIdUtils.hasInvalidId(endAttemptReq.entityId, endAttemptReq.attemptId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        EndAttemptRes endAttemptRes = null;
        try {
            endAttemptRes = AnalyticsManager.endAttempt(endAttemptReq, System.currentTimeMillis());
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(endAttemptRes).toObjectNode());
    }

    public static Result endStudentAttempt() {
        Form<StartAttemptReq> endAttemptForm = Form.form(StartAttemptReq.class).bindFromRequest();
        if (endAttemptForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(endAttemptForm))).toObjectNode());
        }
        StartAttemptReq endAttemptReq = endAttemptForm.get();
        if (ObjectIdUtils.hasInvalidId(endAttemptReq.entityId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        EndAttemptRes endAttemptRes = null;
        try {
            endAttemptRes = AnalyticsManager.endStudentAttempt(endAttemptReq, System.currentTimeMillis());
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(endAttemptRes).toObjectNode());
    }

    public static Result pauseStudentTest() {
        Form<StartAttemptReq> endAttemptForm = Form.form(StartAttemptReq.class).bindFromRequest();
        if (endAttemptForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(endAttemptForm))).toObjectNode());
        }
        StartAttemptReq endAttemptReq = endAttemptForm.get();
        if (ObjectIdUtils.hasInvalidId(endAttemptReq.entityId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        EndAttemptRes endAttemptRes = null;
        try {
            endAttemptRes = AnalyticsManager.pauseStudentAttempt(endAttemptReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(endAttemptRes).toObjectNode());
    }

    public static Result resumeStudentTest() {
        Form<StartAttemptReq> endAttemptForm = Form.form(StartAttemptReq.class).bindFromRequest();
        if (endAttemptForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(endAttemptForm))).toObjectNode());
        }
        StartAttemptReq endAttemptReq = endAttemptForm.get();
        if (ObjectIdUtils.hasInvalidId(endAttemptReq.entityId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        EndAttemptRes endAttemptRes = null;
        try {
            endAttemptRes = AnalyticsManager.resumeStudentAttempt(endAttemptReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(endAttemptRes).toObjectNode());
    }

    public static Result recomputeEntitynalytics() {

        Form<StartAttemptReq> reComputeAnalyticsForm = Form.form(StartAttemptReq.class)
                .bindFromRequest();
        if (reComputeAnalyticsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(reComputeAnalyticsForm))).toObjectNode());
        }
        StartAttemptReq reComputeAnalyticsReq = reComputeAnalyticsForm.get();
        if (ObjectIdUtils.hasInvalidId(reComputeAnalyticsReq.entityId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        Map<String, EntityMeasures> newMeasuresMap = null;
        try {
            newMeasuresMap = AnalyticsManager
                    .reComputeUserEntityAnalyticsData(reComputeAnalyticsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(newMeasuresMap).toObjectNode());
    }

    public static Result recordAttempt() {

        Form<RecordAttemptReq> recordAttemptForm = Form.form(RecordAttemptReq.class)
                .bindFromRequest();
        if (recordAttemptForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(recordAttemptForm))).toObjectNode());
        }
        RecordAttemptReq recordAttemptReq = recordAttemptForm.get();
        if(EntityType.QUESTION == recordAttemptReq.entityType || EntityType.ASSIGNMENT == recordAttemptReq.entityType){
            if (ObjectIdUtils.hasInvalidId(recordAttemptReq.entityId,recordAttemptReq.qId)) {
                return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                        .toObjectNode());
            }
        }else{
            if (ObjectIdUtils.hasInvalidId(recordAttemptReq.entityId, recordAttemptReq.attemptId,
                    recordAttemptReq.qId)) {
                return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                        .toObjectNode());
            }
        }
        RecordAttemptRes recordAttemptRes = null;
        try {
            recordAttemptRes = AnalyticsManager.recordAttempt(recordAttemptReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(recordAttemptRes).toObjectNode());
    }

    public static Result testStatus() {
        Form<StartAttemptReq> testStatusForm = Form.form(StartAttemptReq.class)
                .bindFromRequest();
        if (testStatusForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(testStatusForm))).toObjectNode());
        }
        StartAttemptReq recordAttemptReq = testStatusForm.get();
        RecordAttemptRes recordAttemptRes = null;
        try {
            recordAttemptRes = AnalyticsManager.testStatus(recordAttemptReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(recordAttemptRes).toObjectNode());
    }

	public static Result gradeTestSubjectiveQuestion() {
		Form<GradeTestSubjectiveQuestionReq> GradeTestSubjectiveQuestionReqForm = Form
				.form(GradeTestSubjectiveQuestionReq.class).bindFromRequest();
		if (GradeTestSubjectiveQuestionReqForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(GradeTestSubjectiveQuestionReqForm)))
					.toObjectNode());
		}
		GradeTestSubjectiveQuestionReq gradeTestSubjectiveQuestionReq = GradeTestSubjectiveQuestionReqForm
				.get();
		GradeTestSubjectiveQuestionRes gradeTestSubjectiveQuestionRes = null;
		try {
			gradeTestSubjectiveQuestionRes = AnalyticsManager
					.gradeTestSubjectiveQuestion(gradeTestSubjectiveQuestionReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(gradeTestSubjectiveQuestionRes)
				.toObjectNode());
	}

    public static Result _testStatus() {
        Form<GetTestInfoReq> testStatusForm = Form.form(GetTestInfoReq.class)
                .bindFromRequest();
        if (testStatusForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(testStatusForm))).toObjectNode());
        }
        GetTestInfoReq recordAttemptReq = testStatusForm.get();
        GetTestInfoRes recordAttemptRes = null;
        recordAttemptRes = AnalyticsManager._testStatus(recordAttemptReq);
        return ok(getResultResponse(recordAttemptRes).toObjectNode());
    }

    public static Result resetQuestionAttempt() {

        Form<ResetQuestionAttemptReq> getResetQuestionAttemptForm = Form.form(
                ResetQuestionAttemptReq.class).bindFromRequest();
        if (getResetQuestionAttemptForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getResetQuestionAttemptForm))).toObjectNode());
        }
        ResetQuestionAttemptReq resetQuestionAttemptReq = getResetQuestionAttemptForm.get();
        ResetQuestionAttemptRes resetQuestionAttemptRes = null;
        try {
            resetQuestionAttemptRes = AnalyticsManager
                    .resetQuestionAttempt(resetQuestionAttemptReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(resetQuestionAttemptRes).toObjectNode());
    }

    public static Result getQuestionAnalytics() {

        Form<GetQuestionAnalyticsReq> getQuestionAnalyticsForm = Form.form(
                GetQuestionAnalyticsReq.class).bindFromRequest();
        if (getQuestionAnalyticsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getQuestionAnalyticsForm))).toObjectNode());
        }
        GetQuestionAnalyticsReq getQuestionAnalyticsReq = getQuestionAnalyticsForm.get();
        if (ObjectIdUtils.hasInvalidId(getQuestionAnalyticsReq.entityId,
                getQuestionAnalyticsReq.qId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        GetQuestionAnalyticsRes getQuestionAnalyticsRes = null;
        try {
            getQuestionAnalyticsRes = AnalyticsManager
                    .getQuestionAnalytics(getQuestionAnalyticsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getQuestionAnalyticsRes).toObjectNode());
    }

    public static Result getEntityResultAnalytics() {

        Form<GetEntityResultAnalyticsReq> getResultAnalyticsForm = Form.form(
                GetEntityResultAnalyticsReq.class).bindFromRequest();
        if (getResultAnalyticsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getResultAnalyticsForm))).toObjectNode());
        }
        GetEntityResultAnalyticsReq getAnalyticsResultReq = getResultAnalyticsForm.get();

        GetEntityResultAnalyticsRes getAnalyticsResultRes = null;
        try {
            getAnalyticsResultRes = AnalyticsManager.getEntityResultAnalytics(
                    getAnalyticsResultReq, true);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getAnalyticsResultRes).toObjectNode());
    }

    public static Result getStudentsListFromEntityAttempts() {

        Form<GetEntityResultAnalyticsReq> getResultAnalyticsForm = Form.form(
                GetEntityResultAnalyticsReq.class).bindFromRequest();
        if (getResultAnalyticsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getResultAnalyticsForm))).toObjectNode());
        }
        GetEntityResultAnalyticsReq getAnalyticsResultReq = getResultAnalyticsForm.get();

        GetEntityAttemptAnalyticsRes getAnalyticsResultRes = null;
        try {
            getAnalyticsResultRes = AnalyticsManager.getEntityAttemtAnalytics(
                    getAnalyticsResultReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getAnalyticsResultRes).toObjectNode());
    }

    public static Result resetStudentTest() {
        Form<GetEntityResultAnalyticsReq> getResultAnalyticsForm = Form.form(
                GetEntityResultAnalyticsReq.class).bindFromRequest();
        if (getResultAnalyticsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getResultAnalyticsForm))).toObjectNode());
        }
        GetEntityResultAnalyticsReq getAnalyticsResultReq = getResultAnalyticsForm.get();

        ResetQuestionAttemptRes resetQuestionAttemptRes = new ResetQuestionAttemptRes(false,0);
        try {
            AnalyticsManager.resetStudentTest(
                    getAnalyticsResultReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(resetQuestionAttemptRes).toObjectNode());
    }
    public static Result regenerateStudentTestAnalytics() {
        Form<GetEntityResultAnalyticsReq> getResultAnalyticsForm = Form.form(
                GetEntityResultAnalyticsReq.class).bindFromRequest();
        if (getResultAnalyticsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getResultAnalyticsForm))).toObjectNode());
        }
        GetEntityResultAnalyticsReq getAnalyticsResultReq = getResultAnalyticsForm.get();

        ResetQuestionAttemptRes resetQuestionAttemptRes = new ResetQuestionAttemptRes(false,0);
        try {
            AnalyticsManager.regenerateStudentTestAnalytics(
                    getAnalyticsResultReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(resetQuestionAttemptRes).toObjectNode());
    }

    public static Result getUserEntityAnalyticsBySubject() {
        Form<GetUserEntityAnalyticsBySubjectReq> getEntityLeaderBoardForm = Form.form(
                GetUserEntityAnalyticsBySubjectReq.class).bindFromRequest();
        if (getEntityLeaderBoardForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getEntityLeaderBoardForm))).toObjectNode());
        }
        GetUserEntityAnalyticsBySubjectReq getEntityLeaderBoardReq = getEntityLeaderBoardForm.get();

        GetUserEntityAnalyticsBySubjectRes getEntityLeaderBoardRes = null;
        getEntityLeaderBoardRes = AnalyticsManager
                .getUserEntityAnalyticsBySubject(getEntityLeaderBoardReq);
        return ok(getResultResponse(getEntityLeaderBoardRes).toObjectNode());
    }

    public static Result getEntityLeaderBoard() {

        Form<GetEntityLeaderBoardReq> getEntityLeaderBoardForm = Form.form(
                GetEntityLeaderBoardReq.class).bindFromRequest();
        if (getEntityLeaderBoardForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getEntityLeaderBoardForm))).toObjectNode());
        }
        GetEntityLeaderBoardReq getEntityLeaderBoardReq = getEntityLeaderBoardForm.get();

        GetEntityResultAnalyticsRes getEntityLeaderBoardRes = null;
        try {
            getEntityLeaderBoardRes = AnalyticsManager.getEntityResultAnalytics(
                    getEntityLeaderBoardReq, !getEntityLeaderBoardReq.miniInfo);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getEntityLeaderBoardRes).toObjectNode());
    }

    public static Result getUserEntityResultAnalytics() {

        Form<GetUserEntityResultAnalyticsReq> getUserResultAnalyticsForm = Form.form(
                GetUserEntityResultAnalyticsReq.class).bindFromRequest();
        if (getUserResultAnalyticsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getUserResultAnalyticsForm))).toObjectNode());
        }
        GetUserEntityResultAnalyticsReq getUserAnalyticsResultReq = getUserResultAnalyticsForm
                .get();

        GetUserEntityResultAnalyticsListRes getAnalyticsResultRes = null;
        try {
            getAnalyticsResultRes = AnalyticsManager
                    .getUserEntityResultAnalytics(getUserAnalyticsResultReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getAnalyticsResultRes).toObjectNode());
    }

    // this will return the details analytics of the user in a TEST{ENTITY}
    public static Result getUserEntityAnalytics() {

        // TODO: make some validation check, i.e member should not be allowed to
        // view other member profile
        Form<GetUserEntityAnalyticsReq> getUserEntityAnalyticsForm = Form.form(
                GetUserEntityAnalyticsReq.class).bindFromRequest();
        if (getUserEntityAnalyticsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getUserEntityAnalyticsForm))).toObjectNode());
        }

        GetUserEntityAnalyticsReq getUserEntityAnalyticsReq = getUserEntityAnalyticsForm.get();

        GetUserEntityResultAnalyticsSingleEntityRes getUserEntityAnalyticsRes = null;
        try {
            getUserEntityAnalyticsRes = AnalyticsManager
                    .getUserEntityAnalytics(getUserEntityAnalyticsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getUserEntityAnalyticsRes).toObjectNode());
    }

    public static Result getUserEntityQuestionAttempts() {

        Form<GetUserEntityQuestionAttemptStatsReq> getUserEntityQusAttemptsForm = Form.form(
                GetUserEntityQuestionAttemptStatsReq.class).bindFromRequest();
        if (getUserEntityQusAttemptsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getUserEntityQusAttemptsForm))).toObjectNode());
        }
        GetUserEntityQuestionAttemptStatsReq getUserEntityQusAttemptsReq = getUserEntityQusAttemptsForm
                .get();
        GetUserEntityQuestionAttemptInfoListRes getUserEntityQusAttamptsRes = null;
        try {
            getUserEntityQusAttamptsRes = AnalyticsManager
                    .getUserEntityQuestionAttemptInfos(getUserEntityQusAttemptsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getUserEntityQusAttamptsRes).toObjectNode());

    }

    public static Result getEntityQuestionAttempts() {

        Form<GetEntityQuestionsAttemptStatReq> getEntityQusAttemptInfoForm = Form.form(
                GetEntityQuestionsAttemptStatReq.class).bindFromRequest();
        if (getEntityQusAttemptInfoForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getEntityQusAttemptInfoForm))).toObjectNode());
        }
        GetEntityQuestionsAttemptStatReq getUserEntityQusAttemptsReq = getEntityQusAttemptInfoForm
                .get();
        GetEntityQuestionAttemptInfoListRes getUserEntityQuestionAttamptsRes = null;
        try {
            getUserEntityQuestionAttamptsRes = AnalyticsManager
                    .getEntityQusAttemptInfoDetails(getUserEntityQusAttemptsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getUserEntityQuestionAttamptsRes).toObjectNode());
    }

    public static Result getStudentsQuestionsAnsweredList(){
        Form<GetQuestionAnalyticsReq> getEntityQusAttemptInfoForm = Form.form(
                GetQuestionAnalyticsReq.class).bindFromRequest();
        if (getEntityQusAttemptInfoForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getEntityQusAttemptInfoForm))).toObjectNode());
        }
        GetQuestionAnalyticsReq getQuestionCorrectWrongReq = getEntityQusAttemptInfoForm.get();
        List<UserQuestionAnalytics> getQuestionCorrectWrongStudentsList = AnalyticsManager
                .getStudentAnalyticsList(getQuestionCorrectWrongReq);
        List<UserQuestionAnalytics> finalRes = new ArrayList<UserQuestionAnalytics>();
        for(int i = 0; i < getQuestionCorrectWrongStudentsList.size(); i++){
            UserQuestionAnalytics info = getQuestionCorrectWrongStudentsList.get(i);
            try {
                GetUserSelfFullProfileRes res = UserManager.getUserFullProfile(info.userId);
                info.userName = res.info.firstName+" "+(StringUtils.isEmpty(res.info.lastName) ? "" : res.info.lastName);
                info.userProfilePic = res.info.thumbnail;
                finalRes.add(info);
            } catch (VedantuException e) {
            }
        }

        return ok(getResultResponse(finalRes).toObjectNode());
    }

    public static Result getEntityMarkDistribution() {

        Form<GetEntityMarkDistributionReq> getEntityMarkDistributionForm = Form.form(
                GetEntityMarkDistributionReq.class).bindFromRequest();
        if (getEntityMarkDistributionForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getEntityMarkDistributionForm))).toObjectNode());
        }
        GetEntityMarkDistributionReq getEntityMarkDistributionReq = getEntityMarkDistributionForm
                .get();
        GetEntityMarkDistributionRes getEntityMarkDistributionRes = null;
        try {
            getEntityMarkDistributionRes = AnalyticsManager
                    .getEntityMarkDistribution(getEntityMarkDistributionReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getEntityMarkDistributionRes).toObjectNode());
    }

    public static Result getEntityScheduleAnalytics() {

        Form<GetEntityScheduleAnalyticsReq> getEntityScheduleAnalyticsForm = Form.form(
                GetEntityScheduleAnalyticsReq.class).bindFromRequest();
        if (getEntityScheduleAnalyticsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getEntityScheduleAnalyticsForm))).toObjectNode());
        }
        GetEntityScheduleAnalyticsReq getEntityScheduleAnalytics = getEntityScheduleAnalyticsForm
                .get();
        GetEntityScheduleAnalyticsRes getEntityAnalyticsScheduleRes = null;
        try {
            getEntityAnalyticsScheduleRes = AnalyticsManager
                    .getEntityAnalyticsSchedule(getEntityScheduleAnalytics);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getEntityAnalyticsScheduleRes).toObjectNode());
    }

    public static Result getEntityScheduleInfo() {

        Form<GetEntityScheduleInfoReq> getEntityScheduleForm = Form.form(
                GetEntityScheduleInfoReq.class).bindFromRequest();
        if (getEntityScheduleForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getEntityScheduleForm))).toObjectNode());
        }
        GetEntityScheduleInfoReq getEntityScheduleReq = getEntityScheduleForm.get();
        ListResponse<EntityScheduleInfo> getEntityScheduleRes = null;
        try {
            getEntityScheduleRes = LibraryManager.getEntityScheduleInfoRes(
                    getEntityScheduleReq.orgId, getEntityScheduleReq.entity.id);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getEntityScheduleRes).toObjectNode());
    }

    public static Result getUserAnalyticsStats() {

        Form<GetUserAnalyticsStatsReq> getUserAnalyticsStatsForm = Form.form(
                GetUserAnalyticsStatsReq.class).bindFromRequest();
        if (getUserAnalyticsStatsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getUserAnalyticsStatsForm))).toObjectNode());
        }
        GetUserAnalyticsStatsReq getUserAnalyticsStatsReq = getUserAnalyticsStatsForm.get();
        GetUserAnalyticsStatsRes getUserAnalyticsStatsRes = null;
        try {
            getUserAnalyticsStatsRes = AnalyticsManager
                    .getUserAnalyticsStats(getUserAnalyticsStatsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getUserAnalyticsStatsRes).toObjectNode());
    }

    public static Result getUserEntityAttemptStatusInfo() {

        Form<GetUserEntityAttemptStatusInfoReq> getUserEntityAttemptStatusInfoForm = Form.form(
                GetUserEntityAttemptStatusInfoReq.class).bindFromRequest();
        if (getUserEntityAttemptStatusInfoForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getUserEntityAttemptStatusInfoForm))).toObjectNode());
        }
        GetUserEntityAttemptStatusInfoReq getUserEntityStatusInfoReq = getUserEntityAttemptStatusInfoForm
                .get();
        GetUserEntityAttemptStatusInfoRes getUserAttemptStatusInfoRes = null;
        try {
            getUserAttemptStatusInfoRes = AnalyticsManager
                    .getUserEntityAttemptStatusInfo(getUserEntityStatusInfoReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getUserAttemptStatusInfoRes).toObjectNode());
    }

    public static Result getUserEntityQuestionsAttemptStatInfo() {

        Form<GetEntityQuestionsAttemptStatReq> getUserEntityQusAttemptStatInfoForm = Form.form(
                GetEntityQuestionsAttemptStatReq.class).bindFromRequest();
        if (getUserEntityQusAttemptStatInfoForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getUserEntityQusAttemptStatInfoForm))).toObjectNode());
        }

        GetEntityQuestionsAttemptStatReq getUserEntityQuestionStatsInfoReq = getUserEntityQusAttemptStatInfoForm
                .get();
        GetUserEntityQuestionAttemptStatInfoListRes getUserEntityQuestionAttemptStatsInfoRes = null;
        try {
            getUserEntityQuestionAttemptStatsInfoRes = AnalyticsManager
                    .getUserEntityQuestionsAttemptInfoStat(getUserEntityQuestionStatsInfoReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getUserEntityQuestionAttemptStatsInfoRes).toObjectNode());
    }

    public static Result getEntityTestStatus() {

        Form<GetUserEntityAnalyticsReq> getEntityTestStatusForm = Form.form(
                GetUserEntityAnalyticsReq.class).bindFromRequest();
        if (getEntityTestStatusForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getEntityTestStatusForm))).toObjectNode());
        }

        GetUserEntityAnalyticsReq getUserEntityQuestionStatsInfoReq = getEntityTestStatusForm
                .get();
        GetEntityTestStatusRes getEntityTestStatusRes = null;
        try {
            getEntityTestStatusRes = AnalyticsManager
                    .getEntityTestStatus(getUserEntityQuestionStatsInfoReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getEntityTestStatusRes).toObjectNode());
    }

    public static Result getEntityMeasures() {

        Form<GetEntityMeasuresReq> getEntityMeasuresForm = Form.form(GetEntityMeasuresReq.class)
                .bindFromRequest();
        if (getEntityMeasuresForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getEntityMeasuresForm))).toObjectNode());
        }
        GetEntityMeasuresReq getEntityMeasuresReq = getEntityMeasuresForm.get();
        GetEntityMeasuresRes getEntityMeasuresRes = null;
        try {
            getEntityMeasuresRes = AnalyticsManager.getEntityMeasures(getEntityMeasuresReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getEntityMeasuresRes).toObjectNode());
    }

    public static Result getUserEntityMeasures() {

        Form<GetUserEntityMeasuresReq> getUserEntityMeasuresForm = Form.form(
                GetUserEntityMeasuresReq.class).bindFromRequest();
        if (getUserEntityMeasuresForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getUserEntityMeasuresForm))).toObjectNode());
        }
        GetUserEntityMeasuresReq getUserEntityMeasuresReq = getUserEntityMeasuresForm.get();
        GetUserEntityMeasuresRes getUserEntityMeasuresRes = null;
        try {
            getUserEntityMeasuresRes = AnalyticsManager
                    .getUserEntityMeasures(getUserEntityMeasuresReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getUserEntityMeasuresRes).toObjectNode());
    }

    public static Result syncTabletAnalytics() {

        Form<SyncTabletAnalyticsReq> syncTabletAnalyticsForm = Form.form(
                SyncTabletAnalyticsReq.class).bindFromRequest();
        if (syncTabletAnalyticsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(syncTabletAnalyticsForm))).toObjectNode());
        }
        SyncTabletAnalyticsReq syncTabletAnalyticsReq = syncTabletAnalyticsForm.get();
        SyncTabletAnalyticsRes syncTabletAnalyticsRes = null;
        try {
            syncTabletAnalyticsRes = AnalyticsManager.syncTabletAnalytics(syncTabletAnalyticsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(syncTabletAnalyticsRes).toObjectNode());
    }

    public static Result getAttemptedEntities() {

        Form<GetAttemptedEntitiesReq> getAttemptedEntitiesForm = Form.form(
                GetAttemptedEntitiesReq.class).bindFromRequest();
        if (getAttemptedEntitiesForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getAttemptedEntitiesForm))).toObjectNode());
        }
        GetAttemptedEntitiesReq getAttemptedEntitiesReq = getAttemptedEntitiesForm.get();
        GetAttemptedEntitiesRes getAttemptedEntitiesRes = null;
        try {
            getAttemptedEntitiesRes = AnalyticsManager
                    .getAttemptedEntities(getAttemptedEntitiesReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getAttemptedEntitiesRes).toObjectNode());
    }

    public static Result getUserEntityRank() {

        Form<GetUserEntityRankReq> getUserEntityRankForm = Form.form(GetUserEntityRankReq.class)
                .bindFromRequest();
        if (getUserEntityRankForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getUserEntityRankForm))).toObjectNode());
        }
        GetUserEntityRankReq getUserEntityRankReq = getUserEntityRankForm.get();
        GetUserEntityRankRes getUserEntityRankRes = null;
        try {
            getUserEntityRankRes = AnalyticsManager.getUserEntityRank(getUserEntityRankReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getUserEntityRankRes).toObjectNode());
    }

}
