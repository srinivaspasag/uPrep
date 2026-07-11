package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.comm.managers.MessageManager;
import com.vedantu.comm.requests.messages.DeleteConversationReq;
import com.vedantu.comm.requests.messages.GetConversationReq;
import com.vedantu.comm.requests.messages.GetConversationSummariesReq;
import com.vedantu.comm.requests.messages.GetConversationSummaryReq;
import com.vedantu.comm.requests.messages.GetConversationUsersReq;
import com.vedantu.comm.requests.messages.GetMessageReq;
import com.vedantu.comm.requests.messages.GetMessageSummariesReq;
import com.vedantu.comm.requests.messages.GetMessageSummaryReq;
import com.vedantu.comm.requests.messages.GetUserMailBoxInfoReq;
import com.vedantu.comm.requests.messages.MarkConversationReq;
import com.vedantu.comm.requests.messages.SendMessageReq;
import com.vedantu.comm.requests.messages.UpdateUserMailBoxInfoReq;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.pojos.response.messages.DeleteConversationRes;
import com.vedantu.pojos.response.messages.GetConversationRes;
import com.vedantu.pojos.response.messages.GetConversationSummariesRes;
import com.vedantu.pojos.response.messages.GetConversationSummaryRes;
import com.vedantu.pojos.response.messages.GetConversationUsersRes;
import com.vedantu.pojos.response.messages.GetMessageRes;
import com.vedantu.pojos.response.messages.GetMessageSummariesRes;
import com.vedantu.pojos.response.messages.GetMessageSummaryRes;
import com.vedantu.pojos.response.messages.GetUserMailBoxInfoRes;
import com.vedantu.pojos.response.messages.MarkConversationRes;
import com.vedantu.pojos.response.messages.SendMessageRes;
import com.vedantu.pojos.response.messages.UpdatedUserMailBoxInfoRes;
@Deprecated
public class MessageCenter extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(MessageCenter.class);

    // TODO verify this later and then remove
    // private static final NewsEntitySerializer newsEntitySerializer = new
    // NewsEntitySerializer();

    public static Result getMessageSummary() {

        LOGGER.debug(" Called adding remarks ");

        GetMessageSummaryReq request = null;
        GetMessageSummaryRes response = new GetMessageSummaryRes();

        try {
            Form<GetMessageSummaryReq> requestForm = Form.form(GetMessageSummaryReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response.summary = MessageManager.get().getMessageSummary(request.orgId,
                    request.userId, request.userMessageId);

        } catch (VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());

        }

        return ok(getResultResponse(response).toObjectNode());

    }

    public static Result getMessageSummaries() {

        GetMessageSummariesReq request = null;
        GetMessageSummariesRes response = new GetMessageSummariesRes();

        try {
            Form<GetMessageSummariesReq> requestForm = Form.form(GetMessageSummariesReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = MessageManager.get().getMessageSummaries(request);
        } catch (VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());

        }

        return ok(getResultResponse(response).toObjectNode());

    }

    public static Result getConversationSummary() {

        GetConversationSummaryReq request = null;
        GetConversationSummaryRes response = new GetConversationSummaryRes();

        try {
            Form<GetConversationSummaryReq> requestForm = Form
                    .form(GetConversationSummaryReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = MessageManager.get().getConversationSummary(request);
        } catch (VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());

        }

        return ok(getResultResponse(response).toObjectNode());

    }

   
    public static Result getUserMailBoxInfo() {

        GetUserMailBoxInfoReq request = null;
        GetUserMailBoxInfoRes response = null;

        try {
            Form<GetUserMailBoxInfoReq> requestForm = Form.form(GetUserMailBoxInfoReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = MessageManager.get().getUserMailBoxInfo(request);
        } catch (VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());

        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getConversationSummaries() {

        GetConversationSummariesReq request = null;
        GetConversationSummariesRes response = null;

        try {
            Form<GetConversationSummariesReq> requestForm = Form.form(
                    GetConversationSummariesReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = MessageManager.get().getConversationSummaries(request);

        } catch (VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());

        }

        return ok(getResultResponse(response).toObjectNode());
    }


    public static Result getMessage() {

        GetMessageReq request = null;
        GetMessageRes response = null;

        try {
            Form<GetMessageReq> requestForm = Form.form(GetMessageReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());
            request = requestForm.get();
            response = MessageManager.get().getMessage(request);
        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());

        }
        return ok(getResultResponse(response).toObjectNode());

    }

    public static Result markConversation() {

        MarkConversationReq request = null;
        MarkConversationRes response = null;

        try {
            Form<MarkConversationReq> requestForm = Form.form(MarkConversationReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = MessageManager.get().markConversation(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());

        }

        return ok(getResultResponse(response).toObjectNode());

    }

    

    /*
     * public static void markMessageAsUnread() {
     * 
     * boolean isMarkedUnread = false; try { isMarkedUnread =
     * MessageManager.get().markAsRead(userId, userMessageId); } catch (HBaseException e) {
     * Logger.log4j.debug(" Could not delete message id:" + userMessageId + " requested by id:" +
     * userId); Logger.log4j.debug(e.getStackTrace());
     * renderJSON(errorResponse(ErrorCode.SERVER_FAILED)); } DBObject resultObject = new
     * BasicDBObject();
     * 
     * resultObject.put(ConstantsGlobal.IS_MARKED_UNREAD, Boolean.toString(isMarkedUnread));
     * renderJSON(new JSONResponse(resultObject)); }
     * 
     * public static void markMessageAsRead() {
     * 
     * boolean isMarkedRead = false;
     * 
     * try { isMarkedRead = MessageManager.get().markAsUnread(userId, userMessageId); } catch
     * (HBaseException e) { Logger.log4j.debug(" Could not delete message id:" + userMessageId +
     * " requested by id:" + userId); Logger.log4j.debug(e.getStackTrace());
     * renderJSON(errorResponse(ErrorCode.SERVER_FAILED)); }
     * 
     * DBObject resultObject = new BasicDBObject();
     * resultObject.put(ConstantsGlobal.IS_MARKED_UNREAD, Boolean.toString(isMarkedRead));
     * renderJSON(new JSONResponse(resultObject));
     * 
     * }
     */

    public static Result getMessageSummariesBefore() {

        GetMessageSummariesReq request = null;
        GetMessageSummariesRes response = null;

        try {
            Form<GetMessageSummariesReq> requestForm = Form.form(GetMessageSummariesReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = MessageManager.get().getMessageSummaries(request);

        } catch (VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());

        }
        return ok(getResultResponse(response).toObjectNode());

    }

    //
    // public static void deleteMessage() {
    //
    // boolean isDeleted = false;
    //
    // try { isDeleted = MessageManager.get().deleteUserMessage(userId,
    // userMessageId); } catch (HBaseException e) {
    // Logger.log4j.debug(" Could not delete message id:" + userMessageId +
    // requested by id:" + userId); Logger.log4j.debug(e.getStackTrace());
    // renderJSON(errorResponse(ErrorCode.SERVER_FAILED)); }
    // renderJSON(isDeleted);
    //
    // }

    public static Result deleteConversation() {

        DeleteConversationReq request = null;
        DeleteConversationRes response = null;

        try {
            Form<DeleteConversationReq> requestForm = Form.form(DeleteConversationReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = MessageManager.get().deleteUserConversation(request);

        } catch (VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());

        }

        return ok(getResultResponse(response).toObjectNode());

    }

    public static Result sendMessage() {

        SendMessageReq request = null;
        SendMessageRes response = null;

        try {
            Form<SendMessageReq> requestForm = Form.form(SendMessageReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
             response = MessageManager.get().sendMessage(request);
        
        } catch (VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());

        }

        return ok(getResultResponse(response).toObjectNode());

    }

    public static Result getConversation() {

        GetConversationReq request = null;
        GetConversationRes response = null;

        try {
            Form<GetConversationReq> requestForm = Form.form(GetConversationReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();

            response = MessageManager.get().getConversation(request);

        } catch (VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());

        }

        return ok(getResultResponse(response).toObjectNode());

    }

   
    public static Result getConversationUsers() {

        GetConversationUsersReq request = null;
        GetConversationUsersRes response = null;

        try {
            Form<GetConversationUsersReq> requestForm = Form.form(GetConversationUsersReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();

            response = MessageManager.get().getConversationUsers(request);

        } catch (VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());

        }

        return ok(getResultResponse(response).toObjectNode());

    }

  

    /** Maintanance api */

    public static Result updateMailBoxInfo() {

        UpdateUserMailBoxInfoReq request = null;
        UpdatedUserMailBoxInfoRes response = null;

        try {
            Form<UpdateUserMailBoxInfoReq> requestForm = Form.form(UpdateUserMailBoxInfoReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();

            response = MessageManager.get().updateUsersMailBoxesInfos(request.userIds);

        } catch (VedantuException e) {
            return ok((new JSONResponse(e)).toObjectNode());

        }

        return ok(getResultResponse(response).toObjectNode());

    }

}