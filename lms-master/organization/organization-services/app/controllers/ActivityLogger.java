package controllers;

import play.Logger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.organization.managers.UserStatusManager;
import com.vedantu.organization.pojos.requests.device.mgmt.DeviceLoginReq;
import com.vedantu.organization.pojos.requests.device.mgmt.DeviceLogoutReq;
import com.vedantu.organization.pojos.requests.device.mgmt.DeviceStatusRes;
import com.vedantu.organization.pojos.requests.device.mgmt.GetUserDeviceStatusReq;
import com.vedantu.organization.pojos.requests.device.mgmt.GetUserStatusReq;
import com.vedantu.organization.pojos.requests.device.mgmt.RecordActivityReq;
import com.vedantu.organization.pojos.requests.organizations.GetLatestActivityReq;
import com.vedantu.organization.pojos.responses.device.mgmt.GetUserDeviceStatusesRes;
import com.vedantu.organization.pojos.responses.device.mgmt.GetUserStatusesRes;
import com.vedantu.organization.pojos.responses.device.mgmt.RecordActivityRes;
import com.vedantu.organization.pojos.responses.organizations.GetLatestActivityRes;
import com.vedantu.organization.pojos.responses.organizations.GetLatestActivityResponseList;

public class ActivityLogger extends AbstractVedantuController {

    public static Result record() {

        Form<RecordActivityReq> requestForm = Form.form(RecordActivityReq.class).bindFromRequest();
        Logger.debug("request params : " + requestForm.data());
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        RecordActivityReq request = requestForm.get();
        RecordActivityRes response = null;
        try {
            response = UserStatusManager.INSTANCE.recordActivity(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getUsers() {

        Form<GetUserStatusReq> requestForm = Form.form(GetUserStatusReq.class).bindFromRequest();
        Logger.debug("request params : " + requestForm.data());
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GetUserStatusReq request = requestForm.get();
        GetUserStatusesRes response = null;
        try {
            response = UserStatusManager.INSTANCE.getUsers(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getUserStatus() {

        Form<GetUserDeviceStatusReq> requestForm = Form.form(GetUserDeviceStatusReq.class)
                .bindFromRequest();
        Logger.debug("request params : " + requestForm.data());
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GetUserDeviceStatusReq request = requestForm.get();
        GetUserDeviceStatusesRes response = null;
        try {
            response = UserStatusManager.INSTANCE.getUserStatus(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result login() {

        Form<DeviceLoginReq> requestForm = Form.form(DeviceLoginReq.class).bindFromRequest();
        Logger.debug("request params : " + requestForm.data());
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        DeviceLoginReq request = requestForm.get();
        DeviceStatusRes response = null;
        try {
            response = UserStatusManager.INSTANCE.newLogin(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result logout() {

        Form<DeviceLogoutReq> requestForm = Form.form(DeviceLogoutReq.class).bindFromRequest();
        Logger.debug("request params : " + requestForm.data());
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        DeviceLogoutReq request = requestForm.get();
        DeviceStatusRes response = null;
        try {
            response = UserStatusManager.INSTANCE.newLogout(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result checkIfUserExists() {
        // Logger.debug("Amrita inside check user");
        Form<DeviceLogoutReq> requestForm = Form.form(DeviceLogoutReq.class).bindFromRequest();
        Logger.debug("request params : " + requestForm.data());
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        DeviceLogoutReq request = requestForm.get();
        DeviceStatusRes response = null;
        try {
            response = UserStatusManager.INSTANCE.checkUserInDB(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());

    }
    
    public static Result getStudentActivity(){
    
    	Form<GetLatestActivityReq> requestForm = Form.form(GetLatestActivityReq.class).bindFromRequest();
        Logger.debug("request params : " + requestForm.data());
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GetLatestActivityReq request = requestForm.get();
        GetLatestActivityResponseList<GetLatestActivityRes> response = null;
        try {
            response = UserStatusManager.INSTANCE.getStudentActivity(request);
            
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

}
