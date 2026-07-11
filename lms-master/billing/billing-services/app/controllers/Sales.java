package controllers;

import play.Logger;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.billing.manager.SaleDetailsManager;
import com.vedantu.billing.pojos.requests.saledetails.GetSaleDetailsListReq;
import com.vedantu.billing.pojos.responses.saledetails.GetSaleDetailsListRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;

public class Sales extends AbstractVedantuController {
    private static ALogger LOGGER = Logger.of(CouponCodes.class);

    public static Result getSaleDetailsDisplayInfo() {
        Form<GetSaleDetailsListReq> getSaleDetailsForm = Form.form(GetSaleDetailsListReq.class)
                .bindFromRequest();
        if (getSaleDetailsForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(getSaleDetailsForm))).toObjectNode());
        }
        GetSaleDetailsListReq getSaleDetailsListReq = getSaleDetailsForm.get();
        GetSaleDetailsListRes getSaleDetailsListRes = null;
        getSaleDetailsListRes = SaleDetailsManager.INSTANCE.getSaleDetails(getSaleDetailsListReq);
        return ok(getResultResponse(getSaleDetailsListRes).toObjectNode());
    }
}
