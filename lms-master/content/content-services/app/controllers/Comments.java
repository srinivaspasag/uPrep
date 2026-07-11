package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.content.managers.CommentManager;
import com.vedantu.content.pojos.requests.comments.AddCommentReq;
import com.vedantu.content.pojos.requests.comments.GetCommentReq;
import com.vedantu.content.pojos.requests.comments.GetCommentsReq;
import com.vedantu.content.pojos.responses.comments.AddCommentRes;
import com.vedantu.content.pojos.responses.comments.GetCommentRes;
import com.vedantu.content.pojos.responses.comments.GetCommentsRes;

public class Comments extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(Comments.class);

    public static Result addComment() {

        Form<AddCommentReq> addCommentForm = Form.form(AddCommentReq.class).bindFromRequest();
        if (addCommentForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(addCommentForm))).toObjectNode());
        }
        AddCommentReq addCommentReq = addCommentForm.get();
        AddCommentRes addCommentRes = null;
        try {
            addCommentRes = CommentManager.addComment(addCommentReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(addCommentRes).toObjectNode());
    }

    public static Result getComment() {

        Form<GetCommentReq> getCommentForm = Form.form(GetCommentReq.class).bindFromRequest();
        if (getCommentForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getCommentForm))).toObjectNode());
        }
        GetCommentReq getCommentReq = getCommentForm.get();
        GetCommentRes getCommentRes = null;
        try {
            getCommentRes = CommentManager.getComment(getCommentReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getCommentRes).toObjectNode());
    }

    public static Result getComments() {

        Form<GetCommentsReq> getCommentsForm = Form.form(GetCommentsReq.class).bindFromRequest();
        LOGGER.debug("getComments : request parms : " + getCommentsForm.data());
        if (getCommentsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getCommentsForm))).toObjectNode());
        }
        GetCommentsReq getCommentsReq = getCommentsForm.get();
        GetCommentsRes getCommentsRes = null;
        try {
            getCommentsRes = CommentManager.getComments(getCommentsReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getCommentsRes).toObjectNode());
    }
}
