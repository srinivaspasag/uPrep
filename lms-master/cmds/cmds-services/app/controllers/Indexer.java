package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.cmds.maintenance.managers.IndexingManager;
import com.vedantu.cmds.pojos.requests.GetLibraryResourcesReq;
import com.vedantu.cmds.pojos.requests.ReIndexLibraryContentReq;
import com.vedantu.cmds.pojos.requests.ReIndexResourceReq;
import com.vedantu.cmds.pojos.responses.GetLibraryResourceRes;
import com.vedantu.cmds.pojos.responses.ReIndexRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;

public class Indexer extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(Indexer.class);

    /**
     * Given orgentity .. get resources {@link GetLibraryResourcesReq} {@link GetLibraryResourceRes}
     * 
     * @return
     */

    public static Result reIndex() {

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

        try {
            response = IndexingManager.INSTANCE.index(request);
        } catch (VedantuException e) {

            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());

    }

    public static Result reIndexLibraryLinks() {

        Form<ReIndexLibraryContentReq> requestForm = Form.form(ReIndexLibraryContentReq.class)
                .bindFromRequest();

        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }

        ReIndexLibraryContentReq request = requestForm.get();
        ReIndexRes response = null;

        try {
            response = IndexingManager.INSTANCE.reIndexLibraryContentLinks(request);
        } catch (VedantuException e) {

            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());

    }

    public static Result reIndexUserActionMappings() {

        Form<ReIndexLibraryContentReq> requestForm = Form.form(ReIndexLibraryContentReq.class)
                .bindFromRequest();

        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }

        ReIndexLibraryContentReq request = requestForm.get();
        ReIndexRes response = null;

        try {
            response = IndexingManager.INSTANCE.reIndexUserActionMappings(request);
        } catch (VedantuException e) {

            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());

    }
}
