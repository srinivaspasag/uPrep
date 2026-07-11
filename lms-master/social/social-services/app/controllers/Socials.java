package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.enums.UserActionType;
import com.vedantu.content.pojos.requests.SendEmailReq;
import com.vedantu.content.pojos.requests.socials.AddEntityUserActionReq;
import com.vedantu.content.pojos.requests.socials.GetEntityUserActionUsersReq;
import com.vedantu.content.pojos.requests.socials.GetUserFollowingsReq;
import com.vedantu.content.pojos.requests.socials.RemoveEntityUserActionReq;
import com.vedantu.content.pojos.responses.SendEmailRes;
import com.vedantu.content.pojos.responses.socials.EntityUserActionRes;
import com.vedantu.content.pojos.responses.socials.EntityUserActionUsersRes;
import com.vedantu.content.pojos.responses.socials.GetFollowingsRes;
import com.vedantu.social.managers.EntityUserActionManager;

public class Socials extends AbstractVedantuController {

	private static final ALogger LOGGER = Logger.of(Socials.class);

	public static Result view() {
		Form<AddEntityUserActionReq> viewForm = Form.form(
				AddEntityUserActionReq.class).bindFromRequest();
		if (viewForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(viewForm))).toObjectNode());
		}
		AddEntityUserActionReq viewReq = viewForm.get();
		EntityUserActionRes viewRes = null;
		try {
			viewRes = EntityUserActionManager.addEntityUserAction(viewReq,
					UserActionType.VIEWED, true);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(viewRes).toObjectNode());
	}

	public static Result completed() {
        Form<AddEntityUserActionReq> viewForm = Form.form(
                AddEntityUserActionReq.class).bindFromRequest();
        if (viewForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(viewForm))).toObjectNode());
        }
        AddEntityUserActionReq viewReq = viewForm.get();
        EntityUserActionRes viewRes = null;
        try {
            viewRes = EntityUserActionManager.addEntityUserAction(viewReq,
                    UserActionType.COMPLETED, true);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(viewRes).toObjectNode());
    }


	public static Result upVote() {
		Form<AddEntityUserActionReq> upVoteForm = Form.form(
				AddEntityUserActionReq.class).bindFromRequest();
		if (upVoteForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(upVoteForm))).toObjectNode());
		}
		AddEntityUserActionReq upVoteReq = upVoteForm.get();
		EntityUserActionRes upVoteRes = null;
		try {
			upVoteRes = EntityUserActionManager.addEntityUserAction(upVoteReq,
					UserActionType.VOTED);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(upVoteRes).toObjectNode());
	}

	public static Result follow() {
		Form<AddEntityUserActionReq> followForm = Form.form(
				AddEntityUserActionReq.class).bindFromRequest();
		if (followForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(followForm))).toObjectNode());
		}
		AddEntityUserActionReq followReq = followForm.get();
		EntityUserActionRes followRes = null;
		try {
			followRes = EntityUserActionManager.addEntityUserAction(followReq,
					UserActionType.FOLLOWING);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(followRes).toObjectNode());
	}

	public static Result unFollow() {
		Form<RemoveEntityUserActionReq> unFollowForm = Form.form(
				RemoveEntityUserActionReq.class).bindFromRequest();
		if (unFollowForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(unFollowForm))).toObjectNode());
		}
		RemoveEntityUserActionReq unFollowReq = unFollowForm.get();
		EntityUserActionRes unFollowRes = null;
		try {
			unFollowRes = EntityUserActionManager.removeEntityUserAction(
					unFollowReq, UserActionType.FOLLOWING);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(unFollowRes).toObjectNode());
	}

	public static Result getFollowers() {
		Form<GetEntityUserActionUsersReq> getFollowsForm = Form.form(
				GetEntityUserActionUsersReq.class).bindFromRequest();
		if (getFollowsForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(getFollowsForm))).toObjectNode());
		}
		GetEntityUserActionUsersReq getFollowReq = getFollowsForm.get();
		EntityUserActionUsersRes getFollowRes = null;
		try {
			getFollowRes = EntityUserActionManager.getEntityUserAction_Users(
					getFollowReq, UserActionType.FOLLOWING);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(getFollowRes).toObjectNode());
	}

	public static Result getFollowings() {
		Form<GetUserFollowingsReq> getFollowingsForm = Form.form(
				GetUserFollowingsReq.class).bindFromRequest();
		if (getFollowingsForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(getFollowingsForm))).toObjectNode());
		}
		GetUserFollowingsReq getFollowingReq = getFollowingsForm.get();
		GetFollowingsRes getFollowRes = null;
		try {
			getFollowRes = (GetFollowingsRes) EntityUserActionManager
					.getUserFollowings(getFollowingReq);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(getFollowRes).toObjectNode());
	}

	public static Result getVoters() {
		Form<GetEntityUserActionUsersReq> getVotersForm = Form.form(
				GetEntityUserActionUsersReq.class).bindFromRequest();
		if (getVotersForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(getVotersForm))).toObjectNode());
		}
		LOGGER.debug("getVoters req data : " + getVotersForm.data());
		GetEntityUserActionUsersReq getVotersReq = getVotersForm.get();
		EntityUserActionUsersRes getVotersRes = null;
		try {
			getVotersRes = EntityUserActionManager.getEntityUserAction_Users(
					getVotersReq, UserActionType.VOTED);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(getVotersRes).toObjectNode());
	}


//	public static Result getCompleted() {
//        Form<GetCompletedReq> getCompletedReq = Form.form(
//                GetCompletedReq.class).bindFromRequest();
//        if (getCompletedReq.hasErrors()) {
//            return ok(getErrorResponse(
//                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
//                            getErrorMessege(getCompletedReq))).toObjectNode());
//        }
//        LOGGER.debug("getVoters req data : " + getCompletedReq.data());
//        GetCompletedReq getVotersReq = getCompletedReq.get();
//        GetCompletedRes getCompletedRes = null;
//        try {
//            getCompletedRes = EntityUserActionManager.getCompleted(
//                    getVotersReq, UserActionType.VOTED);
//        } catch (VedantuException e) {s
//            return ok(getErrorResponse(e).toObjectNode());
//        }
//        return ok(getResultResponse(getCompletedRes).toObjectNode());
//    }


	public static Result getViewers() {
		Form<GetEntityUserActionUsersReq> getViewersForm = Form.form(
				GetEntityUserActionUsersReq.class).bindFromRequest();
		if (getViewersForm.hasErrors()) {
			return ok(getErrorResponse(
					new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
							getErrorMessege(getViewersForm))).toObjectNode());
		}
		GetEntityUserActionUsersReq getViewersReq = getViewersForm.get();
		EntityUserActionUsersRes getViewersRes = null;
		try {
			getViewersRes = EntityUserActionManager.getEntityUserAction_Users(
					getViewersReq, UserActionType.VIEWED);
		} catch (VedantuException e) {
			return ok(getErrorResponse(e).toObjectNode());
		}
		return ok(getResultResponse(getViewersRes).toObjectNode());
	}

	public static Result getCommonPeople() {
		return TODO;
	}

	public static Result sendEmail(){
	    Form<SendEmailReq> sendEmailForm = Form.form(SendEmailReq.class).bindFromRequest();
	    if(sendEmailForm.hasErrors()){
	        return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(sendEmailForm))).toObjectNode());
	    }
	    SendEmailReq request = sendEmailForm.get();
	    SendEmailRes response = EntityUserActionManager.sendEmail(request);
        return ok(getResultResponse(response).toObjectNode());

	}
}
