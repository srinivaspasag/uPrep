package controllers;

import java.util.Map;

import org.json.JSONObject;

import play.Logger;
import play.libs.F;
import play.mvc.With;
import util.ClientUtil;
import util.ResponseUtil;
import controllers.AbstractUIController.JSONResponseWrapper;

@With(Security.class)
public class Invoices extends AbstractUIController{

    static final String className = Invoices.class.getSimpleName();

    public static void invoicePage(String orgId,String orderId) {
        orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        request.params.put("orderId", orderId);
        JSONObject myOrgInfo = Institute._setOrgParams(orgId);
        JSONObject resp = _getOrder(null);
        render(renderTheme(orgId, getHTMLFilePath(className,"invoice")), resp,myOrgInfo);
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
}
