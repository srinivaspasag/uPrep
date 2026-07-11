package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.cmds.managers.NotificationManager;
import com.vedantu.cmds.models.Notification;
import com.vedantu.cmds.pojos.requests.notifications.GetRegIdsReq;
import com.vedantu.cmds.pojos.requests.notifications.NotificationRegIDReq;
import com.vedantu.cmds.pojos.responses.notifications.GetRegIdsRes;
import com.vedantu.cmds.pojos.responses.notifications.NotificationRegIDRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;

public class Notifications extends AbstractVedantuController {

	    private static ALogger LOGGER = Logger.of(Notification.class);
	    public static Result registerById() {

	        Form<NotificationRegIDReq> notificationRegIDForm = Form.form(NotificationRegIDReq.class)
	                .bindFromRequest();
	        Logger.debug("request params : " + notificationRegIDForm.data());
	        if (notificationRegIDForm.hasErrors()) {
	            return ok(getErrorResponse(
	                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
	                            getErrorMessege(notificationRegIDForm))).toObjectNode());
	        }
	        NotificationRegIDReq getNotificationRegIdReq = notificationRegIDForm.get();
	        NotificationRegIDRes getNotificationRegIdRes = null;
	        try {
	        	getNotificationRegIdRes = NotificationManager.INSTANCE.registerById(getNotificationRegIdReq);
	        } catch (VedantuException e) {
	            return ok(getErrorResponse(e).toObjectNode());
	        }
	        return ok(getResultResponse(getNotificationRegIdRes).toObjectNode());
	    }

	    public static Result getRegIds() {
	    	Form<GetRegIdsReq> getRegIDForm = Form.form(GetRegIdsReq.class)
	                .bindFromRequest();
	        Logger.debug("request params : " + getRegIDForm.data());
	        if (getRegIDForm.hasErrors()) {
	            return ok(getErrorResponse(
	                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
	                            getErrorMessege(getRegIDForm))).toObjectNode());
	        }
	    	GetRegIdsReq getRegIdsReq = getRegIDForm.get();
	    	GetRegIdsRes getRegIdsRes = null ;
	    	try {
	        	getRegIdsRes = NotificationManager.INSTANCE.getRegIds(getRegIdsReq);
	        } catch (VedantuException e) {
	            return ok(getErrorResponse(e).toObjectNode());
	        }
	    	return ok(getResultResponse(getRegIdsRes).toObjectNode());
	    }
	}