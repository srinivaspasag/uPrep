package controllers;

import play.Logger;
import play.libs.F;
import play.libs.WS;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONObject;

import static play.libs.F.Promise;
import static play.libs.WS.HttpResponse;

public class QrReferrals extends AbstractQRUIController {

    public static void referralUsers() {
        render();
    }

    public static void getReferredUsersData() {
        Map<String, Object> allParams = getReqParams();

        final Promise<HttpResponse> promise = WS
                .url(ClientUtil.ORGANIZATION_SERVICE_URL + "/members/getReferredUsersData")
                .params(allParams).headers(new HashMap<String, String>()).timeout("5min")
                .getAsync();
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        try {
            HttpResponse httpResponse = promise.get();
            String contentType = httpResponse.getContentType();
            Logger.log4j.info("Content Type: " + contentType);
            if ("text/csv".equals(contentType)) {
                InputStream stream = httpResponse.getStream();
                renderBinary(stream, "usersData.csv", false);
            } else {
                renderText(httpResponse.getString());
            }
            Logger.log4j.info("Response: " + httpResponse.getString());
        } catch (Exception e) {
            Logger.log4j.error(e);
        }
    }

    public static void direct() {
        JSONObject orgInfo = _getOrgInfo(null);
        String includeName = "QrReferrals/referralUsers.html";
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render("Application/mapper.html", includeName, orgInfo, currentOrgInfo);
    }

    protected static JSONObject _getOrgInfo(Map<String, Object> allParams) {

        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getOrganization", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
}
