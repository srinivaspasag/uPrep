package controllers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;

import org.apache.commons.lang3.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.Logger.ALogger;
import play.data.Form;
import play.mvc.Result;

import com.vedantu.billing.dao.OrderDAO;
import com.vedantu.billing.manager.PaymentManager;
import com.vedantu.billing.managers.OrderManager;
import com.vedantu.billing.models.Order;
import com.vedantu.billing.payment.managers.IPaymentManager;
import com.vedantu.billing.payment.managers.PaymentManagersFactory;
import com.vedantu.billing.pojos.requests.ApplyCouponReq;
import com.vedantu.billing.pojos.requests.ApplyLPCreditsReq;
import com.vedantu.billing.pojos.requests.ConfirmPaymentReq;
import com.vedantu.billing.pojos.requests.GetOrderItemInfoReq;
import com.vedantu.billing.pojos.requests.GetTransactionStatusReq;
import com.vedantu.billing.pojos.requests.PaymentReq;
import com.vedantu.billing.pojos.requests.StartTransactionReq;
import com.vedantu.billing.pojos.requests.UpdateTransactionReq;
import com.vedantu.billing.pojos.responses.ApplyCouponRes;
import com.vedantu.billing.pojos.responses.ApplyLPCreditsRes;
import com.vedantu.billing.pojos.responses.ConfirmPaymentRes;
import com.vedantu.billing.pojos.responses.GetOrderItemInfoRes;
import com.vedantu.billing.pojos.responses.GetTransactionStatusRes;
import com.vedantu.billing.pojos.responses.OnPaymentReceivedRes;
import com.vedantu.billing.pojos.responses.PaymentRes;
import com.vedantu.billing.pojos.responses.StartTransactionRes;
import com.vedantu.billing.pojos.responses.UpdateTransactionRes;
import com.vedantu.commons.VedantuErrorCode;
import com.vedantu.commons.VedantuException;
import com.vedantu.organization.daos.OrganizationDAO;
import com.vedantu.organization.models.Organization;

public class Payments extends AbstractVedantuController {

    private static final ALogger LOGGER = Logger.of(Payments.class);

    public static Result pay() {

        Form<PaymentReq> requestForm = Form.form(PaymentReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }

        PaymentReq request = requestForm.get();
        LOGGER.debug("request params : " + request + " realIp: " + request().getHeader("X-Real-IP")
                + ", remoteAddress: " + request().remoteAddress());
        PaymentRes res = new PaymentRes();
        try {
            Order order = OrderDAO.INSTANCE.getOrderById(request.orderId);
            if (request.paymentChannel.equals("INSTAMOJO")) {
                res.gatewayUrl = createInstamojoPaymentRequestFromPHP(request, order, request.orgId);
            } else {
                IPaymentManager paymentManager = PaymentManagersFactory.INSTANCE
                        .getPaymentManager(request.paymentChannel);
                res.gatewayUrl = paymentManager.getChargingRequestUrl(request.transactionId,
                        request.userId, request.item_sku, request.callbackUrl, request.email,
                        request.phone);
            }
            order.item_sku = request.item_sku;
            OrderDAO.INSTANCE.save(order);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return ok(getResultResponse(res).toObjectNode());
    }

    public static Result confirmPayment() {
        LOGGER.error("Divesh Entered confirm payment");
        Form<ConfirmPaymentReq> requestForm = Form.form(ConfirmPaymentReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        ConfirmPaymentReq request = requestForm.get();
        ConfirmPaymentRes response = null;
        try {

            response = OrderManager.confirmPayment(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            LOGGER.error("Divesh Entered confirm payment" + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
            LOGGER.error("Divesh Entered confirm payment" + e.getMessage());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    private static String createInstamojoPaymentRequest(PaymentReq request, Order order, String orgId)
            throws VedantuException, IOException, JSONException {

        String url = Play.application().configuration().getString("instamojo.charging.url");
        if(StringUtils.isEmpty(orgId)){
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }
        Organization org = OrganizationDAO.INSTANCE.getById(orgId);
        String apiKey = org.instaMojoApiKey;
        String authToken = org.instaMojoAuthToken;

        if(StringUtils.isEmpty(apiKey) || StringUtils.isEmpty(authToken)){
            apiKey = Play.application().configuration().getString("instamojo.apikey");
            authToken = Play.application().configuration().getString("instamojo.authtoken");
        }
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Setting basic post request
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("X-Api-Key", apiKey);
        con.setRequestProperty("X-Auth-Token", authToken);

        JSONObject payload = new JSONObject();
        payload.put("amount", String.valueOf((order.totalAmount - order.discount - order.lpCreditsRedeemed) / 100));
        payload.put("purpose", order.items.get(0).name);
        payload.put("buyer_name", request.buyer_name);
        payload.put("email", request.email);
        payload.put("phone", request.phone);
        payload.put("redirect_url", request.callbackUrl);
        payload.put("allow_repeated_payments", false);
        payload.put("send_email", true);
        payload.put("send_sms", true);

        String postJsonData = payload.toString();

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(postJsonData);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        LOGGER.debug("nSending 'POST' request to URL : " + url);
        LOGGER.debug("Post Data : " + postJsonData);
        LOGGER.debug("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String output;
        StringBuffer response = new StringBuffer();
        String gatewayUrl = StringUtils.EMPTY;

        while ((output = in.readLine()) != null) {
            response.append(output);
        }
        in.close();
        LOGGER.debug("response : " + response.toString());
        JSONObject payloadResp = new JSONObject(response.toString());
        if (payloadResp.getBoolean("success")) {
            gatewayUrl = payloadResp.getJSONObject("payment_request").getString("longurl");
            order.paymentChannelTransactionId = payloadResp.getJSONObject("payment_request").getString("id");
            order.paymentChannel = request.paymentChannel;
            order.pointOfSale = org.fullName;
            order.sellerReferenceNo = orgId;
            OrderDAO.INSTANCE.save(order);
        }
        LOGGER.debug("gatewayUrl : " + gatewayUrl);
        return gatewayUrl;
    }

    private static String createInstamojoPaymentRequestFromPHP(PaymentReq request, Order order, String orgId)
            throws VedantuException, IOException, JSONException {

        String instamojo_url = Play.application().configuration().getString("instamojo.charging.url");
        if(StringUtils.isEmpty(orgId)){
            throw new VedantuException(VedantuErrorCode.ORGANIZATION_NOT_FOUND);
        }
        Organization org = OrganizationDAO.INSTANCE.getById(orgId);
        String apiKey = org.instaMojoApiKey;
        String authToken = org.instaMojoAuthToken;

        if(StringUtils.isEmpty(apiKey) || StringUtils.isEmpty(authToken)){
            apiKey = Play.application().configuration().getString("instamojo.apikey");
            authToken = Play.application().configuration().getString("instamojo.authtoken");
        }
        String url = "http://apigateway.learnpedia.in/secure-api/index.php/instamojo-payment-request";
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // Setting basic post request
        con.setRequestMethod("POST");
        con.setRequestProperty("Content-Type", "application/json");
        con.setRequestProperty("X-Api-Key", apiKey);
        con.setRequestProperty("X-Auth-Token", authToken);

        JSONObject payload = new JSONObject();
        payload.put("instamojo_url", instamojo_url);
        payload.put("amount", String.valueOf((order.totalAmount - order.discount - order.lpCreditsRedeemed) / 100));
        //Send only programName in UPrep.
        if(request.orgId.equals("5df8a0d0e4b0897459b25d86")){
            String programName = order.items.get(0).name.split(", ")[0];
            payload.put("purpose", programName);
        }else{
            payload.put("purpose", order.items.get(0).name);
        }
        payload.put("buyer_name", request.buyer_name);
        payload.put("email", request.email);
        payload.put("phone", request.phone);
        payload.put("redirect_url", request.callbackUrl);
        payload.put("allow_repeated_payments", false);
        payload.put("send_email", true);
        payload.put("send_sms", true);

        String postJsonData = payload.toString();

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(postJsonData);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        LOGGER.debug("nSending 'POST' request to URL : " + url);
        LOGGER.debug("Post Data : " + postJsonData);
        LOGGER.debug("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
        String output;
        StringBuffer response = new StringBuffer();
        String gatewayUrl = StringUtils.EMPTY;

        while ((output = in.readLine()) != null) {
            response.append(output);
        }
        in.close();
        LOGGER.debug("response : " + response.toString());
        JSONObject payloadResp = new JSONObject(response.toString());
        if (payloadResp.getBoolean("success")) {
            gatewayUrl = payloadResp.getJSONObject("payment_request").getString("longurl");
            order.paymentChannelTransactionId = payloadResp.getJSONObject("payment_request").getString("id");
            order.paymentChannel = request.paymentChannel;
            order.pointOfSale = org.fullName;
            order.sellerReferenceNo = orgId;
            OrderDAO.INSTANCE.save(order);
        }
        LOGGER.debug("gatewayUrl : " + gatewayUrl);
        return gatewayUrl;
    }

    public static Result onPaymentReceived() {

        OnPaymentReceivedRes response = null;
        try {
            response = PaymentManager.onPaymentReceived(request().getHeader(REFERER),
                    getReqParams());
            if (StringUtils.isNotEmpty(response.callbackUrl)) {
                StringBuilder sb = new StringBuilder();
                sb.append("orderId=");
                sb.append(response.orderId);
                sb.append("&transactionId=");
                sb.append(response.transactionId);
                sb.append("&transactionStatus=");
                sb.append(response.transactionStatus);
                sb.append("&item_sku=");
                sb.append(URLEncoder.encode(response.item_sku, "UTF-8"));

                String callbackUrl = response.callbackUrl + "?" + sb.toString();
                LOGGER.debug("redirecting to " + callbackUrl);
                return redirect(callbackUrl);
            }
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        } catch (Throwable e) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.SERVICE_ERROR, e.getMessage()))
                    .toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result startTransaction() {

        Form<StartTransactionReq> requestForm = Form.form(StartTransactionReq.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        StartTransactionReq request = requestForm.get();
        StartTransactionRes response = null;
        try {

            response = OrderManager.startTransaction(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getTransactionStatus() {

        Form<GetTransactionStatusReq> requestForm = Form.form(GetTransactionStatusReq.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GetTransactionStatusReq request = requestForm.get();
        GetTransactionStatusRes response = null;
        try {
            response = OrderManager.getTransactionStatus(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result getOrderItemInfo() {

        Form<GetOrderItemInfoReq> requestForm = Form.form(GetOrderItemInfoReq.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        GetOrderItemInfoReq request = requestForm.get();
        GetOrderItemInfoRes response = null;
        try {
            response = OrderManager.getOrderItemInfo(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result applyCoupon() {

        Form<ApplyCouponReq> requestForm = Form.form(ApplyCouponReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        ApplyCouponReq request = requestForm.get();
        ApplyCouponRes response = null;
        try {
            response = OrderManager.applyCoupon(request);
        } catch (VedantuException e) {
            LOGGER.debug("Payments::: ApplyCoupon:: Got the exception");
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result applyLPCredits() {

        Form<ApplyLPCreditsReq> requestForm = Form.form(ApplyLPCreditsReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        ApplyLPCreditsReq request = requestForm.get();
        ApplyLPCreditsRes response = null;
        try {
            response = OrderManager.applyLPCredits(request);
        } catch (VedantuException e) {
            LOGGER.debug("Payments::: applyLPCredits:: Got the exception");
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    public static Result removeLPCredits() {

        Form<ApplyLPCreditsReq> requestForm = Form.form(ApplyLPCreditsReq.class).bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        ApplyLPCreditsReq request = requestForm.get();
        ApplyLPCreditsRes response = null;
        try {
            response = OrderManager.removeLPCredits(request);
        } catch (VedantuException e) {
            LOGGER.debug("Payments::: removeLPCredits:: Got the exception");
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }

    /**
     * NOTE: this method will only be called for GoogleWallet Server from
     * Android APP, in rest of the flow onPaymentReceive method will be called
     *
     * @return
     */
    public static Result updateTransaction() {

        Form<UpdateTransactionReq> requestForm = Form.form(UpdateTransactionReq.class)
                .bindFromRequest();
        if (requestForm.hasErrors()) {
            return ok(getErrorResponse(
                    new VedantuException(VedantuErrorCode.MISSING_PARAMETERS,
                            getErrorMessege(requestForm))).toObjectNode());
        }
        UpdateTransactionReq request = requestForm.get();
        UpdateTransactionRes response = null;
        try {
            response = OrderManager.updateTransaction(request);
        } catch (VedantuException e) {
            return ok(getErrorResponse(e).toObjectNode());
        }
        return ok(getResultResponse(response).toObjectNode());
    }
}
