package controllers;

import play.Logger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.ListResponse;
import com.vedantu.content.managers.DiscussionManager;
import com.vedantu.content.pojos.requests.GetSimilarEntities;
import com.vedantu.content.pojos.requests.RecordTeacherResponseReq;
import com.vedantu.content.pojos.requests.discussions.AddDiscussionReq;
import com.vedantu.content.pojos.requests.discussions.GetDiscussionReq;
import com.vedantu.content.pojos.requests.discussions.GetDiscussionsReq;
import com.vedantu.content.pojos.requests.discussions.RemoveDiscussionReq;
import com.vedantu.content.pojos.responses.RecordTeacherResponseRes;
import com.vedantu.content.pojos.responses.discussions.AddDiscussionRes;
import com.vedantu.content.pojos.responses.discussions.GetDiscussionRes;
import com.vedantu.content.pojos.responses.discussions.RemoveDiscussionRes;

public class Discussions extends AbstractVedantuController {

    public static Result addDiscussion() {

        Form<AddDiscussionReq> addDissForm = Form.form(AddDiscussionReq.class).bindFromRequest();
        if (addDissForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(addDissForm))).toObjectNode());
        }
        AddDiscussionReq addDissReq = addDissForm.get();
        AddDiscussionRes addDissRes = null;
        try {
            addDissRes = DiscussionManager.addDiscussion(addDissReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(addDissRes).toObjectNode());
    }

    public static Result getDiscussionInfo() {
        Form<GetDiscussionReq> getDissForm = Form.form(GetDiscussionReq.class).bindFromRequest();
        if (getDissForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getDissForm))).toObjectNode());
        }
        GetDiscussionReq getDissReq = getDissForm.get();
        GetDiscussionRes getDissRes = null;
        try {
            getDissRes = DiscussionManager.getDiscussionInfo(getDissReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getDissRes).toObjectNode());
    }

    public static Result removeDiscussion() {
        Form<RemoveDiscussionReq> removeDissForm = Form.form(RemoveDiscussionReq.class)
                .bindFromRequest();
        if (removeDissForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(removeDissForm))).toObjectNode());
        }
        RemoveDiscussionReq removeDissReq = removeDissForm.get();
        RemoveDiscussionRes removeDissRes = null;
        try {
            removeDissRes = DiscussionManager.removeDiscussion(removeDissReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(removeDissRes).toObjectNode());
    }

    public static Result getDiscussions() {

        Form<GetDiscussionsReq> getDissForm = Form.form(GetDiscussionsReq.class).bindFromRequest();
        Logger.debug("request params: " + getDissForm.data());
        if (getDissForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, getDissForm.errors()
                            .toString())).toObjectNode());
        }
        GetDiscussionsReq getDissListReq = getDissForm.get();
        ListResponse<GetDiscussionRes> getDissListRes = DiscussionManager
                .getDiscussions(getDissListReq);
        return ok(getResultResponse(getDissListRes).toObjectNode());
    }

    public static Result fixDiscussions() {

        Form<GetDiscussionReq> getDissForm = Form.form(GetDiscussionReq.class).bindFromRequest();
        Logger.debug("request params: " + getDissForm.data());
        if (getDissForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, getDissForm.errors()
                            .toString())).toObjectNode());
        }
        GetDiscussionReq getDissListReq = getDissForm.get();
        GetDiscussionRes getDissListRes = DiscussionManager
                .fixDiscussions(getDissListReq);
        return ok(getResultResponse(getDissListRes).toObjectNode());
    }

    public static Result getSimilarDiscussions() {
        Form<GetSimilarEntities> getSimilarDissForm = Form.form(GetSimilarEntities.class)
                .bindFromRequest();
        if (getSimilarDissForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS, getSimilarDissForm
                            .errors().toString())).toObjectNode());
        }
        GetSimilarEntities getSimilarDissListReq = getSimilarDissForm.get();
        ListResponse<GetDiscussionRes> getDissListRes = DiscussionManager
                .getSimilarDiscussions(getSimilarDissListReq);
        return ok(getResultResponse(getDissListRes).toObjectNode());
    }

    public static Result recordTeacherResponse() {
        Form<RecordTeacherResponseReq> recordTeacherResponseForm = Form.form(
                RecordTeacherResponseReq.class).bindFromRequest();
        if (recordTeacherResponseForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            recordTeacherResponseForm.errors().toString())).toObjectNode());
        }
        RecordTeacherResponseReq recordTeacherResponseReq = recordTeacherResponseForm.get();
        RecordTeacherResponseRes recordTeacherResponseRes = DiscussionManager
                .recordTeacherResponse(recordTeacherResponseReq);
        return ok(getResultResponse(recordTeacherResponseRes).toObjectNode());
    }
}
