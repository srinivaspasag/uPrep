package controllers;

import org.apache.commons.lang3.mutable.MutableLong;

import play.data.Form;
import play.mvc.Result;

import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.ActionTakenRes;
import com.vedantu.mongo.MongoManager;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.managers.LicensingManager;
import com.vedantu.organization.pojos.requests.licensing.AddLicensingPlanReq;
import com.vedantu.organization.pojos.requests.licensing.DeleteLicensingPlanReq;
import com.vedantu.organization.pojos.requests.licensing.GetLicensingPlansReq;
import com.vedantu.organization.pojos.requests.licensing.MarkStateReq;
import com.vedantu.organization.pojos.responses.licensing.AvailablePlansRes;
import com.vedantu.organization.pojos.responses.licensing.SupportedFeaturesRes;

public class Licensing extends AbstractVedantuController {

    public static Result getSupportedFeatures() {

        SupportedFeaturesRes response = null;
        response = LicensingManager.getAllSupportedFeatures();

        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getPlans() {

        Form<GetLicensingPlansReq> requestForm = Form.form(GetLicensingPlansReq.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GetLicensingPlansReq request = requestForm.get();

        AvailablePlansRes response = null;
        try {
            response = LicensingManager.getPlans(request.planIds, request.state);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result create() {

        Form<AddLicensingPlanReq> requestForm = Form.form(AddLicensingPlanReq.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        AddLicensingPlanReq request = requestForm.get();
        AvailablePlansRes response = null;
        try {
            response = LicensingManager.createPlan(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result mark() {

        Form<MarkStateReq> requestForm = Form.form(MarkStateReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        MarkStateReq request = requestForm.get();
        ActionTakenRes response = new ActionTakenRes();
        try {

            response = LicensingManager.mark(request.planId, request.state);

        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result delete() {

        Form<DeleteLicensingPlanReq> requestForm = Form.form(DeleteLicensingPlanReq.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        DeleteLicensingPlanReq request = requestForm.get();
        ActionTakenRes response = new ActionTakenRes();
        try {
            MutableLong totalHits = new MutableLong();
            OrganizationDAO.INSTANCE.getByPlanId(request.id, MongoManager.NO_START,
                    MongoManager.NO_LIMIT, totalHits);
            if (totalHits.longValue() != 0) {
                throw new VedantuException(VedantuErrorCode.CAN_NOT_BE_REMOVED);
            }
            response = LicensingManager.delelePlan(request.id);

        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }

        return ok(getResultResponse(response).toObjectNode());
    }
    public static Result update(){
    	return null;
    }

}
