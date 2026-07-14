package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.cmds.managers.CMDSFileManager;
import com.vedantu.cmds.pojos.requests.files.ConfirmFileUploadReq;
import com.vedantu.cmds.pojos.requests.files.GetCMDSFileReq;
import com.vedantu.cmds.pojos.requests.videos.ConfirmVideoUploadReq;
import com.vedantu.cmds.pojos.responses.files.GetCMDSFileRes;
import com.vedantu.cmds.pojos.responses.questions.EditContentRes;
import com.vedantu.cmds.pojos.responses.videos.ConfirmVideoUploadRes;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.content.pojos.requests.EditContentReq;

public class CMDSFiles extends AbstractVedantuController {

    static ALogger LOGGER = Logger.of(CMDSFiles.class);

    /**
     * Edit existing question {@link ConfirmVideoUploadReq}
     * 
     * @return { {@link ConfirmVideoUploadRes}
     */
    public static Result confirm() {

        LOGGER.debug(" Called createDirectory");

        ConfirmFileUploadReq request = null;
        GetCMDSFileRes response = null;

        try {
            Form<ConfirmFileUploadReq> requestForm = Form.form(ConfirmFileUploadReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = CMDSFileManager.INSTANCE.confirm(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    /**
     * Edit existing question {@link ConfirmVideoUploadReq}
     * 
     * @return { {@link ConfirmVideoUploadRes}
     */
    public static Result get() {

        LOGGER.debug(" Called createDirectory");

        GetCMDSFileReq request = null;
        GetCMDSFileRes response = null;

        try {
            Form<GetCMDSFileReq> requestForm = Form.form(GetCMDSFileReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = CMDSFileManager.INSTANCE.get(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    /**
     * Edit existing question {@link EditContentReq}
     * 
     * @return { {@link EditContentRes}
     */
    public static Result update() {

        LOGGER.debug(" Called update");

        EditContentReq request = null;
        EditContentRes response = new EditContentRes();

        try {
            Form<EditContentReq> requestForm = Form.form(EditContentReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response.isUpdated = CMDSFileManager.INSTANCE.update(request);
            response.id = request.entity.id;

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

}
