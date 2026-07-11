/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.data.validation.Required;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Scope.Params;
import play.mvc.With;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

/**
 *
 * @author anirban
 */
@With(Security.class)
public class UIComInvoices extends AbstractUIController {

    public static void orgInvoices(@Required String orgId) {
        render();
    }

    protected static JSONObject _getOrders(String orgId) {
        Params allParams = request.params;
        String startStr = allParams.get("start");
        if (startStr == null || StringUtils.isEmpty(startStr)) {
            allParams.put("start", "0");
        }
        allParams.put("orgId", orgId);
        allParams.put("customer.id", orgId);
        allParams.put("customer.type", "ORGANIZATION");
        allParams.put("size", "12");
        F.Promise<JSONResponseWrapper> promise = client(ClientUtil.BILLING_WEB_SERVICE_URL
                + "/invoices/getBuyOrders", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    protected static JSONObject _getOrder(Map<String,Object> allParams) {
        F.Promise<JSONResponseWrapper> promise = client(ClientUtil.BILLING_WEB_SERVICE_URL
                + "/invoices/getOrder", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static void generateMonthlyInvoices(String orgId) {
        Map<String, Object> allParams = getReqParams();
        allParams.put("orgId", orgId);
        F.Promise<JSONResponseWrapper> promise = client(ClientUtil.BILLING_WEB_SERVICE_URL
                + "/invoices/generate", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        try {
            String errorCode = resp.getString("errorCode");
            if (StringUtils.isEmpty(errorCode)) {
                JSONObject result = resp.getJSONObject("result");
                JSONArray list = result.getJSONArray("orderInfos");
                if (list.length() > 0) {
                    result.put("list", list);
                    result.put("totalHits", list.length());
                    boolean checkForLoadMore = false;
                    render("UIComInvoices/invoiceList.html", resp, checkForLoadMore);
                    return;
                }
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex);
        }
        renderJSON(resp.toString());
    }

    public static void loadOrders(String orgId) {
        JSONObject resp = _getOrders(orgId);
        render("UIComInvoices/invoiceList.html", resp);
    }

    public static void invoicePage(String orgId,String orderId) {
        request.params.put("orderId", orderId);
        JSONObject resp = _getOrder(null);
        JSONObject currentOrgInfo = _getOrgInfo(orgId);
        render("tags/UIComInvoicing/invoice.html", resp, currentOrgInfo);
    }
    private static JSONObject _getOrgInfo(@Required String orgId) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getOrganizationInfoForInvoice", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject instituteResponse = getJSON(promise);
        instituteResponse = ResponseUtil.checkResponse(instituteResponse);
        flash.keep(UIComSecurity.redirectUrlKey);
        return instituteResponse;
    }

    public static void testPage(String orgId,String orderId) {
        request.params.put("orderId", orderId);
        JSONObject resp = _getOrder(null);
        render("tags/UIComInvoicing/test.html", resp);
    }

//    return methods
    protected static JSONObject _getBuyOrders(Map<String, Object> allParams) {
        F.Promise<JSONResponseWrapper> promise = client(ClientUtil.BILLING_WEB_SERVICE_URL
                + "/invoices/getBuyOrders", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    protected static JSONObject _getSellOrders(Map<String, Object> allParams) {
        F.Promise<JSONResponseWrapper> promise = client(ClientUtil.BILLING_WEB_SERVICE_URL
                + "/invoices/getSellOrders", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
}
