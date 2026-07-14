package controllers;

import java.util.Map;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.cmds.daos.CMDSVideoDAO;
import com.vedantu.cmds.managers.CMDSVideoManager;
import com.vedantu.cmds.models.CMDSVideo;
import com.vedantu.cmds.pojos.requests.videos.ConfirmVideoUploadReq;
import com.vedantu.cmds.pojos.requests.videos.GetCMDSVideoReq;
import com.vedantu.cmds.pojos.responses.questions.EditContentRes;
import com.vedantu.cmds.pojos.responses.videos.ConfirmVideoUploadRes;
import com.vedantu.cmds.pojos.responses.videos.GetCMDSVideoRes;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.content.pojos.requests.EditContentReq;

public class CMDSVideos extends AbstractVedantuController {

    static ALogger LOGGER = Logger.of(CMDSVideos.class);

    /**
     * Edit existing question {@link ConfirmVideoUploadReq}
     * 
     * @return { {@link ConfirmVideoUploadRes}
     */
    public static Result confirm() {

        LOGGER.debug(" Called createDirectory");

        ConfirmVideoUploadReq request = null;
        GetCMDSVideoRes response = null;

        try {
            Form<ConfirmVideoUploadReq> requestForm = Form.form(ConfirmVideoUploadReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = CMDSVideoManager.INSTANCE.confirmVideo(request);

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

        GetCMDSVideoReq request = null;
        GetCMDSVideoRes response = null;

        try {

            Map<String, String> sessionParamsMap = getSessionParams();

            LOGGER.debug("==== Request session headers  : " + sessionParamsMap);

            Form<GetCMDSVideoReq> requestForm = Form.form(GetCMDSVideoReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }

            request = requestForm.get();
            sessionParamsMap.put("orgId", request.orgId);
            request.__setSessionParams(sessionParamsMap);

            response = CMDSVideoManager.INSTANCE.getVideo(request);

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
            response.isUpdated = CMDSVideoManager.INSTANCE.update(request);
            response.id = request.entity.id;

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result reprocess(String id){
        EditContentRes response = new EditContentRes();
        CMDSVideo cmdsVideo = CMDSVideoDAO.INSTANCE.getById(id);
        int bitrate = -2;
        CMDSVideoManager.INSTANCE.startReprocessingVideo( cmdsVideo,bitrate );
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result convertAgain(String id){
        EditContentRes response = new EditContentRes();
        CMDSVideo cmdsVideo = CMDSVideoDAO.INSTANCE.getById(id);
        int bitrate = 0;
        CMDSVideoManager.INSTANCE.startReprocessingVideo( cmdsVideo,bitrate );
        return ok(getResultResponse(response).toObjectNode());
    }
}
