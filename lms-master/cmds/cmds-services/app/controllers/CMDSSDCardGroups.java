package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.cmds.managers.SDCardGroupManager;
import com.vedantu.cmds.managers.SDCardManager;
import com.vedantu.cmds.pojos.requests.DeleteSdCardGroupReq;
import com.vedantu.cmds.pojos.requests.exports.GetSDCardGroupReq;
import com.vedantu.cmds.pojos.requests.exports.GetSDCardGroupsReq;
import com.vedantu.cmds.pojos.requests.exports.MarkSDGroupReq;
import com.vedantu.cmds.pojos.requests.exports.MoveSDContentReq;
import com.vedantu.cmds.pojos.requests.exports.ScheduleSDGroupCreateReq;
import com.vedantu.cmds.pojos.responses.DeleteSdCardGroupRes;
import com.vedantu.cmds.pojos.responses.GetExportRecordRes;
import com.vedantu.cmds.pojos.responses.GetExportRecordsRes;
import com.vedantu.cmds.pojos.responses.videos.ConfirmVideoUploadRes;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.ActionTakenRes;

public class CMDSSDCardGroups extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(CMDSSDCardGroups.class);

    /**
     *
     *
     * @return { {@link ConfirmVideoUploadRes}
     */
    public static Result schedule() {

        LOGGER.debug(" Called schedule sdcardgroup");

        ScheduleSDGroupCreateReq request = null;
        GetExportRecordRes response = null;

        try {
            Form<ScheduleSDGroupCreateReq> requestForm = Form.form(ScheduleSDGroupCreateReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                LOGGER.error("Errors:" + getErrorMessege(requestForm));
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            SDCardGroupManager groupManager = new SDCardGroupManager();

            response = groupManager.create(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result gets() {

        LOGGER.debug(" Called getSDCardGroups");

        GetSDCardGroupsReq request = null;
        GetExportRecordsRes response = null;

        try {
            Form<GetSDCardGroupsReq> requestForm = Form.form(GetSDCardGroupsReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                                getErrorMessege(requestForm))).toObjectNode());
            }
            request = requestForm.get();
            SDCardGroupManager groupManager = new SDCardGroupManager();
            response = groupManager.getSDCardGroups(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result get() {

        LOGGER.debug(" Called getSDCardGroup");

        GetSDCardGroupReq request = null;
        GetExportRecordRes response = null;

        try {
            Form<GetSDCardGroupReq> requestForm = Form.form(GetSDCardGroupReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                LOGGER.error("Errors:" + getErrorMessege(requestForm));
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            SDCardGroupManager groupManager = new SDCardGroupManager();
            response = groupManager.getSDCardGroup(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result mark() {

        LOGGER.debug(" Called getSDCardGroups");

        MarkSDGroupReq request = null;
        ActionTakenRes response = null;

        try {
            Form<MarkSDGroupReq> requestForm = Form.form(MarkSDGroupReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                LOGGER.error("Errors:" + getErrorMessege(requestForm));
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            SDCardGroupManager groupManager = new SDCardGroupManager();
            response = groupManager.mark(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result move() {

        LOGGER.debug(" Called getSDCardGroups");

        MoveSDContentReq request = null;
        ActionTakenRes response = new ActionTakenRes();

        try {
            Form<MoveSDContentReq> requestForm = Form.form(MoveSDContentReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                LOGGER.error("Errors:" + getErrorMessege(requestForm));
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            SDCardManager cardManager = new SDCardManager();
            response.done = cardManager.move(request.userId, request.content,
                    request.moveFromSDCardId, request.moveToSDCardId, request.orgId);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result deleteSdCardGroup() {
        LOGGER.debug(" called DeleteSdCardGroup");

        DeleteSdCardGroupReq request = null;
        DeleteSdCardGroupRes response = null;

        Form<DeleteSdCardGroupReq> requestForm = Form.form(DeleteSdCardGroupReq.class)
                .bindFromRequest();

        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        request = requestForm.get();
        response = SDCardGroupManager.deleteSdCardGroup(request);

        return ok(getResultResponse(response).toObjectNode());
    }

}
