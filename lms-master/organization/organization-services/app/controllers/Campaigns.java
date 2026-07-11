package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.organization.managers.CampaignManager;
import com.vedantu.organization.pojos.requests.campaigns.AddCampaignReq;
import com.vedantu.organization.pojos.requests.campaigns.DeleteCampaignReq;
import com.vedantu.organization.pojos.requests.campaigns.GetCampaignReq;
import com.vedantu.organization.pojos.requests.campaigns.GetCampaignsReq;
import com.vedantu.organization.pojos.requests.campaigns.UpdateCampaignReq;
import com.vedantu.organization.pojos.responses.campaigns.AddCampaignRes;
import com.vedantu.organization.pojos.responses.campaigns.DeleteCampaignRes;
import com.vedantu.organization.pojos.responses.campaigns.GetCampaignRes;
import com.vedantu.organization.pojos.responses.campaigns.GetCampaignsRes;
import com.vedantu.organization.pojos.responses.campaigns.UpdateCampaignRes;

public class Campaigns extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(Organizations.class);

    public static Result addCampaign() {

        Form<AddCampaignReq> addCampaignForm = Form.form(AddCampaignReq.class).bindFromRequest();
        if (addCampaignForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        AddCampaignReq addCampaignReq = addCampaignForm.get();
        AddCampaignRes addCampaignRes = null;

        try {
            addCampaignRes = CampaignManager.addCampaign(addCampaignReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(addCampaignRes).toObjectNode());
    }

    public static Result deleteCampaign() {

        Form<DeleteCampaignReq> deleteCampaignForm = Form.form(DeleteCampaignReq.class)
                .bindFromRequest();
        if (deleteCampaignForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        DeleteCampaignReq deleteCampaignReq = deleteCampaignForm.get();
        DeleteCampaignRes deleteCampaignRes = null;

        try {
            deleteCampaignRes = CampaignManager.deleteCampaign(deleteCampaignReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(deleteCampaignRes).toObjectNode());
    }

    public static Result updateCampaign() {

        Form<UpdateCampaignReq> udpateCampaignForm = Form.form(UpdateCampaignReq.class)
                .bindFromRequest();
        if (udpateCampaignForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        UpdateCampaignReq updateCampaignReq = udpateCampaignForm.get();
        UpdateCampaignRes updateCampaignRes = null;

        updateCampaignRes = CampaignManager.updateCampaign(updateCampaignReq);

        return ok(getResultResponse(updateCampaignRes).toObjectNode());
    }

    public static Result getCampaign() {

        Form<GetCampaignReq> getCampaignForm = Form.form(GetCampaignReq.class).bindFromRequest();
        if (getCampaignForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetCampaignReq getCampaignReq = getCampaignForm.get();
        GetCampaignRes getCampaignRes = null;

        try {
            getCampaignRes = CampaignManager.getCampaign(getCampaignReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(getCampaignRes).toObjectNode());
    }

    public static Result getCampaigns() {
        Form<GetCampaignsReq> getCampaignsForm = Form.form(GetCampaignsReq.class).bindFromRequest();
        if (getCampaignsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetCampaignsReq getCampaignsReq = getCampaignsForm.get();
        GetCampaignsRes getCampaignsRes = null;
        getCampaignsRes = CampaignManager.getCampaigns(getCampaignsReq);
        return ok(getResultResponse(getCampaignsRes).toObjectNode());
    }
}
