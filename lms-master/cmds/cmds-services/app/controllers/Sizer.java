package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.cmds.maintenance.managers.ContentSizeManager;
import com.vedantu.cmds.pojos.requests.GetLibraryResourcesReq;
import com.vedantu.cmds.pojos.requests.LibSizeReq;
import com.vedantu.cmds.pojos.requests.ReIndexResourceReq;
import com.vedantu.cmds.pojos.responses.GetLibraryResourceRes;
import com.vedantu.cmds.pojos.responses.ReIndexRes;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.ActionTakenRes;

public class Sizer extends AbstractVedantuController {

    static ALogger LOGGER = Logger.of(CMDSUpgrades.class);

    /**
     * Given orgentity .. get resources {@link GetLibraryResourcesReq} {@link GetLibraryResourceRes}
     * 
     * @return
     */

    public static Result calculate() {

        LOGGER.debug(" Called reIndex");
        Form<ReIndexResourceReq> requestForm = Form.form(ReIndexResourceReq.class)
                .bindFromRequest();

        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }

        ReIndexResourceReq request = requestForm.get();
        ReIndexRes response = null;

        response = ContentSizeManager.calculate(request.type, request.includes, request.userId);

        return ok(getResultResponse(response).toObjectNode());

    }

    public static Result calculateLibs() {

        LOGGER.debug(" Called reIndex");
        Form<LibSizeReq> requestForm = Form.form(LibSizeReq.class).bindFromRequest();

        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }

        LibSizeReq request = requestForm.get();
        ActionTakenRes response = null;

        try {
            response = ContentSizeManager.calculate(request.orgId, request.sectionId);
        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());

        }

        return ok(getResultResponse(response).toObjectNode());

    }

}
