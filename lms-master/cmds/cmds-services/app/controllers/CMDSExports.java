package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.cmds.managers.ExportRecordManager;
import com.vedantu.cmds.pojos.requests.exports.GetExportDetailsReq;
import com.vedantu.cmds.pojos.requests.exports.GetExportsReq;
import com.vedantu.cmds.pojos.requests.exports.ScheduleExportReq;
import com.vedantu.cmds.pojos.responses.DeleteExportRecordRes;
import com.vedantu.cmds.pojos.responses.GetExportDetailsRes;
import com.vedantu.cmds.pojos.responses.GetExportRecordRes;
import com.vedantu.cmds.pojos.responses.GetExportRecordsRes;
import com.vedantu.cmds.pojos.responses.videos.ConfirmVideoUploadRes;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;

public class CMDSExports extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(CMDSExports.class);

    /**
     * 
     * 
     * @return { {@link ConfirmVideoUploadRes}
     */
    public static Result schedule() {

        LOGGER.debug(" Called schedule Export");

        ScheduleExportReq request = null;
        GetExportRecordRes response = null;

        try {
            Form<ScheduleExportReq> requestForm = Form.form(ScheduleExportReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                LOGGER.error("Errors:" + getErrorMessege(requestForm));
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = ExportRecordManager.start(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getExports() {

        LOGGER.debug(" Called getExports");

        GetExportsReq request = null;
        GetExportRecordsRes response = null;

        try {
            Form<GetExportsReq> requestForm = Form.form(GetExportsReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                LOGGER.error("Errors:" + getErrorMessege(requestForm));
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = ExportRecordManager.getExports(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getExportDetails() {

        LOGGER.debug(" Called getExports");

        GetExportDetailsReq request = null;
        GetExportDetailsRes response = null;

        try {
            Form<GetExportDetailsReq> requestForm = Form.form(GetExportDetailsReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                LOGGER.error("Errors:" + getErrorMessege(requestForm));
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = ExportRecordManager.getExportDetails(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }
    
    
    public static Result delete() {

        LOGGER.debug(" Called exports delete");

        GetExportDetailsReq request = null;
        DeleteExportRecordRes response = null;

        try {
            Form<GetExportDetailsReq> requestForm = Form.form(GetExportDetailsReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                LOGGER.error("Errors:" + getErrorMessege(requestForm));
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = ExportRecordManager.cancel(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }
}
