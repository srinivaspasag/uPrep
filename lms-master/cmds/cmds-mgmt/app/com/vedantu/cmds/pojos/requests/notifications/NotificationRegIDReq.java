package com.vedantu.cmds.pojos.requests.notifications;

import play.Logger;
import play.data.validation.Constraints.Required;

import com.vedantu.organization.pojos.requests.AbstractOrgScopeReq;

public class NotificationRegIDReq extends AbstractOrgScopeReq {
	@Required
	public String regId;
	@Required
	public String deviceId;
	@Required
	public String programName;

	public String validate() {
		Logger.debug(".....Inside NotificationRegIdReqValidation validate function.........");

		if (regId == null) {
			return "regId is null";
		}
		if (deviceId == null) {
			return "deviceId is null";
		}
		if(userId == null)
			return "userId is null";
		return null;
	}
}