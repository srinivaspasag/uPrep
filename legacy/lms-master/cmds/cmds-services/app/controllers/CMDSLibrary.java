package controllers;

import java.util.Map;

import org.apache.commons.lang3.StringUtils;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.cmds.managers.CMDSLibraryManager;
import com.vedantu.cmds.models.CMDSContentLink;
import com.vedantu.cmds.pojos.requests.AddToLibraryReq;
import com.vedantu.cmds.pojos.requests.GetLibraryResourcesReq;
import com.vedantu.cmds.pojos.requests.MakeVisibleReq;
import com.vedantu.cmds.pojos.requests.library.GetVisibilityChartReq;
import com.vedantu.cmds.pojos.requests.library.UpdateRankReq;
import com.vedantu.cmds.pojos.responses.AddToLibraryRes;
import com.vedantu.cmds.pojos.responses.GetLibraryResourceRes;
import com.vedantu.cmds.pojos.responses.MakeVisibleRes;
import com.vedantu.cmds.pojos.responses.library.GetVisibilityChartRes;
import com.vedantu.commons.JSONResponse;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.ActionTakenRes;

public class CMDSLibrary extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(CMDSLibrary.class);

    /**
     * Given orgentity .. get resources {@link GetLibraryResourcesReq} {@link GetLibraryResourceRes}
     * 
     * @return
     */
    public static Result getLibraryResources() {

        LOGGER.debug(" Called createDirectory");
        Form<GetLibraryResourcesReq> requestForm = Form.form(GetLibraryResourcesReq.class)
                .bindFromRequest();

        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode(), getErrorMessege(requestForm));
        }

        GetLibraryResourcesReq getLibraryContentReq = requestForm.get();
        GetLibraryResourceRes getLibraryResponse = null;

        try {

            if (StringUtils.isEmpty(getLibraryContentReq.orderBy)
                    || getLibraryContentReq.orderBy.equalsIgnoreCase("customOrder")) {

                getLibraryContentReq.orderBy = CMDSContentLink.POSITION;
                getLibraryResponse = CMDSLibraryManager.getResources2(getLibraryContentReq);
            } else {

                getLibraryResponse = CMDSLibraryManager.getResources(getLibraryContentReq);
            }

        } catch (VedantuException e) {

            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getLibraryResponse).toObjectNode());

    }

    /**
     * Given orgentities ..add resources to section libraries {@link AddToLibraryReq}
     * {@link AddToLibraryRes}
     * 
     * @return
     */
    public static Result addToLibrary() {

        LOGGER.debug(" Called add To library");

        Form<AddToLibraryReq> requestForm = Form.form(AddToLibraryReq.class).bindFromRequest();

        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode(), getErrorMessege(requestForm));
        }

        AddToLibraryReq addToLibraryReq = requestForm.get();
        AddToLibraryRes addToLibraryResponse = null;

        try {
            addToLibraryResponse = CMDSLibraryManager.addToLibrary(addToLibraryReq.userId,
                    addToLibraryReq.orgId, addToLibraryReq.orgEntities, addToLibraryReq.contents);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(addToLibraryResponse).toObjectNode());

    }

    /**
     * Given orgentities ..add resources to section libraries {@link AddToLibraryReq}
     * {@link AddToLibraryRes}
     * 
     * @return
     */
    public static Result removeFromLibrary() {

        LOGGER.debug(" Called remove from library");

        Form<AddToLibraryReq> requestForm = Form.form(AddToLibraryReq.class).bindFromRequest();

        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode(), getErrorMessege(requestForm));
        }

        AddToLibraryReq addToLibraryReq = requestForm.get();
        AddToLibraryRes addToLibraryResponse = null;

        try {
            addToLibraryResponse = CMDSLibraryManager.removeFromLibrary(addToLibraryReq.userId,
                    addToLibraryReq.orgId, addToLibraryReq.orgEntities, addToLibraryReq.contents);

        } catch (VedantuException e) {

            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(addToLibraryResponse).toObjectNode());

    }

    /**
     * Given orgentities ..add resources to section libraries {@link AddToLibraryReq}
     * {@link AddToLibraryRes}
     * 
     * @return
     */
    public static Result makeVisible() {

        LOGGER.debug(" Called makeVisible");
        Form<MakeVisibleReq> requestForm = null;
        //
        try {
            LOGGER.debug("................ Inside try.................");
            // requestForm = (Form<MakeVisibleReq>)
            // Form.form(MakeVisibleReq.class).bindFromRequest();
            requestForm = Form.form(MakeVisibleReq.class).bindFromRequest();

        } catch (Exception exception) {
            LOGGER.debug("................ Inside exception 1.................");
            LOGGER.error(" Failed while parsing data " + requestForm.get(), exception);
            return ok((new JSONResponse(new VedantuException(VedantuErrorCode.SERVICE_ERROR)))
                    .toObjectNode());
        }
        LOGGER.debug(" Request data " + requestForm.get());
        if (requestForm.hasErrors()) {
            LOGGER.debug("................ Inside exception 2.................");
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode(), getErrorMessege(requestForm));
        }
        //
        LOGGER.debug(" Mid way madeVisble");
        MakeVisibleReq request = requestForm.get();
        Map<String, Object> sessionParams = getReqParams();
        MakeVisibleRes response = null;
        //
        try {

            response = CMDSLibraryManager.makeVisible(request, sessionParams);
            //
        } catch (VedantuException e) {
            //
            return ok((new JSONResponse(e)).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());

    }

    public static Result getVisibilityStatus() {

        LOGGER.debug(" Called content visibilty report");
        //

        Form<GetVisibilityChartReq> requestForm = Form.form(GetVisibilityChartReq.class)
                .bindFromRequest();

        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode(), getErrorMessege(requestForm));
        }
        //
        GetVisibilityChartReq request = requestForm.get();
        GetVisibilityChartRes response = null;
        //
        try {

            // MutableLong totalHits = new MutableLong(0);
            response = CMDSLibraryManager.getContentVisibilityReport(request);
            //

        } catch (VedantuException ex) {
            Logger.debug("Error", ex);
            LOGGER.debug("Error", ex);
            return ok((new JSONResponse(new VedantuException(VedantuErrorCode.SERVICE_ERROR)))
                    .toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());

    }

    public static Result move() {

        LOGGER.debug(" Called content visibilty report");
        //

        Form<UpdateRankReq> requestForm = Form.form(UpdateRankReq.class).bindFromRequest();

        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode(), getErrorMessege(requestForm));
        }
        //
        UpdateRankReq request = requestForm.get();
        ActionTakenRes response = null;
        //
        try {

            // MutableLong totalHits = new MutableLong(0);
            response = CMDSLibraryManager.updateLocation(request);
            //

        } catch (VedantuException ex) {
            Logger.debug("Error", ex);
            LOGGER.debug("Error", ex);
            return ok((new JSONResponse(new VedantuException(VedantuErrorCode.SERVICE_ERROR)))
                    .toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());

    }
}
