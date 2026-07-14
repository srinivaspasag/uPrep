package controllers;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.validation.Required;
import play.libs.Crypto;
import play.libs.F;
import play.mvc.Http;
import play.mvc.Http.Header;
import play.mvc.Scope;
import pojos.OrgTnCInfo;
import uicom.response.JSONResponse;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;
import uicom.util.Utilities;

public class UIComSecurity extends AbstractUIController {

    public static final String redirectUrlKey = Play.configuration.getProperty("redirect.url.key");

    protected static void checkAccessOfRequest() {

        if (Play.configuration.getProperty("application.id").equals("web-app")) {
            Map<String, Collection<String>> allParams = new HashMap<String, Collection<String>>();
            allParams.put("deviceId", Arrays.asList(session.getId()));
            allParams.put("userId", Arrays.asList(session.get("userId")));
            allParams.put("deviceType", Arrays.asList("UNKNOWN"));
            JSONObject resp = syncCaller(ClientUtil.ORGANIZATION_SERVICE_URL
                    + "/activityLogger/checkIfUserExists", allParams);
            Logger.log4j.info("response from syncCaller in UIComSecurity/checkAccessOfRequest: "
                    + resp.toString());
            try {
                String errorCode = resp.getString("errorCode");
                boolean isPresent = resp.getJSONObject("result").optBoolean("recorded");
                if (!isPresent) {
                    if (true) {
                        Logger.log4j
                                .info("::::::::::::::::         Handing signup/login failure for OTP              :::::::::::::::");
                        return;
                    } else
                        _logout();
                }
            } catch (JSONException error) {
            }
        }
        if (!session.contains("userId")) {
            flash.put(redirectUrlKey, getLogoutAccessUrl());
            String orgId = request.params.get("orgId");

            String redirectUrl = "/login";
            String instLoginPageUrl = UIComRegister._getOrgRefererUrl(orgId);
            if (!StringUtils.isEmpty(instLoginPageUrl)) {
                redirectUrl = instLoginPageUrl;
            } else if (StringUtils.isNotEmpty(orgId)) {
                redirectUrl = "/login/organization/" + orgId;
            }
            Logger.log4j
                    .info("Redirect url for failed case in UIComSecurity/checkAccessOfRequest: "
                            + redirectUrl);
            render("UIComRegister/redirectPage.html", redirectUrl);
        } else if (session.contains("needTNC")) {
            flash.keep(redirectUrlKey);
            String redirectUrl = "/termsandconditions";
            render("UIComRegister/redirectPage.html", redirectUrl);
        }
        String requestedOrgId = request.params.get("orgId");
        if (session.contains("BLOCKED_ORG_ID") && StringUtils.isNotEmpty(requestedOrgId)
                && requestedOrgId.equals(session.get("BLOCKED_ORG_ID"))) {
            flash.keep(redirectUrlKey);
            String redirectUrl = "/showuserblockmsg/" + requestedOrgId;
            render("UIComRegister/redirectPage.html", redirectUrl);
        }
    }

    protected static void checkIfBusinessTncAccepted(String orgId) {

        if (StringUtils.isNotEmpty(orgId)) {
            try {
                OrgTnCInfo orgTnCInfo = Utilities._getOrgTnCInfo(orgId, session.get("userId"));
                boolean needToAcceptBusinessTnc = orgTnCInfo.getNeedsTnCAcceptance();
                String status = orgTnCInfo.getStatus();
                if (needToAcceptBusinessTnc) {
                    flash.keep(redirectUrlKey);
                    flash.put("version", orgTnCInfo.getLatestTnCVersion());
                    flash.put("orgId", orgId);
                    String redirectUrl = "/business-agreement";
                    render("UIComRegister/redirectPage.html", redirectUrl);
                }
                if(status.equalsIgnoreCase("BLOCKED") || status.equalsIgnoreCase("REMOVED")){
                    flash.keep(redirectUrlKey);
                    flash.put("orgId", orgId);
                    String redirectUrl = "/access-denied";
                    render("UIComRegister/redirectPage.html", redirectUrl);
                }
            } catch (Exception e) {
                Logger.log4j.error("Error in checking admin tnc" + e.getMessage());
            }
        }
    }

    public static void login() throws Throwable {

        Http.Cookie remember = request.cookies.get("rememberme");
        if (remember != null && remember.value.indexOf("-") > 0) {
            String sign = remember.value.substring(0, remember.value.indexOf("-"));
            String username = remember.value.substring(remember.value.indexOf("-") + 1);
            if (Crypto.sign(username).equals(sign)) {
                session.put("username", username);
                redirectToOriginalURL();
            }
        }
        flash.keep(redirectUrlKey);
        String orgId = request.params.get("orgId");
        String instLoginPageUrl = UIComRegister._getOrgRefererUrl(orgId);
        if (!StringUtils.isEmpty(instLoginPageUrl)) {
            String redirectUrl = instLoginPageUrl;
            render("UIComRegister/redirectPage.html", redirectUrl);
        } else if (StringUtils.isNotEmpty(orgId)) {
            redirect("/login/organization/" + orgId);
        } else {
            redirect("/login");
        }
    }

    protected static String getLogoutAccessUrl() {

        boolean isAjax = false;
        String url;
        Header xHead = play.mvc.Http.Request.current().headers.get("x-requested-with");
        String newPageOpen = Scope.Params.current().get("newPageOpen");
        if (newPageOpen != null && !newPageOpen.isEmpty()) {
            url = newPageOpen;
            return url;
        } else if (xHead != null) {
            String headStr = xHead.value();
            isAjax = "XMLHttpRequest".equals(headStr);
        }
        if (!isAjax) {
            url = "GET".equals(request.method) ? request.url : "/home";
        }// seems good default
        else {
            url = flash.get(redirectUrlKey);
            url = url == null || url.isEmpty() ? "/home" : url;
        }
        return url;
    }

    public static void redirectToOriginalURL() throws Throwable {

        String url = flash.get(redirectUrlKey);
        flash.remove(redirectUrlKey);
        if (url == null) {
            url = "/home";
        }
        redirect(url);
    }

    public static void authAccessCode(@Required String accessCode) {

        Logger.log4j.info("authentication is being done in UIComSecurity for Access Code.");
        JSONObject jsonResponse = null;
        try {
            F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                    ClientUtil.AUTH_ACCESS_CODE_SERVICE_URL, null);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            jsonResponse = ResponseUtil.checkResponse(getJSON(promise));
        } catch (Exception ex) {
            Logger.log4j.error(ex.getLocalizedMessage());
        }
        renderJSON(jsonResponse.toString());
    }

    protected static String _getMicrositeUrlWithError(String referer, String loginError,
            String lastUserName, String lastMemberId) {
        String url = referer + "?";
        if (!StringUtils.isEmpty(loginError)) {
            url += "loginError=" + loginError + "&lastUserName=" + lastUserName + "&lastMemberId="
                    + lastMemberId;
        }
        return url;
    }

    protected static String _getMicrositeUrlWithError(String referer, String loginError,
            String lastUserName, String lastMemberId, String sectionId) {
        if (sectionId == null) {
            return _getMicrositeUrlWithError(referer, loginError, lastUserName, lastMemberId);
        }
        String url = referer + "?";
        if (!StringUtils.isEmpty(loginError)) {
            url += "loginError=" + loginError + "&lastUserName=" + lastUserName + "&lastMemberId="
                    + lastMemberId + "&lastSectionId=" + sectionId;
        }
        return url;
    }

    protected static String _getMicrositeUrlWithError(String referer, String signupError) {
        String url = referer + "?";
        if (!StringUtils.isEmpty(signupError)) {
            url += "signupError=" + signupError;
        }
        return url;
    }

    protected static JSONObject _authAccessCodeMember(String accessCode, String firstName,
            String lastName, String email, String twitterHandle) throws Throwable {

        Logger.log4j
                .info("authentication is being done in UIComSecurity for Access Code Member Registration.");
        JSONObject jsonResponse = null;
        try {
            Logger.log4j.info("calling member REG========");
            F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                    ClientUtil.LOGIN_ACCESS_CODE_SERVICE_URL, null, null, false);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            jsonResponse = ResponseUtil.checkResponse(getJSON(promise));

            Logger.log4j.info("jsonresponse from the web service is:" + jsonResponse);
            if (!jsonResponse.get("errorCode").toString().isEmpty()) {
                String errorCode = jsonResponse.getString("errorCode");
                String orgId = request.params.get("orgId");
                String instLoginPageUrl = UIComRegister._getOrgRefererUrl(orgId);
                flash.put("loginError", errorCode);
                flash.put("lastFirstName", firstName);
                flash.put("lastLastName", lastName);
                flash.put("lastUsername", email);
                flash.put("lastTwitterHandle", twitterHandle);
                flash.put("lastAccessCode", accessCode);
                flash.put("loginType", "ACCESS_CODE");
                flash.keep(redirectUrlKey);
                if (!StringUtils.isEmpty(instLoginPageUrl)) {
                    String redirectUrl = _getMicrositeUrlWithError(instLoginPageUrl, errorCode,
                            email, "", "");
                    render("UIComRegister/redirectPage.html", redirectUrl);
                } else if (StringUtils.isNotEmpty(orgId)) {
                    redirect("/login/organization/" + orgId);
                } else {
                    redirect("/login");
                }
            } else {
                jsonResponse = jsonResponse.getJSONObject("result");
                _setUserSession(jsonResponse);
                session.put("firstName", firstName);
                _recordLogin();
            }
        } catch (IllegalArgumentException ex) {
            Logger.log4j.error(ex.getLocalizedMessage());
        } catch (JSONException ex1) {
            Logger.log4j.error(ex1.getLocalizedMessage());
        }
        return jsonResponse;
    }

    protected static JSONObject auth(String username, String password) throws Throwable {
        return auth(username, password, null);
    }

    protected static JSONObject auth(String username, String password, String sectionId)
            throws Throwable {

        Logger.log4j.info("authentication is being done in UIComSecurity");
        JSONObject jsonResponse = null;
        try {
            F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                    ClientUtil.LOGIN_SERVICE_URL, null, null, false);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            jsonResponse = ResponseUtil.checkResponse(getJSON(promise));

            String orgId = Scope.Params.current().get("orgId");
            Logger.log4j.info("jsonresponse from the web service is:" + jsonResponse);
            if (!jsonResponse.get("errorCode").toString().isEmpty()) {
                String errorCode = jsonResponse.getString("errorCode");
                String instLoginPageUrl = UIComRegister._getOrgRefererUrl(orgId);
                flash.put("loginError", errorCode);
                flash.put("lastUsername", username);
                flash.put("loginType", "EMAIL_LOGIN");
                flash.put("lastSectionId", sectionId);
                flash.keep(redirectUrlKey);
                if (!StringUtils.isEmpty(instLoginPageUrl)) {
                    String redirectUrl = _getMicrositeUrlWithError(instLoginPageUrl, errorCode,
                            username, "", sectionId);
                    redirect(redirectUrl);
                    // render("UIComRegister/redirectPage.html", redirectUrl);
                } else if (StringUtils.isNotEmpty(orgId)) {
                    redirect("/login/organization/" + orgId);
                } else {
                    redirect("/login");
                }
            } else {
                jsonResponse = jsonResponse.getJSONObject("result");
                _setUserSession(jsonResponse);
                session.put("username", username);
                session.put("loginOrgId", orgId);
                _recordLogin();
            }
        } catch (IllegalArgumentException ex) {
            Logger.log4j.error(ex.getLocalizedMessage());
        } catch (JSONException ex1) {
            Logger.log4j.error(ex1.getLocalizedMessage());
        }
        return jsonResponse;
    }

    protected static JSONObject authMember(String orgId, String memberId, String password)
            throws Throwable {
        return authMember(orgId, memberId, password, null);
    }

    protected static JSONObject authMember(String orgId, String memberId, String password,
            String sectionId) throws Throwable {

        Logger.log4j.info("authentication is being done in UIComSecurity");
        JSONObject jsonResponse = null;
        try {
            F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                    ClientUtil.LOGIN_MEMBER_SERVICE_URL, null, null, false);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            jsonResponse = ResponseUtil.checkResponse(getJSON(promise));

            Logger.log4j.info("jsonresponse from the web service is:" + jsonResponse);
            if (!jsonResponse.get("errorCode").toString().isEmpty()) {
                String errorCode = jsonResponse.getString("errorCode");
                orgId = Scope.Params.current().get("orgId");
                String instLoginPageUrl = UIComRegister._getOrgRefererUrl(orgId);
                flash.put("loginError", errorCode);
                flash.put("lastMemberId", memberId);
                flash.put("lastSectionId", sectionId);
                flash.put("loginType", "MEMBER_LOGIN");
                flash.keep(redirectUrlKey);
                if (!StringUtils.isEmpty(instLoginPageUrl)) {
                    String redirectUrl = _getMicrositeUrlWithError(instLoginPageUrl, errorCode, "",
                            memberId, sectionId);
                    // render("UIComRegister/redirectPage.html", redirectUrl);
                    redirect(redirectUrl);
                } else if (StringUtils.isNotEmpty(orgId)) {
                    redirect("/login/organization/" + orgId);
                } else {
                    redirect("/login");
                }
            } else {
                jsonResponse = jsonResponse.getJSONObject("result");
                _setUserSession(jsonResponse);
                session.put("memberId", memberId);
                session.put("loginMemberId", memberId);
                session.put("loginOrgId", orgId);
                _recordLogin();
            }
        } catch (IllegalArgumentException e) {
            Logger.log4j.error(e.getLocalizedMessage());
        } catch (JSONException e) {
            Logger.log4j.error(e.getLocalizedMessage());
        }
        return jsonResponse;
    }

    protected static JSONObject authentifyOTPMember(String orgId, String username) throws Throwable {
        return authentifyOTPMember(orgId, username, null);
    }

    protected static JSONObject authentifyOTPMember(String orgId, String username, String sectionId)
            throws Throwable {

        Logger.log4j.info("authentication with OTP is being done in UIComSecurity");
        JSONObject jsonResponse = null;
        try {
            F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                    ClientUtil.ORGANIZATION_SERVICE_URL + "/members/authenticateOTPMember", null,
                    null, false);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            jsonResponse = ResponseUtil.checkResponse(getJSON(promise));

            Logger.log4j.info("jsonresponse from the web service is:" + jsonResponse);
            if (!jsonResponse.get("errorCode").toString().isEmpty()) {
                String errorCode = jsonResponse.getString("errorCode");
                orgId = Scope.Params.current().get("orgId");
                String instLoginPageUrl = UIComRegister._getOrgRefererUrl(orgId);
                flash.put("loginError", errorCode);
                flash.put("lastUsername", username);
                flash.put("lastSectionId", sectionId);
                flash.put("loginType", "OTP_LOGIN");
                flash.keep(redirectUrlKey);
                if (!StringUtils.isEmpty(instLoginPageUrl)) {
                    String redirectUrl = _getMicrositeUrlWithError(instLoginPageUrl, errorCode, "",
                            username, sectionId);
                    // render("UIComRegister/redirectPage.html", redirectUrl);
                    redirect(redirectUrl);
                } else if (StringUtils.isNotEmpty(orgId)) {
                    redirect("/login/organization/" + orgId);
                } else {
                    redirect("/login");
                }
            } else {
                jsonResponse = jsonResponse.getJSONObject("result");
                _setUserSession(jsonResponse);
                session.put("username", username);
                session.put("loginOrgId", orgId);
                session.put("isOTPlogin", request.params.get("isOTPlogin"));
                session.put("progType", request.params.get("progType"));
                _recordLogin();
            }
        } catch (IllegalArgumentException e) {
            Logger.log4j.error(e.getLocalizedMessage());
        } catch (JSONException e) {
            Logger.log4j.error(e.getLocalizedMessage());
        }
        return jsonResponse;
    }

    private static void PostToLeadSquared(String signupType) {
        try {
            String fieldName;
            String fieldvalue;
            LeadSquaredJSONHelper leadJSON = new LeadSquaredJSONHelper();
            if (signupType.equals("Phone")) {
                fieldName = "Phone";
                fieldvalue = request.params.get("contactNumber");
            } else {
                fieldName = "EmailAddress";
                fieldvalue = request.params.get("email");
                leadJSON.addLeadAttribute("Phone", request.params.get("contactNumber"));
            }
            leadJSON.addLeadAttribute(fieldName, fieldvalue);
            leadJSON.addLeadAttribute("FirstName", request.params.get("firstName"));
            leadJSON.addLeadAttribute("Source", "Registered on Old Website");

            String jsonData = leadJSON.getJSONString();

            // adding a lead
            String accessKey = "u$rd12484176de6dd1318a8621f45305114";
            String secretKey = "1bbd3f66fb3d78f644969e382553a84dc9f004c3";

            LeadSquaredAPIManager lead = new LeadSquaredAPIManager(accessKey, secretKey);

            lead.addLead(jsonData);
        } catch (Exception ex) {
            Logger.log4j
                    .error("*****     Something error happened while posting in LeadSquared     ******");
        }
    }

    protected static JSONObject signupAndAuth(String orgId) throws Throwable {

        Logger.log4j.info("Signup is being done in UIComSecurity");
        JSONObject jsonResponse = null;
        try {
            F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                    ClientUtil.SIGNUP_MEMBER_SERVICE_URL, null, null, false);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            jsonResponse = ResponseUtil.checkResponse(getJSON(promise));

            Logger.log4j.info("jsonresponse from the signup web service is:" + jsonResponse);
            String errorCode = jsonResponse.getString("errorCode");
            if (!errorCode.isEmpty()) {
                String instLoginPageUrl = UIComRegister._getOrgRefererUrl(orgId);
                orgId = Scope.Params.current().get("orgId");
                flash.put("signupError", errorCode);
                flash.keep(redirectUrlKey);
                if (!StringUtils.isEmpty(instLoginPageUrl)) {
                    String redirectUrl = _getMicrositeUrlWithError(instLoginPageUrl, errorCode);
                    redirect(redirectUrl);
                    // render("UIComRegister/redirectPage.html", redirectUrl);
                } else if (StringUtils.isNotEmpty(orgId)) {
                    redirect("/login/organization/" + orgId);
                } else {
                    redirect("/login");
                }
            } else {
                if (play.Play.configuration.getProperty("learnpedia.id").equals(
                        request.params.get("orgId"))
                        && Play.configuration.getProperty("application.name").equals("web-app")) {
                    PostToLeadSquared("Email");
                }
                jsonResponse = jsonResponse.getJSONObject("result");
                _setUserSession(jsonResponse);
                session.put("loginOrgId", orgId);
                session.put("isOTPsignup", request.params.get("isOTPsignup"));
                session.put("progType", request.params.get("progType"));
                session.put("autoAddDemoProgram", jsonResponse.getBoolean("autoAddDemoProgram"));
                session.put("autoAddCampaignProgram",
                        jsonResponse.getBoolean("autoAddCampaignProgram"));
                session.put("contactNumber", jsonResponse.get("contactNumber"));
                session.put("isSignup", "true");
                if (jsonResponse.getBoolean("showSpecialMessage") == true) {
                    session.put("showWelcomeMessage", true);
                }
                _recordLogin();
            }
        } catch (IllegalArgumentException e) {
            Logger.log4j.error(e.getLocalizedMessage());
        } catch (JSONException e) {
            Logger.log4j.error(e.getLocalizedMessage());
        }
        return jsonResponse;
    }

    protected static void _recordLogin() {

        Map<String, Object> allParams = new HashMap<String, Object>();
        String userId = session.get("callingUserId");
        allParams.put("userId", userId);
        allParams.put("callingUserId", userId);
        allParams.put("deviceId", session.getId());
        allParams.put("deviceType", "WEB");
        allParams.put("expiryTimeOffset", -1);
        client(ClientUtil.ORGANIZATION_SERVICE_URL + "/activityLogger/login", allParams, null,
                false);
    }

    protected static void _recordLogout() {

        String userId = session.get("callingUserId");
        if (StringUtils.isEmpty(userId)) {
            return;
        }
        request.params.put("userId", userId);
        request.params.put("callingUserId", userId);
        request.params.put("deviceId", session.getId());
        request.params.put("deviceType", "WEB");
        syncCaller(ClientUtil.ORGANIZATION_SERVICE_URL + "/activityLogger/logout", null);
    }

    protected static void _logout() {

        _recordLogout();
        String clearCache = Play.configuration.getProperty("clearCacheOnLogout");
        Logger.log4j.info("decision for clear cache:::::::   " + clearCache);
        if (StringUtils.equals(clearCache, "clear")) {
            Cache.clear();
        }
        String userId = session.get("userId");
        Logger.log4j.info("Clearing the orgInfoTnC of userId: " + userId);
        try {
            JSONObject userOrgsJSONObject = syncCaller(ClientUtil.ORGANIZATION_SERVICE_URL
                    + "/organizations/getAssociatedOrgsOfUser", null);
            JSONArray userOrgsJSONList = userOrgsJSONObject.getJSONObject("result").getJSONArray(
                    "list");
            for (int i = 0; i < userOrgsJSONList.length(); i++) {
                String orgId = userOrgsJSONList.getJSONObject(i).getString("id");
                String orgTnCInfoKey = "ORG_TNC_INFO_" + orgId + "_" + userId;
                Cache.safeDelete(orgTnCInfoKey);
            }
        } catch (Exception e) {
            Logger.log4j.error("Error in deleting the cache of orgTnCInfo: " + e.getMessage());
        }
        String cmdsOrgsKey = "ORGS_OF_USER_" + userId;
        Cache.delete(cmdsOrgsKey);
        session.clear();
    }

    public static void logout(String orgId) {

        // making sure that the userId is changed back to calling user id
        // incase the user was browsing on someone else's behalf
        session.put("userId", session.get("callingUserId"));
        orgId = StringUtils.isEmpty(orgId) ? session.contains("loginOrgId") ? session
                .get("loginOrgId") : "" : orgId;
        String instLoginPageUrl = UIComRegister._getOrgRefererUrl(orgId);
        _logout();
        flash.keep(redirectUrlKey);
        if (!StringUtils.isEmpty(instLoginPageUrl)) {
            String redirectUrl = instLoginPageUrl;
            render("UIComRegister/redirectPage.html", redirectUrl);
        } else if (StringUtils.isNotEmpty(orgId)) {
            redirect("/login/organization/" + orgId);
        } else {
            redirect("/login");
        }
    }

    public static void removeBrowseAsUser() {

        session.put("userId", session.get("callingUserId"));
        renderJSON(new JSONResponse(true));
    }

    private static void _setUserSession(JSONObject loginResponse) {

        // clearing the session to remove any old persisting values.
        session.clear();
        try {
            Boolean needTNC = loginResponse.getBoolean("needsTnCAcceptance");
            String userId = loginResponse.getString("id");
            if (loginResponse.has("userId") && loginResponse.get("userId") != null) {
                userId = loginResponse.getString("userId");
            }
            if (needTNC) {
                session.put("needTNC", needTNC);
                session.put("TNC-CODE", loginResponse.getString("latestTnCVersion"));
            }
            session.put("profilePic", loginResponse.get("thumbnail"));
            session.put("userId", userId);
            session.put("callingUserId", userId);
            session.put("firstName", loginResponse.get("firstName"));
            session.put("lastName", loginResponse.get("lastName"));
            session.put("fullname",
                    loginResponse.get("firstName") + " " + loginResponse.get("lastName"));
            if (loginResponse.has("authType")) {
                session.put("userAuthType", loginResponse.get("authType"));
            }
        } catch (Exception e) {
            Logger.log4j.info("Error in setting user session params = " + e);
        }
    }

    public static void signupSection(String sectionId, String orgId) {
        flash.remove(redirectUrlKey);
        Map<String, Object> allParams = getReqParams();
        allParams.put("targetUserId", session.get("userId"));
        F.Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/getMemberProfile", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject memberResponse = getJSON(promise);
        memberResponse = ResponseUtil.checkResponse(memberResponse);
        render("UIComMicrosite/signupSection.html", sectionId, orgId, memberResponse);
    }

    public static void addMemberToSection() {
        request.params.put("targetUserId", session.get("userId"));
        F.Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/addMemberMapping", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
}
