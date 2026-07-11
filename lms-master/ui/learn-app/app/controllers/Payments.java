package controllers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.libs.F.Promise;
import play.mvc.With;
import util.ClientUtil;
import util.ResponseUtil;
import controllers.AbstractUIController.JSONResponseWrapper;

@With(Security.class)
public class Payments extends AbstractUIController {
    static final String className = Payments.class.getSimpleName();

    public static void startTransactionUser() throws JSONException {
        Map<String, Object> allParams = getReqParams();
        Logger.log4j.info("---Payments::startTransactionUser---");
        String userId = session.get("userId");
        Logger.log4j.info("userId:" + userId);
        allParams.put("customer.id", userId);
        allParams.put("customer.type", "USER");
        allParams.put("deviceType", "WEB");
        JSONObject resp = _recordTransaction(allParams);
        Logger.log4j.info("RESP:::" + resp);
        String errorCode = resp.getString("errorCode");
        if (StringUtils.isEmpty(errorCode)) {
            JSONObject result = resp.getJSONObject("result");
            JSONObject newResult = new JSONObject();
            String redirectURL = result.getString("paymentUrl");
            newResult.put("redirectUrl", redirectURL);
            newResult.put("transactionId", result.getString("transactionId"));
            newResult.put("orderId", result.getString("orderId"));
            newResult.put("orderTotal", result.getString("orderTotal"));
            newResult.put("userId", userId);
            newResult.put("email", result.getString("email"));
            newResult.put("phone", result.getString("phone"));
            newResult.put("needEmail", result.getString("needEmail"));
            newResult.put("paymentChannel", result.getString("paymentChannel"));
            resp.put("result", newResult);
        }
        renderJSON(resp.toString());
    }

    public static void validateAndApplyCoupon() throws JSONException {
        Map<String, Object> allParams = getReqParams();
        Logger.log4j.info("---UIComPayments::validateAndApplyCoupon---");
        String userId = session.get("userId");
        Logger.log4j.info("userId:" + userId);
        allParams.put("customer.id", userId);
        allParams.put("customer.type", "USER");
        allParams.put("deviceType", "WEB");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.BILLING_WEB_SERVICE_URL
                + "/payments/applyCoupon", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        Logger.log4j.info("RESP:::" + resp);
        renderJSON(resp.toString());
    }

    public static void validateAndApplyWalletBalance() throws JSONException {
        Map<String, Object> allParams = getReqParams();
        Logger.log4j.info("---UIComPayments::validateAndApplyWalletBalance---");
        String userId = session.get("userId");
        Logger.log4j.info("userId:" + userId);
        allParams.put("customer.id", userId);
        allParams.put("customer.type", "USER");
        allParams.put("deviceType", "WEB");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.BILLING_WEB_SERVICE_URL
                + "/payments/applyLPCredits", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        Logger.log4j.info("RESP:::" + resp);
        renderJSON(resp.toString());
    }

    public static void validateAndRemoveWalletBalance() throws JSONException {
        Map<String, Object> allParams = getReqParams();
        Logger.log4j.info("---UIComPayments::validateAndRemoveWalletBalance---");
        String userId = session.get("userId");
        Logger.log4j.info("userId:" + userId);
        allParams.put("customer.id", userId);
        allParams.put("customer.type", "USER");
        allParams.put("deviceType", "WEB");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.BILLING_WEB_SERVICE_URL
                + "/payments/removeLPCredits", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        Logger.log4j.info("RESP:::" + resp);
        renderJSON(resp.toString());
    }

    private static JSONObject _recordTransaction(Map<String, Object> allParams) {
        Logger.log4j.info("URL is ---");
        Logger.log4j.info(ClientUtil.BILLING_WEB_SERVICE_URL);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.BILLING_WEB_SERVICE_URL
                + "/payments/startTransaction", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static void showTransactionConfirmPopup() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        render(renderTheme(orgId, getHTMLFilePath(className,"showTransactionConfirmPopup")));
    }
}
