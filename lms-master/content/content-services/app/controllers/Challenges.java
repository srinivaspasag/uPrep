package controllers;

import play.Logger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.content.managers.ChallengeManager;
import com.vedantu.content.pojos.requests.challenges.AddChallengeReq;
import com.vedantu.content.pojos.requests.challenges.AttemptChallengeReq;
import com.vedantu.content.pojos.requests.challenges.GetChallengeGlobalLeaderBoardReq;
import com.vedantu.content.pojos.requests.challenges.GetChallengeHintReq;
import com.vedantu.content.pojos.requests.challenges.GetChallengeLeaderBoardReq;
import com.vedantu.content.pojos.requests.challenges.GetChallengeReq;
import com.vedantu.content.pojos.requests.challenges.GetChallengeStatsReq;
import com.vedantu.content.pojos.requests.challenges.GetChallengeUserInfoReq;
import com.vedantu.content.pojos.requests.challenges.GetChallengesReq;
import com.vedantu.content.pojos.responses.challenges.AddChallengeRes;
import com.vedantu.content.pojos.responses.challenges.AttempteChallengeRes;
import com.vedantu.content.pojos.responses.challenges.GetChallengeAttemptInfoRes;
import com.vedantu.content.pojos.responses.challenges.GetChallengeDetailsRes;
import com.vedantu.content.pojos.responses.challenges.GetChallengeGlobalLeaderBoardRes;
import com.vedantu.content.pojos.responses.challenges.GetChallengeHitRes;
import com.vedantu.content.pojos.responses.challenges.GetChallengeLeaderBoardRes;
import com.vedantu.content.pojos.responses.challenges.GetChallengeRes;
import com.vedantu.content.pojos.responses.challenges.GetChallengeStatsRes;
import com.vedantu.content.pojos.responses.challenges.GetChallengeUserInfoRes;

public class Challenges extends AbstractVedantuController {

	public static Result addChallenge() {

		Form<AddChallengeReq> addChallForm = Form.form(AddChallengeReq.class)
				.bindFromRequest();
		if (addChallForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(addChallForm))).toObjectNode());
		}
		AddChallengeReq addChallReq = addChallForm.get();
		AddChallengeRes addChallRes = null;
		try {
			addChallRes = ChallengeManager.addChallenge(addChallReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(addChallRes).toObjectNode());
	}

	public static Result getChallengeInfo() {
		Form<GetChallengeReq> getChallForm = Form.form(GetChallengeReq.class)
				.bindFromRequest();
		if (getChallForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(getChallForm))).toObjectNode());
		}
		GetChallengeReq getChallReq = getChallForm.get();
		GetChallengeRes getChallRes = null;
		try {
			getChallRes = ChallengeManager.getChallenge(getChallReq);
			// we don't want to show the ui what all entities are added in this
			// challenge
			getChallRes.entities = null;
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(getChallRes).toObjectNode());
	}

	public static Result getChallengeDetails() {
		Form<GetChallengeReq> getChallForm = Form.form(GetChallengeReq.class)
				.bindFromRequest();
		if (getChallForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(getChallForm))).toObjectNode());
		}
		GetChallengeReq getChallReq = getChallForm.get();
		GetChallengeDetailsRes getChallDetailsRes = null;
		try {
			getChallDetailsRes = ChallengeManager
					.getChallengeDetails(getChallReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(getChallDetailsRes).toObjectNode());
	}

	public static Result getHint() {

		Form<GetChallengeHintReq> getHintForm = Form.form(
				GetChallengeHintReq.class).bindFromRequest();
		if (getHintForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(getHintForm))).toObjectNode());
		}
		GetChallengeHintReq getHintReq = getHintForm.get();
		GetChallengeHitRes getChallHintRes = null;
		try {
			getChallHintRes = ChallengeManager.getChallengeHint(getHintReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(getChallHintRes).toObjectNode());
	}

	public static Result attemptChallenge() {

		Form<AttemptChallengeReq> attemptChallForm = Form.form(
				AttemptChallengeReq.class).bindFromRequest();
		if (attemptChallForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(attemptChallForm))).toObjectNode());
		}
		AttemptChallengeReq attemptChallReq = attemptChallForm.get();
		AttempteChallengeRes attemptChallRes = null;
		try {
			attemptChallRes = ChallengeManager
					.attemptChallenge(attemptChallReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(attemptChallRes).toObjectNode());
	}

	public static Result getChallengeStats() {

		Form<GetChallengeStatsReq> getChallStatsForm = Form.form(
				GetChallengeStatsReq.class).bindFromRequest();
		if (getChallStatsForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(getChallStatsForm))).toObjectNode());
		}
		GetChallengeStatsReq getChallStatsReq = getChallStatsForm.get();
		GetChallengeStatsRes getChallStatsRes = null;
		try {
			getChallStatsRes = ChallengeManager
					.getChallengeStats(getChallStatsReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(getChallStatsRes).toObjectNode());
	}

	/**
	 * for now challenges are orderBy--challenge creation time, not the time it
	 * was added in a channel
	 **/
	public static Result getChallenges() {

		Form<GetChallengesReq> getChallengsForm = Form.form(
				GetChallengesReq.class).bindFromRequest();
		Logger.debug("request params: " + getChallengsForm.data());
		if (getChallengsForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(getChallengsForm))).toObjectNode());
		}
		GetChallengesReq getChallengesReq = getChallengsForm.get();
		SearchListResponse<GetChallengeRes> getChallengesRes = null;
		try {
			getChallengesRes = ChallengeManager
					.getChallenges(getChallengesReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(getChallengesRes).toObjectNode());
	}

	public static Result getChallengeUserAttemptInfo() {
		Form<GetChallengeReq> getChallUserAttemptForm = Form.form(
				GetChallengeReq.class).bindFromRequest();
		if (getChallUserAttemptForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(getChallUserAttemptForm)))
					.toObjectNode());
		}
		GetChallengeReq getChallengeAttemptInfoReq = getChallUserAttemptForm
				.get();
		GetChallengeAttemptInfoRes getChallAttemptInfoRes = null;
		try {
			getChallAttemptInfoRes = ChallengeManager
					.getChallengeAttemptInfo(getChallengeAttemptInfoReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(getChallAttemptInfoRes).toObjectNode());
	}

	public static Result getUserChallengeInfo() {
		Form<GetChallengeUserInfoReq> getChallUserInfoForm = Form.form(
				GetChallengeUserInfoReq.class).bindFromRequest();
		if (getChallUserInfoForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(getChallUserInfoForm)))
					.toObjectNode());
		}
		GetChallengeUserInfoReq getChallUserInfoReq = getChallUserInfoForm
				.get();
		GetChallengeUserInfoRes getChallUserInfoRes = null;
		try {
			getChallUserInfoRes = ChallengeManager
					.getChallengeUserInfo(getChallUserInfoReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(getChallUserInfoRes).toObjectNode());
	}

	public static Result getChallengeLeaderBoard() {
		Form<GetChallengeLeaderBoardReq> getChallLeaderBoardForm = Form.form(
				GetChallengeLeaderBoardReq.class).bindFromRequest();
		if (getChallLeaderBoardForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(getChallLeaderBoardForm)))
					.toObjectNode());
		}
		GetChallengeLeaderBoardReq getChallLeaderBoardReq = getChallLeaderBoardForm
				.get();
		ListResponse<GetChallengeLeaderBoardRes> getChallLeaderBoardRes = null;
		try {
			getChallLeaderBoardRes = ChallengeManager
					.getChallengeLeaderBoard(getChallLeaderBoardReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(getChallLeaderBoardRes).toObjectNode());
	}

	public static Result getChallengeGlobalLeaderBoard() {
		Form<GetChallengeGlobalLeaderBoardReq> getChallGlobalLeaderBoardForm = Form
				.form(GetChallengeGlobalLeaderBoardReq.class).bindFromRequest();
		if (getChallGlobalLeaderBoardForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(getChallGlobalLeaderBoardForm)))
					.toObjectNode());
		}
		GetChallengeGlobalLeaderBoardReq getChallGlobalLeaderBoardReq = getChallGlobalLeaderBoardForm
				.get();
		ListResponse<GetChallengeGlobalLeaderBoardRes> getChallGlobalLeaderBoardRes = null;
		try {
			getChallGlobalLeaderBoardRes = ChallengeManager
					.getChallengeGlobalLeaderBoard(getChallGlobalLeaderBoardReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(getChallGlobalLeaderBoardRes)
				.toObjectNode());
	}
}
