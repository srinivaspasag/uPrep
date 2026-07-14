package controllers;

import static controllers.Application.cache;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.libs.Codec;
import play.libs.F;
import play.mvc.With;
import uicom.response.JSONResponse;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

@With(Security.class)
public class Organizations extends AbstractUIController {



    public static void index() {
        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getOrganizations",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject orgs = ResponseUtil.checkResponse(getJSON(promise));
        render(orgs);
    }
//    public static void addOrganizationSubmit() {
//        String isEnc = params.get("isEnc");
//        if (isEnc != null && isEnc.equals("on")) {
//            String encLevel = Play.configuration.getProperty("ORG_ENCRYPTION_LEVEL");
//            params.put("encLevel", encLevel);
//        } else {
//            params.put("encLevel", "NA");
//        }
//        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
//                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/addOrganization",
//                null);
//        Logger.log4j.info("BEFORE AWAIT");
//        await(promise);
//        Logger.log4j.info("AFTER AWAIT");
//        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
//        flash.put("adminEmail", params.get("representative.email"));
//        try {
//            flash.put("errorCode", resp.getString("errorCode"));
//            flash.put("addedOrgId", resp.getJSONObject("result").getString("id"));
//        } catch (Exception e) {
//            Logger.log4j.error("Error in setting error code");
//        }
//        index();
//    }

    public static void approveOrg() {
        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/approveOrganization",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void viewAppCredential() {
        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/generateAppCredentials",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void restartAll() {
        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.EVENT_BUS_SERVICE_URL + "/EventBusProcessors/restartAll",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void updateSlug() {
        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/updateOrganizationSlug",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void updateStatus() {
        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/updateOrganizationStatus",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void updateOrganizationSharedSubjects() {
        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/updateOrganizationSharedSubjects",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void updateOrganizationClassroomConnectStatus() {
        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/updateOrganizationClassroomConnectStatus",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void updateOrganizationDownloadStatus() {
        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/updateOrganizationDownloadStatus",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void checkReferer() {
        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/checkReferer",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void updateReferer() {
        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/updateOrganizationReferer",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void organization() {
        JSONObject org = _getOrg(null);
        render(org);
    }

    protected static JSONObject _getMembers(Map<String, Object> allParams) {

        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/getMembers", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static void orgMembers(String orgId) {
        request.params.put("targetProfile", "MANAGER");
        request.params.put("start", "0");
        request.params.put("size", "10");
        JSONObject resp = _getMembers(null);
        render(resp);
    }

    public static void orgMembersTable() {
        JSONObject resp = _getMembers(null);
        render(resp);
    }

    public static void updateMember(Map<String, Object> allParams) {
        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/members/updateMember",
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void getDirectURL(Map<String, Object> allParams) {
        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.USER_SERVICE_URL + "/users/getDirectURL",
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    protected static JSONObject _getOrg(Map<String, Object> allParams) {
        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getOrganization",
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static void orgInvoices(String orgId) {
        JSONObject resp = UIComInvoices._getOrders(orgId);
        String includeFile = "UIComInvoices/orgInvoices.html";
        render(resp, includeFile);
    }

    //add org
    public static void addOrganization() {
        JSONObject plansResp = _getPlans();
        String randomId = Codec.UUID();
        String tncVersion = _getLatestTNC();
        render(plansResp, randomId, tncVersion);
    }

    private static String _getOrgServiceUrl() {
        return Play.configuration.getProperty("ORGANIZATION_SERVICE_URL");
    }

    private static JSONObject _doOrgCall(String url, Map<String, Object> allParams) {
        if (allParams == null) {
            allParams = getReqParams();
        }
        String appName = Play.configuration.getProperty("application.name");
        allParams.put("callingApp", appName);
        allParams.put("callingAppId", appName);

        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                _getOrgServiceUrl() + url,
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        return resp;
    }

    public static void submitOrg(String randomId, String code, String name, String fullName,
            String website, String emailDomain, String contactNumber, String type,
            String address, String description, String slug, String planId, String planName, String theme, boolean isNewUI) {
        if (cache.get(randomId) == null || !code.equals(cache.get(randomId))) {
            Logger.log4j.info("the code u entered " + code + " did not match "
                    + cache.get(randomId));
            JSONResponse resp = new JSONResponse(false, "Either captcha text not matching or time elapsed", "CAPTCHA_ERROR");
            JSONObject plansResp = _getPlans();
            String tncVersion = _getLatestTNC();
            render("Organization/add.html", plansResp, tncVersion, resp, randomId, name, fullName, slug, website, emailDomain, contactNumber, type, address, description, planId);
        } else {
            JSONObject resp = _doOrgCall("/organizations/addOrganization", null);
            try {
                if (resp != null && resp.has("errorCode") && !resp.getString("errorCode").isEmpty()) {
                    JSONObject plansResp = _getPlans();
                    String tncVersion = _getLatestTNC();
                    render("Organization/add.html", plansResp, tncVersion, resp, randomId, name, fullName, slug, website, emailDomain, contactNumber, type, address, description, planId);
                }
            } catch (JSONException ex) {
                JSONObject plansResp = _getPlans();
                String tncVersion = _getLatestTNC();
                JSONResponse respObj = new JSONResponse(false, "Something went wrong, try again later.", "UNKNOWN_ERROR");
                resp = new JSONObject(respObj);
                render("Organization/add.html", plansResp, tncVersion, resp, randomId, name, fullName, slug, website, emailDomain, contactNumber, type, address, description, planId);
            }
            //render(resp, name, fullName, slug, website, contactNumber, type, address, planName);
            index();
        }
    }

    public static void submitPage() {
        String name = "Vedantu";
        String fullName = "Vedantu";
        String website = "http://veantu.com";
        String contactNumber = "976458291";
        String type = "COLLEGE";
        String address = "address";
        String slug = "123";
        String planName = "plan 1";
        render("Organization/submitOrg.html", name, fullName, slug, website, contactNumber, type, address, planName);
    }

    public static void checkSlug() {
        JSONObject resp = _doOrgCall("/organizations/checkSlug", null);
        renderJSON(resp.toString());
    }

    private static JSONObject _getPlans() {
        request.params.put("state", "ACTIVE");
        return _doOrgCall("/licensing/getPlans", null);
    }

    private static JSONObject _getPlans(Map<String, Object> allParams) {
        return _doOrgCall("/licensing/getPlans", allParams);
    }

    private static String _getLatestTNC() {
        String tncVersion = "20131121";
        try {
            JSONObject resp = _doOrgCall("/organizations/getLatestTNC", null);
            if (resp != null && resp.has("result") && resp.getJSONObject("result").has("tncVersion")) {
                tncVersion = resp.getJSONObject("result").getString("tncVersion");
            }
        } catch (JSONException ex) {
            Logger.log4j.error("ERROR IN TNC FETCH ======= " + ex.getLocalizedMessage());
            tncVersion = "20131121";
        }
        return tncVersion;
    }

    public static void checkWebsite() {
        JSONObject resp = _doOrgCall("/organizations/checkWebsite", null);
        renderJSON(resp.toString());
    }

    public static void plans(String planId) {
        Map<String, Object> allParams = getReqParams();
        if (planId != null && !"all".equals(planId)) {
            allParams.put("planIds[0]", planId);
        } else {
            allParams.put("state", "ACTIVE");
        }
        JSONObject plansResp = _getPlans(allParams);
        render(plansResp);
    }

}
