package controllers;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.cmds.managers.SDCardManager;
import com.vedantu.cmds.models.CMDSContentLink;
import com.vedantu.cmds.pojos.requests.exports.GetSDCardContentsReq;
import com.vedantu.cmds.pojos.requests.exports.GetSDCardReq;
import com.vedantu.cmds.pojos.responses.GetResourcesRes;
import com.vedantu.cmds.pojos.responses.GetSDCardInfoRes;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;

public class CMDSSDCards extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(CMDSSDCardGroups.class);

    public static Result get() {

        LOGGER.debug(" Called getSDCard");

        GetSDCardReq request = null;
        GetSDCardInfoRes response = null;

        try {
            Form<GetSDCardReq> requestForm = Form.form(GetSDCardReq.class).bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                LOGGER.error("Errors:" + getErrorMessege(requestForm));
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = SDCardManager.getCard(request);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());

    }

    public static Result getContents() {

        LOGGER.debug(" Called getSDCardGroups");

        GetSDCardContentsReq request = null;
        GetResourcesRes response = null;

        try {
            Form<GetSDCardContentsReq> requestForm = Form.form(GetSDCardContentsReq.class)
                    .bindFromRequest();

            LOGGER.debug("Request " + requestForm.data());

            if (requestForm.hasErrors()) {
                LOGGER.error("Errors:" + getErrorMessege(requestForm));
                return ok(getErrorResponse(
                        new VedantuException(VedantuErrorCode.MISSING_PARAMETERS)).toObjectNode());
            }
            request = requestForm.get();
            response = SDCardManager.getResources(request);

            if (StringUtils.isEmpty(request.orderBy)
                    || request.orderBy.equalsIgnoreCase("customOrder")) {

                request.orderBy = CMDSContentLink.POSITION;
                response = SDCardManager.getResources2(request);
            } else {

                response = SDCardManager.getResources(request);
            }

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }
}
