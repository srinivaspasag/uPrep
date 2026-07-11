package controllers;

import play.data.Form;
import play.mvc.Result;

import com.vedantu.billing.manager.InvoiceManager;
import com.vedantu.billing.managers.OrderManager;
import com.vedantu.billing.pojos.OrderInfo;
import com.vedantu.billing.pojos.requests.GenerateInvoiceReq;
import com.vedantu.billing.pojos.requests.GenerateOrderInfoReq;
import com.vedantu.billing.pojos.requests.GetBuyOrdersReq;
import com.vedantu.billing.pojos.requests.GetSellOrdersReq;
import com.vedantu.billing.pojos.responses.GenerateInvoiceRes;
import com.vedantu.billing.pojos.responses.GetOrdersRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;

public class Invoices extends AbstractVedantuController {

    public static Result generate() {

        Form<GenerateInvoiceReq> requestForm = Form.form(GenerateInvoiceReq.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GenerateInvoiceReq request = requestForm.get();
        GenerateInvoiceRes response = null;
        try {

            response = InvoiceManager.generateInvoices(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getBuyOrders() {

        Form<GetBuyOrdersReq> requestForm = Form.form(GetBuyOrdersReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }

        GetBuyOrdersReq request = requestForm.get();
        GetOrdersRes response = null;
        try {
            response = OrderManager.getOrders(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    /**
     * 
     * @return will return all the orders for which request.customer is seller
     */
    public static Result getSellOrders() {

        Form<GetSellOrdersReq> requestForm = Form.form(GetSellOrdersReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }

        GetSellOrdersReq request = requestForm.get();
        GetOrdersRes response = null;
        try {
            response = OrderManager.getItemOrders(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getOrder() {

        Form<GenerateOrderInfoReq> requestForm = Form.form(GenerateOrderInfoReq.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }

        GenerateOrderInfoReq request = requestForm.get();
        OrderInfo response = null;
        try {
            response = OrderManager.getOrderInfo(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }
}
