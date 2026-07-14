package controllers;

import java.util.List;

import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.organization.managers.SalesCampaignManager;
import com.vedantu.organization.models.SalesCampaign;
import com.vedantu.organization.pojos.requests.AddSalesCampaignReq;
import com.vedantu.organization.pojos.requests.DeleteSalesCampaignReq;
import com.vedantu.organization.pojos.requests.GetSalesCampaignReq;
import com.vedantu.organization.pojos.requests.GetSalesCampaignsReq;
import com.vedantu.organization.pojos.requests.UpdateSalesCampaignReq;
import com.vedantu.organization.pojos.responses.AddSalesCampaignRes;
import com.vedantu.organization.pojos.responses.DeleteSalesCampaignRes;
import com.vedantu.organization.pojos.responses.UpdateSalesCampaignRes;

public class SalesCampaigns extends AbstractVedantuController {

    public static Result addSalesCampaign() throws VedantuException {

        Form<AddSalesCampaignReq> addSalesCampaignForm = Form.form(AddSalesCampaignReq.class)
                .bindFromRequest();
        if (addSalesCampaignForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        AddSalesCampaignReq addSalesCampaignReq = addSalesCampaignForm.get();
        AddSalesCampaignRes addSalesCampaignRes = SalesCampaignManager
                .addSalesCampaign(addSalesCampaignReq);

        return ok(getResultResponse(addSalesCampaignRes).toObjectNode());
    }

    public static Result getSalesCampaign() throws VedantuException {

        Form<GetSalesCampaignReq> getSalesCampaignForm = Form.form(GetSalesCampaignReq.class)
                .bindFromRequest();
        if (getSalesCampaignForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetSalesCampaignReq getSalesCampaignReq = getSalesCampaignForm.get();
        SalesCampaign getSalesCampaignRes = SalesCampaignManager
                .getSalesCampaign(getSalesCampaignReq);

        return ok(getResultResponse(getSalesCampaignRes).toObjectNode());
    }

    public static Result getSalesCampaigns() throws VedantuException {

        Form<GetSalesCampaignsReq> getSalesCampaignsForm = Form.form(GetSalesCampaignsReq.class)
                .bindFromRequest();
        if (getSalesCampaignsForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        GetSalesCampaignsReq getSalesCampaignsReq = getSalesCampaignsForm.get();
        List<SalesCampaign> getSalesCampaignsRes = SalesCampaignManager
                .getSalesCampaigns(getSalesCampaignsReq);

        return ok(getResultResponse(getSalesCampaignsRes).toObjectNode());
    }

    public static Result updateSalesCampaign() throws VedantuException {

        Form<UpdateSalesCampaignReq> updateSalesCampaignForm = Form.form(
                UpdateSalesCampaignReq.class).bindFromRequest();
        if (updateSalesCampaignForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        UpdateSalesCampaignReq updateSalesCampaignReq = updateSalesCampaignForm.get();
        UpdateSalesCampaignRes updateSalesCampaignRes = SalesCampaignManager
                .updateSalesCampaign(updateSalesCampaignReq);

        return ok(getResultResponse(updateSalesCampaignRes).toObjectNode());
    }

    public static Result deleteSalesCampaign() throws VedantuException {

        Form<DeleteSalesCampaignReq> deleteSalesCampaignForm = Form.form(
                DeleteSalesCampaignReq.class).bindFromRequest();
        if (deleteSalesCampaignForm.hasErrors()) {
            return ok(getErrorResponse(new VedantuException(VedantuErrorCode.MISSING_PARAMETERS))
                    .toObjectNode());
        }
        DeleteSalesCampaignReq deleteSalesCampaignReq = deleteSalesCampaignForm.get();
        DeleteSalesCampaignRes updateSalesCampaignRes = SalesCampaignManager
                .deleteSalesCampaign(deleteSalesCampaignReq);

        return ok(getResultResponse(updateSalesCampaignRes).toObjectNode());
    }

}
