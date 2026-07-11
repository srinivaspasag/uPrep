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
import play.i18n.Messages;
import play.libs.F.Promise;
import play.mvc.Scope.Params;
import play.mvc.With;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

/**
 *
 * @author anirban
 */
@With(Security.class)
public class QrExtSignup extends AbstractQRUIController {
    private static JSONArray  CURRENCY_ARRAY   = null;
    private static JSONObject DEFAULT_CURRENCY = uicom.util.Utilities._getDefaultCurrency();

    private static JSONArray _getCurrencyArray() {
        if (CURRENCY_ARRAY == null) {
            CURRENCY_ARRAY = uicom.util.Utilities._getCurrencyList();
        }
        return CURRENCY_ARRAY;
    }

    public static JSONObject _getAllOrgPrograms(String orgId) {
        Params allParams = request.params;
        allParams.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getPrograms", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static void getProgramSections(String orgId, String programId) {
        Params allParams = request.params;
        allParams.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getProgramInfo", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        JSONObject currency = DEFAULT_CURRENCY;
        render(resp, currency);
    }

    public static void getProgramSectionsJSON(String orgId, String programId) {
        Params allParams = request.params;
        allParams.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getProgramInfo", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void home(String orgId) {
        recordActivity(ClientUtil.ActivityPages.EXT_SIGNUP, ClientUtil.ActivityAction.OPEN);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if (Widgets._amISuperAdmin(currentOrgInfo)) {
            JSONObject resp = _getAllOrgPrograms(orgId);
            JSONArray currencyArray = _getCurrencyArray();
            render(resp, currencyArray);
        } else {
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            render("UIComRegister/msgPage.html", msg);
        }
    }

    public static void direct(String orgId) {
        recordActivity(ClientUtil.ActivityPages.EXT_SIGNUP, ClientUtil.ActivityAction.OPEN);
        Map<String, Object> allParams = getReqParams();
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        JSONObject orgInfo = QrAcadStr._getOrgInfo(allParams);
        if (Widgets._amISuperAdmin(currentOrgInfo)) {
            JSONObject resp = _getAllOrgPrograms(orgId);
            JSONArray currencyArray = _getCurrencyArray();
            String includeName = "QrExtSignup/home.html";
            flash.put("ENTRY", "DIRECT");
            render("Application/mapper.html", includeName, resp, currencyArray, currentOrgInfo,
                    orgInfo);
        } else {
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            String includeName = "UIComRegister/msgPage.html";
            flash.put("ENTRY", "DIRECT");
            render("Application/mapper.html", includeName, msg, currentOrgInfo, orgInfo);
        }
    }

    public static void customizeSignup(String orgId) {
        recordActivity(ClientUtil.ActivityPages.EXT_SIGNUP, ClientUtil.ActivityAction.OPEN);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if (Widgets._amISuperAdmin(currentOrgInfo)) {
            JSONArray resp = _getCustomSignupFields();
            JSONArray additional = new JSONArray();
            render(resp, additional);
        } else {
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            render("UIComRegister/msgPage.html", msg);
        }
    }

    public static void customizeSignupDirect(String orgId) {
        recordActivity(ClientUtil.ActivityPages.EXT_SIGNUP, ClientUtil.ActivityAction.OPEN);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        JSONObject orgInfo = QrAcadStr._getOrgInfo(null);
        if (Widgets._amISuperAdmin(currentOrgInfo)) {
            JSONArray resp = _getCustomSignupFields();
            JSONArray additional = new JSONArray();
            String includeName = "QrExtSignup/customizeSignup.html";
            flash.put("ENTRY", "DIRECT");
            render("Application/mapper.html", includeName, resp, additional, currentOrgInfo,
                    orgInfo);
        } else {
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            String includeName = "UIComRegister/msgPage.html";
            flash.put("ENTRY", "DIRECT");
            render("Application/mapper.html", includeName, msg, currentOrgInfo, orgInfo);
        }
    }

    public static void submitCustomSignup() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/updateOrgMemberExtraInputFields", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static JSONArray _getCustomSignupFields() {
        request.params.put("targetOrgMemberProfile", "STUDENT");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getOrgMemberExtraInputFields", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        JSONArray array = null;
        try {
            if (StringUtils.isEmpty(resp.getString("errorCode"))) {
                array = resp.getJSONObject("result").getJSONArray("fields");
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
            array = null;
        }
        return array;
    }

    // Manage Digital Library
    public static void manageDigitalLibrary(String orgId) {
        recordActivity(ClientUtil.ActivityPages.EXT_SIGNUP, ClientUtil.ActivityAction.OPEN);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if (Widgets._amISuperAdmin(currentOrgInfo)) {
            JSONArray resp = _getDigitalLibraryFields(orgId);
            JSONArray additional = new JSONArray();
            render(resp, additional);
        } else {
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            render("UIComRegister/msgPage.html", msg);
        }
    }

    public static void manageDigitalLibraryDirect(String orgId) {
        recordActivity(ClientUtil.ActivityPages.EXT_SIGNUP, ClientUtil.ActivityAction.OPEN);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        JSONObject orgInfo = QrAcadStr._getOrgInfo(null);
        if (Widgets._amISuperAdmin(currentOrgInfo)) {
            JSONArray resp = _getDigitalLibraryFields(orgId);
            String includeName = "QrExtSignup/manageDigitalLibrary.html";
            flash.put("ENTRY", "DIRECT");
            render("Application/mapper.html", includeName, resp, currentOrgInfo, orgInfo);
        } else {
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            String includeName = "UIComRegister/msgPage.html";
            flash.put("ENTRY", "DIRECT");
            render("Application/mapper.html", includeName, msg, currentOrgInfo, orgInfo);
        }
    }

    public static JSONArray _getDigitalLibraryFields(String orgId) {
        request.params.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getDigitalLibraryFields", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        JSONArray array = null;
        try {
            if (StringUtils.isEmpty(resp.getString("errorCode"))) {
                array = resp.getJSONObject("result").getJSONArray("fields");
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
            array = null;
        }
        return array;
    }

    public static void submitDigitalLibraryFields() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/updateDigitalLibraryFields", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    // CATEGORY SERVER POINTS
    public static void addCategory() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/addCategory", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void removeCategory() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/removeCategory", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void getCategories() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getCategories", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void editCategories() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/editCategories", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void updateSectionAccess() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/updateSectionAccess", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void getPackageInfo(String orgId, String programId, String sectionId) {
        Params allParams = request.params;
        allParams.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getSectionPackageInfo", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render(resp);
    }

    public static void updatePackageInfo() {
        Params allParams = request.params;
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/updatePackageInfo", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void updateMaxDiscount() {
        Params allParams = request.params;
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/updateSectionMaxDiscount", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void customizeCategories(String orgId) {
        recordActivity(ClientUtil.ActivityPages.EXT_SIGNUP, ClientUtil.ActivityAction.OPEN);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if (Widgets._amISuperAdmin(currentOrgInfo)) {
            Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                    + "/organizations/getCategories", null);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
            render(resp);
        } else {
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            render("UIComRegister/msgPage.html", msg);
        }
    }

    public static void customizeCategoriesDirect(String orgId) {
        recordActivity(ClientUtil.ActivityPages.EXT_SIGNUP, ClientUtil.ActivityAction.OPEN);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        JSONObject orgInfo = QrAcadStr._getOrgInfo(null);
        if (Widgets._amISuperAdmin(currentOrgInfo)) {
            Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                    + "/organizations/getCategories", null);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
            String includeName = "QrExtSignup/customizeCategories.html";
            flash.put("ENTRY", "DIRECT");
            render("Application/mapper.html", includeName, resp, currentOrgInfo, orgInfo);
        } else {
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            String includeName = "UIComRegister/msgPage.html";
            flash.put("ENTRY", "DIRECT");
            render("Application/mapper.html", includeName, msg, currentOrgInfo, orgInfo);
        }
    }

    public static void customizeCategory(String orgId) {
        recordActivity(ClientUtil.ActivityPages.EXT_SIGNUP, ClientUtil.ActivityAction.OPEN);
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        if (Widgets._amISuperAdmin(currentOrgInfo)) {
            Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                    + "/organizations/customizeCategory", null);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
            renderJSON(resp.toString());
        } else {
            String msg = Messages.get("PAGE_ACCESS_DENIED");
            render("UIComRegister/msgPage.html", msg);
        }
    }
}
