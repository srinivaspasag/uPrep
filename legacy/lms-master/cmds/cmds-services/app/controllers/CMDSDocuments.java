package controllers;

import java.util.Map;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.cmds.managers.CMDSDocumentManager;
import com.vedantu.cmds.pojos.requests.documents.ConfirmDocumentUploadReq;
import com.vedantu.cmds.pojos.requests.documents.GetCMDSDocumentReq;
import com.vedantu.cmds.pojos.requests.videos.ConfirmVideoUploadReq;
import com.vedantu.cmds.pojos.responses.documents.GetCMDSDocumentRes;
import com.vedantu.cmds.pojos.responses.questions.EditContentRes;
import com.vedantu.cmds.pojos.responses.videos.ConfirmVideoUploadRes;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.content.pojos.requests.EditContentReq;

public class CMDSDocuments extends AbstractVedantuController {

    static ALogger LOGGER = Logger.of(CMDSDocuments.class);

    /**
     * Edit existing question {@link ConfirmVideoUploadReq}
     * 
     * @return { {@link ConfirmVideoUploadRes}
     */
    public static Result confirm() {

        LOGGER.debug(" Called createDirectory");

        ConfirmDocumentUploadReq request = null;
        GetCMDSDocumentRes response = null;

        try {
            Form<ConfirmDocumentUploadReq> requestForm = Form.form(ConfirmDocumentUploadReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = CMDSDocumentManager.INSTANCE.confirm(request);

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

        GetCMDSDocumentReq request = null;
        GetCMDSDocumentRes response = null;

        try {

            Map<String, String> sessionParamsMap = getSessionParams();
            LOGGER.debug("==== Request session headers  : " + sessionParamsMap);

            Form<GetCMDSDocumentReq> requestForm = Form.form(GetCMDSDocumentReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }

            request = requestForm.get();
            request.__setSessionParams(sessionParamsMap);

            response = CMDSDocumentManager.INSTANCE.get(request);

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
            response.isUpdated = CMDSDocumentManager.INSTANCE.update(request);
            response.id = request.entity.id;

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    /**
     * Reconvert documetns {@link ConfirmDocumentUploadReq}
     * 
     * @return { {@link GetCMDSDocumentRes}
     */
    public static Result reconvert() {

        LOGGER.debug(" Called update");

        GetCMDSDocumentReq request = null;
        GetCMDSDocumentRes response = new GetCMDSDocumentRes();

        try {
            Form<GetCMDSDocumentReq> requestForm = Form.form(GetCMDSDocumentReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = CMDSDocumentManager.INSTANCE.reconvert(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }
}
