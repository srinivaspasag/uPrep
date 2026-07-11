package controllers;

import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.utils.ObjectIdUtils;
import com.vedantu.commons.utils.URLUtils;
import com.vedantu.organization.managers.MicrositeManager;
import com.vedantu.organization.pojos.requests.microsite.AddMicrositeConfigReq;
import com.vedantu.organization.pojos.requests.microsite.GetOrgMicrositeConfigReq;
import com.vedantu.organization.pojos.requests.microsite.ValidateExternalURLReq;
import com.vedantu.organization.pojos.responses.microsite.GetOrgMicrositeRes;
import com.vedantu.organization.pojos.responses.organizations.CheckSlugRes;

public class Microsites extends AbstractVedantuController {

    public static Result getConfig() {

        Form<GetOrgMicrositeConfigReq> getOrgForm = Form.form(GetOrgMicrositeConfigReq.class)
                .bindFromRequest();
        if (getOrgForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getOrgForm))).toObjectNode());
        }
        GetOrgMicrositeConfigReq request = getOrgForm.get();
        if (ObjectIdUtils.hasInvalidId(request.orgId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        GetOrgMicrositeRes getOrgRes = null;
        try {
            getOrgRes = MicrositeManager.getConfig(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgRes).toObjectNode());
    }

    public static Result addToConfig() {

        Form<AddMicrositeConfigReq> getOrgForm = Form.form(AddMicrositeConfigReq.class)
                .bindFromRequest();
        if (getOrgForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getOrgForm))).toObjectNode());
        }
        AddMicrositeConfigReq request = getOrgForm.get();
        if (ObjectIdUtils.hasInvalidId(request.orgId)) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.INVALID_ID))
                    .toObjectNode());
        }
        GetOrgMicrositeRes getOrgRes = null;
        try {
            getOrgRes = MicrositeManager.addToConfig(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getOrgRes).toObjectNode());
    }

    public static Result checkURL() {

        Form<ValidateExternalURLReq> request = Form.form(ValidateExternalURLReq.class)
                .bindFromRequest();
        if (request.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(request))).toObjectNode());
        }
        ValidateExternalURLReq checkOrgSlugReq = request.get();
        CheckSlugRes checkOrgSlugRes = new CheckSlugRes();
        checkOrgSlugRes.available = URLUtils.isURLExist(checkOrgSlugReq.url);

        return ok(getResultResponse(checkOrgSlugRes).toObjectNode());
    }

}
