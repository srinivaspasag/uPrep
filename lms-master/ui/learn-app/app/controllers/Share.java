package controllers;

import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.data.validation.Required;
import play.libs.F;
import play.mvc.Scope;
import util.ClientUtil;
import util.Validation;
import controllers.AbstractUIController.JSONResponseWrapper;

public class Share extends AbstractUIController{

    static final String className = Share.class.getSimpleName();

    public static void shareWith() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                util.ClientUtil.ORGANIZATION_SERVICE_URL
                        + "/organizations/getAssociatedOrgsOfUser", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject instData = getJSON(promise);
        instData = Validation.verifyResponse(instData);
        render(renderTheme(orgId,getHTMLFilePath(className)),instData);
    }

    public static void shareWithInst(@Required String orgId, @Required String orgName) {
        JSONObject instData = null;
        render(renderTheme(orgId,getHTMLFilePath(className,"shareWith")), instData);
    }

    public static void getShareIndvUi() {
        String orgId = session.get("loginOrgId");
        render(renderTheme(orgId,getHTMLFilePath(className,"shareWithIndv")));
    }

    public static void getSharePublicUi() {
        String orgId = session.get("loginOrgId");
        render(renderTheme(orgId,getHTMLFilePath(className,"shareWithPublic")));
    }

    public static void shareEntity() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        F.Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL
                + "/share/shareEntity", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        renderJSON(resp.toString());
    }

    public static void shareFrndsSuggs() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Map<String, Object> allParams = getReqParams();
        F.Promise<JSONResponseWrapper> promise = client(ClientUtil.SEARCH_WEB_SERVICE_URL
                + "/search/suggestion", allParams);
        Logger.log4j.info("BEFORE AWAIT - GET SEARCH SUGGESTION");
        await(promise);
        Logger.log4j.info("AFTER AWAIT - GET SEARCH SUGGESTION");
        JSONObject resp = getJSON(promise);
        renderJSON(resp.toString());
    }

    protected static JSONObject _getSharebleDomains() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Scope.Params.current().put("targetUserId", session.get("userId"));
        F.Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/getMemberProfile", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        try {
            if (data != null && data.getString("errorCode").isEmpty()) {
                data = data.getJSONObject("result").getJSONObject("info").getJSONObject("mappings");
            }
        } catch (JSONException ex) {
            data = null;
            Logger.log4j.error(ex.getLocalizedMessage());
        }
        return data;
    }

    public static void shareableBatches() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        JSONObject data = _getSharebleDomains();
        render(renderTheme(orgId,getHTMLFilePath(className)),data);
    }

    public static void shareableCenters() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        JSONObject data = null;
        String progIndex = Scope.Params.current().get("batchIndex");
        if (progIndex != null) {
            data = _getSharebleDomains();
            Integer index = Integer.parseInt(progIndex);
            try {
                data = data.getJSONArray("programs").getJSONObject(index);
            } catch (JSONException ex) {
                data = null;
            }
        }
        render(renderTheme(orgId,getHTMLFilePath(className)),data);
    }

}
