package controllers;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.libs.Codec;
import play.libs.F.Promise;
import play.mvc.Scope;
import uicom.util.ClientUtil;

public class invite extends AbstractUIController {
    private static final String gmail_redirect_uri = Play.configuration
            .getProperty("google.redirect.url");
    private static final String fb_redirect_uri = Play.configuration
            .getProperty("facebook.oauth.redirect.url");
    private static final String fb_client_id = Play.configuration
            .getProperty("facebook.oauth.client_id");
    private static final String fb_scope = Play.configuration
            .getProperty("facebook.oauth.scope");
    private static final String fbConnectUrl = "https://www.facebook.com/dialog/oauth?client_id="
            + fb_client_id
            + "&redirect_uri="
            + fb_redirect_uri
            + "&scope="
            + fb_scope + "&state=123456&response_type=code";

    public static void googleoAuth2Callback(String code, String state,
            String error) {
        if (StringUtils.isNotEmpty(error)) {
            renderJSON("Error Try Again");
        } else {
            Promise<JSONResponseWrapper> promise = client(
                    ClientUtil.PROFILE_WEB_SERVICE_URL
                            + "/invite/googleOAuth2Callback?redirect_uri="
                            + gmail_redirect_uri, null);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = getJSON(promise);
            renderHtml("<script>window.onload=function(){window.opener.showGmailEmails("
                    + resp.toString() + ");window.close();}</script>");
        }
    }

    public static void sendInvite() {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.PROFILE_WEB_SERVICE_URL + "/invite/sendInvite", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        renderJSON(resp.toString());
    }

    public static void register(@Required String email,
            @Required String inviteKey) {
        try {
            if (validation.hasErrors()) {
                Logger.log4j.error("trying to sign up without invite key");
                render("/Register/index.html");
            }
            if (StringUtils.isNotEmpty(session.get("userId"))
                    && StringUtils
                            .isNotEmpty(session.get("userSessionAuthKey"))) {
                Application.home();
            }
            Promise<JSONResponseWrapper> promise = client(
                    ClientUtil.PROFILE_WEB_SERVICE_URL
                            + "/invite/validInviteKey", null);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = getJSON(promise);
            if (resp != null && resp.has("errorCode")
                    && StringUtils.isEmpty(resp.getString("errorCode"))) {
                response.setHeader("Pragma", "no-cache");
                response.setHeader("Cache-Control",
                        "no-cache, max-age=0, must-revalidate, no-store");
                response.setHeader("Expires", "0");
                String randomId = Codec.UUID();
                render(randomId, inviteKey);
            } else {
                render("/Register/index.html");
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage(), ex);
        }
    }

    public static void fbConnect() {
        redirect(fbConnectUrl);
    }

    public static void connectFacebookAccount(String code, String error) {
        if (StringUtils.isNotEmpty(error)) {
            renderJSON("Error Try Again");
        } else {
            Promise<JSONResponseWrapper> promise = client(
                    ClientUtil.PROFILE_WEB_SERVICE_URL
                            + "/socialConnect/connectFacebookAccount?redirect_uri="
                            + fb_redirect_uri, null);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = getJSON(promise);
            renderHtml("<script>window.onload=function(){window.opener.showFBFriends("
                    + resp.toString() + ");window.close();}</script>");
        }
    }

    public static void connectedToFB() {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.PROFILE_WEB_SERVICE_URL
                        + "/socialConnect/isfacebookconnected", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        renderJSON(resp.toString());
    }

    public static void followSignupDoc() {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL + "/librarys/addDocument", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        renderJSON(jsonResponse.toString());
    }

    public static void followSignupUser() {
        Scope.Params.current().put("srcUserId", session.get("userId"));
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.FOLLOW_WEB_SERVICE_URL + "/follows/followUser", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject follow = getJSON(promise);
        renderJSON(follow.toString());
    }

    public static void SUMyDesk() {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.MYDESK_WEB_SERVICE_URL
                        + "/MyDeskApplication/addItem", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        renderJSON(jsonResponse.toString());
    }

    public static void SUAddToLibrary() {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL + "/librarys/addDocument", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        renderJSON(jsonResponse.toString());
    }

    public static void invalidateSession() {
        UIComSecurity._logout();
        session.remove("username");
        session.remove("authKey");
        session.remove("fullname");
        session.remove("userSessionAuthKey");
        session.remove("userId");
        session.remove("isVerified");
        //session.clear();
        Logger.log4j.info("session removed");
        renderJSON("{success:true}");
    }
}
