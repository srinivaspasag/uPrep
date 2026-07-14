package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.SearchListResponse;
import com.vedantu.content.managers.ModuleManager;
import com.vedantu.content.pojos.requests.GetModuleReq;
import com.vedantu.content.pojos.requests.GetModuleScheduleReq;
import com.vedantu.content.pojos.requests.GetModulesReq;
import com.vedantu.content.pojos.requests.GetUserModuleReq;
import com.vedantu.content.pojos.requests.GetUserModulesReq;
import com.vedantu.content.pojos.requests.SyncModuleReq;
import com.vedantu.content.pojos.requests.UpdateUserModuleReq;
import com.vedantu.content.pojos.responses.GetModuleRes;
import com.vedantu.content.pojos.responses.GetModuleScheduleRes;
import com.vedantu.content.pojos.responses.GetUserModuleRes;
import com.vedantu.content.pojos.responses.GetUserModulesRes;
import com.vedantu.content.pojos.responses.SyncModuleRes;
import com.vedantu.content.pojos.responses.UpdateUserModuleRes;

public class Modules extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(Files.class);

    public static Result updateUserModuleStatus() {

        Form<UpdateUserModuleReq> requestForm = Form.form(UpdateUserModuleReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        UpdateUserModuleReq request = requestForm.get();
        UpdateUserModuleRes response = null;
        try {
            response = ModuleManager.INSTANCE.updateUserModuleStatus(request);
            } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }
    
    
//    public static Result syncUserModuleStatus() {
//
//        Form<SyncUserModuleReq> requestForm = Form.form(SyncUserModuleReq.class).bindFromRequest();
//        if (requestForm.hasErrors()) {
//            return ok(getErrorResponse(
//                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
//                            getErrorMessege(requestForm))).toObjectNode());
//        }
//        SyncUserModuleReq request = requestForm.get();
//        SyncUserModuleRes response = null;
//        try {
//            response = ModuleManager.INSTANCE.syncUserModuleStatus(request);
//            } catch (VedantuException e) {
//            return ok(getErrorResponse(e).toObjectNode());
//        }
//        return ok(getResultResponse(response).toObjectNode());
//    }
      
    
    public static Result syncModule() {
        LOGGER.debug("..........Inside sync module function............");
        Form<SyncModuleReq> syncModuleReqForm = Form.form(
                SyncModuleReq.class).bindFromRequest();
        if (syncModuleReqForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(syncModuleReqForm))).toObjectNode());
        }
        
        SyncModuleReq syncModuleReq = syncModuleReqForm.get();
        SyncModuleRes syncModuleRes = null;
        try {
            syncModuleRes = ModuleManager.syncModule(
                    syncModuleReq);
            LOGGER.debug("..........End of sync module function............");
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(syncModuleRes).toObjectNode());
    }
    
    public static Result getUserModuleStatus() {

        Form<GetUserModuleReq> requestForm = Form.form(GetUserModuleReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GetUserModuleReq request = requestForm.get();
        GetUserModuleRes response = null;
        try {
            response = ModuleManager.INSTANCE.getUserModuleStatus(request);
            } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }
    
    public static Result getUserModulesStatus() {

        Form<GetUserModulesReq> requestForm = Form.form(GetUserModulesReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GetUserModulesReq request = requestForm.get();
        GetUserModulesRes response = null;
        try {
            response = ModuleManager.INSTANCE.getUserModulesStatus(request);
            } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }
    
//    public static Result getModules() {
//
//        Form<GetModulesReq1> requestForm = Form.form(GetModulesReq1.class).bindFromRequest();
//        if (requestForm.hasErrors()) {
//            return ok(getErrorResponse(
//                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
//                            getErrorMessege(requestForm))).toObjectNode());
//        }
//        GetModulesReq1 request = requestForm.get();
//        GetModulesRes response = null;
//        try {
//            response = ModuleManager.INSTANCE.getModules(request);
//            } catch (VedantuException e) {
//            return ok(getErrorResponse(e).toObjectNode());
//        }
//        return ok(getResultResponse(response).toObjectNode());
//    }
//    
    
    public static Result getModules() {

        Form<GetModulesReq> requestForm = Form.form(GetModulesReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GetModulesReq request = requestForm.get();
        SearchListResponse<GetModuleRes> getModulesRes = null;
        try {
            getModulesRes = ModuleManager.INSTANCE.getModules(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getModulesRes).toObjectNode());
    }

    public static Result getModuleSchedules(){
        LOGGER.debug(" Called getModuleSchedules");
        Form<GetModuleScheduleReq> getModuleSchedulesReqForm = Form.form(GetModuleScheduleReq.class)
                .bindFromRequest();

        if (getModuleSchedulesReqForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }

        GetModuleScheduleReq getModuleSchedulesReq = getModuleSchedulesReqForm.get();
        GetModuleScheduleRes getModuleSchedulesResponse = null;

        try {
            getModuleSchedulesResponse = ModuleManager.getModuleSchedules(getModuleSchedulesReq);

        } catch (VedantuException e) {

            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getModuleSchedulesResponse).toObjectNode());
    }

    
    public static Result getModule() {

        Form<GetModuleReq> requestForm = Form.form(GetModuleReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GetModuleReq request = requestForm.get();
        GetModuleRes getModuleRes = null;
        try {
            getModuleRes = ModuleManager.INSTANCE.getModule(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getModuleRes).toObjectNode());
    }
}
