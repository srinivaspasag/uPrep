package controllers;

import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.organization.managers.CampaignCodeManager;
import com.vedantu.organization.pojos.requests.AddCampaignCodeReq;
import com.vedantu.organization.pojos.requests.ApplyCampaignCodeReq;
import com.vedantu.organization.pojos.requests.CreateBulkCampaignCodeReq;
import com.vedantu.organization.pojos.requests.GetCampaignCodeReq;
import com.vedantu.organization.pojos.responses.AddCampaignCodeRes;
import com.vedantu.organization.pojos.responses.ApplyCampaignCodeRes;
import com.vedantu.organization.pojos.responses.CreateBulkCampaignCodeRes;
import com.vedantu.organization.pojos.responses.GetCampaignCodeRes;
import com.vedantu.organization.pojos.responses.ValidateCampaignCodeRes;

public class CampaignCodes extends AbstractVedantuController {

    public static Result addCampaignCode() throws VedantuException {

        Form<AddCampaignCodeReq> addCampaignCodeForm = Form.form(AddCampaignCodeReq.class)
                .bindFromRequest();
        if (addCampaignCodeForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        AddCampaignCodeReq addCampaignCodeReq = addCampaignCodeForm.get();
        AddCampaignCodeRes addCampaignCodeRes = CampaignCodeManager
                .addCampaignCode(addCampaignCodeReq);

        return ok(getResultResponse(addCampaignCodeRes).toObjectNode());
    }

    public static Result createBulkCampaignCodes() throws VedantuException {

        Form<CreateBulkCampaignCodeReq> createBulkCampaignCodesForm = Form.form(
                CreateBulkCampaignCodeReq.class).bindFromRequest();
        if (createBulkCampaignCodesForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        CreateBulkCampaignCodeReq createBulkCampaignCodeReq = createBulkCampaignCodesForm.get();
        CreateBulkCampaignCodeRes createBulkCampaignCodeRes = CampaignCodeManager
                .createBulkCampaignCodes(createBulkCampaignCodeReq);

        return ok(getResultResponse(createBulkCampaignCodeRes).toObjectNode());
    }

    public static Result getCampaignCode() throws VedantuException {

        Form<GetCampaignCodeReq> getCampaignCodeForm = Form.form(GetCampaignCodeReq.class)
                .bindFromRequest();
        if (getCampaignCodeForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetCampaignCodeReq getCampaignCodeReq = getCampaignCodeForm.get();
        GetCampaignCodeRes getCampaignCodeRes = CampaignCodeManager
                .getCampaignCode(getCampaignCodeReq);

        return ok(getResultResponse(getCampaignCodeRes).toObjectNode());
    }

    public static Result isValidCampaignCode() throws VedantuException {

        Form<GetCampaignCodeReq> validateCampaignCodeForm = Form.form(GetCampaignCodeReq.class)
                .bindFromRequest();
        if (validateCampaignCodeForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetCampaignCodeReq validateCampaignCodeReq = validateCampaignCodeForm.get();
        ValidateCampaignCodeRes validateCampaignCodeRes = CampaignCodeManager
                .isValidCampaignCode(validateCampaignCodeReq);

        return ok(getResultResponse(validateCampaignCodeRes).toObjectNode());
    }

    public static Result applyCampaignCode() throws VedantuException {

        Form<ApplyCampaignCodeReq> applyCampaignCodeForm = Form.form(ApplyCampaignCodeReq.class)
                .bindFromRequest();
        if (applyCampaignCodeForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        ApplyCampaignCodeReq applyCampaignCodeReq = applyCampaignCodeForm.get();
        ApplyCampaignCodeRes applyCampaignCodeRes = CampaignCodeManager
                .applyCampaignCode(applyCampaignCodeReq);

        return ok(getResultResponse(applyCampaignCodeRes).toObjectNode());
    }
}
