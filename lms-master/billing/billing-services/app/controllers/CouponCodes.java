package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.billing.managers.CouponCodeManager;
import com.vedantu.billing.pojos.requests.couponcodes.AddCouponCodeReq;
import com.vedantu.billing.pojos.requests.couponcodes.DeleteCouponCodeReq;
import com.vedantu.billing.pojos.requests.couponcodes.GetCouponCodeReq;
import com.vedantu.billing.pojos.requests.couponcodes.GetCouponCodesReq;
import com.vedantu.billing.pojos.requests.couponcodes.UpdateCouponCodeReq;
import com.vedantu.billing.pojos.responses.couponcodes.AddCouponCodeRes;
import com.vedantu.billing.pojos.responses.couponcodes.GetCouponCodeRes;
import com.vedantu.billing.pojos.responses.couponcodes.GetCouponCodesRes;
import com.vedantu.billing.pojos.responses.couponcodes.UpdateCouponCodeRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.commons.pojos.responses.ActionTakenRes;

public class CouponCodes extends AbstractVedantuController {

    private static ALogger LOGGER = Logger.of(CouponCodes.class);

    public static Result addCouponCode() {
        Form<AddCouponCodeReq> addCouponCodeForm = Form.form(AddCouponCodeReq.class)
                .bindFromRequest();
        if (addCouponCodeForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(addCouponCodeForm))).toObjectNode());
        }
        AddCouponCodeReq addCouponCodeReq = addCouponCodeForm.get();
        AddCouponCodeRes addCouponCodeRes = null;
        try {
            addCouponCodeRes = CouponCodeManager.INSTANCE.addCouponCode(addCouponCodeReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(addCouponCodeRes).toObjectNode());
    }

    public static Result updateCouponCode() {
        Form<UpdateCouponCodeReq> updateCouponCodeForm = Form.form(UpdateCouponCodeReq.class)
                .bindFromRequest();
        if (updateCouponCodeForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(updateCouponCodeForm))).toObjectNode());
        }
        UpdateCouponCodeReq updateCouponCodeReq = updateCouponCodeForm.get();
        UpdateCouponCodeRes updateCouponCodeRes = null;
        try {
            updateCouponCodeRes = CouponCodeManager.INSTANCE.updateCouponCode(updateCouponCodeReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(updateCouponCodeRes).toObjectNode());
    }

    public static Result deleteCouponCode() {
        Form<DeleteCouponCodeReq> deleteCouponCodeForm = Form.form(DeleteCouponCodeReq.class)
                .bindFromRequest();
        if (deleteCouponCodeForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(deleteCouponCodeForm))).toObjectNode());
        }
        DeleteCouponCodeReq deleteCouponCodeReq = deleteCouponCodeForm.get();
        ActionTakenRes deleteCouponCodeRes = new ActionTakenRes();
        try {
            deleteCouponCodeRes = CouponCodeManager.INSTANCE.deleteCouponCode(deleteCouponCodeReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(deleteCouponCodeRes).toObjectNode());
    }

    public static Result getActiveCodes() {
        Form<GetCouponCodesReq> getCouponCodesForm = Form.form(GetCouponCodesReq.class)
                .bindFromRequest();
        if (getCouponCodesForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getCouponCodesForm))).toObjectNode());
        }
        GetCouponCodesReq getCouponCodesReq = getCouponCodesForm.get();
        GetCouponCodesRes getCouponCodesRes = null;
        try {
            getCouponCodesRes = CouponCodeManager.INSTANCE.getCouponCodes(getCouponCodesReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getCouponCodesRes).toObjectNode());
    }

    public static Result getCouponCode() {
        Form<GetCouponCodeReq> getCouponCodeForm = Form.form(GetCouponCodeReq.class)
                .bindFromRequest();
        if (getCouponCodeForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getCouponCodeForm))).toObjectNode());
        }
        GetCouponCodeReq getCouponCodeReq = getCouponCodeForm.get();
        GetCouponCodeRes getCouponCodeRes = null;
        try {
            getCouponCodeRes = CouponCodeManager.INSTANCE.getCouponCode(getCouponCodeReq);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(getCouponCodeRes).toObjectNode());
    }
}
