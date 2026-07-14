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
import play.libs.F;
import play.mvc.Before;
import play.mvc.Scope;
import play.mvc.Http.Header;
import util.Utilities;
import util.ClientUtil;
import util.ResponseUtil;

public class Security extends AbstractUIController {

    public static final String redirectUrlKey = Play.configuration.getProperty("redirect.url.key");

    @Before(unless = { "logout", "authentify", "authentifyWithOTP", "authentifyMember",
            "directaccess", "signup" })
    static void checkAccess() throws Throwable {
        flash.keep(redirectUrlKey);
        checkAccessOfRequest();

        String callerUserId = null;
        if (request.params._contains("myUserId")) {
            callerUserId = request.params.get("myUserId");
        }
        String userId = session.get("userId");
        if (callerUserId != null && !callerUserId.equals(userId)) {
            String redirectUrl = request.params.get("newPageOpen");
            redirectUrl = StringUtils.isNotEmpty(redirectUrl) ? redirectUrl : "/home";
            redirect(redirectUrl);
        }
        String orgId = request.params.get("orgId");
        if (StringUtils.isNotEmpty(orgId)) {
            Logger.log4j.info(":::::::       Checking Org Access       ::::::::");
            Logger.log4j.info("orgId is " + orgId);
            checkIfNewWebAccessGranted(orgId);
        }
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

    private static void checkIfNewWebAccessGranted(String orgId) {
        if (StringUtils.isNotEmpty(orgId)) {
            try {
                JSONObject info = Utilities._getOrgNewWebAppInfo(orgId);
                Logger.log4j.info("----------------------------------");
                Logger.log4j.info("response from Org Access " + info);
                Logger.log4j.info("----------------------------------");
                info = info.getJSONObject("result");
                boolean isNewUI = info.getBoolean("isNewUI");
                if (isNewUI) {
                    flash.keep(redirectUrlKey);
                    session.put("isNewUI", true);
                    session.put("theme", info.get("theme"));
                } else {
                    session.put("isNewUI", false);
                    redirect("/noAccess");
                }
            } catch (Exception e) {
                Logger.log4j.error("Error in checking admin tnc" + e.getMessage());
            }
        }
    }

    @SuppressWarnings("unused")
    protected static void checkAccessOfRequest() {

        if (Play.configuration.getProperty("application.id").equals("learn-app")) {
            Map<String, Collection<String>> allParams = new HashMap<String, Collection<String>>();
            allParams.put("deviceId", Arrays.asList(session.getId()));
            allParams.put("userId", Arrays.asList(session.get("userId")));
            allParams.put("deviceType", Arrays.asList("UNKNOWN"));
            JSONObject resp = syncCaller(ClientUtil.ORGANIZATION_SERVICE_URL
                    + "/activityLogger/checkIfUserExists", allParams);
            Logger.log4j.info("response from syncCaller in Security/checkAccessOfRequest: "
                    + resp.toString());
            try {
                String errorCode = resp.getString("errorCode");
                if (StringUtils.isEmpty(errorCode)) {
                    boolean isPresent = resp.getJSONObject("result").optBoolean("recorded");
                    if (!isPresent) {
                        if (!session.contains("isLoggedIn")) {
                            Logger.log4j
                                    .info(":::::::::         Handing signup/login failure for OTP        ::::::::");
                            session.put("isLoggedIn", true);
                            return;
                        } else {
                            _logout();
                        }
                    }
                } else {
                    redirect("/");
                }
            } catch (JSONException error) {
            }
        }
        flash.keep(redirectUrlKey);
        if (!session.contains("userId")) {
            flash.put(redirectUrlKey, getLogoutAccessUrl());
            String orgId = request.params.get("orgId");
            String redirectUrl = "/";
            String instLoginPageUrl = Register._getOrgRefererUrl(orgId);
            if (!StringUtils.isEmpty(instLoginPageUrl)) {
                redirectUrl = instLoginPageUrl;
            } else if (StringUtils.isNotEmpty(orgId)) {
                redirectUrl = "/";
            }
            Logger.log4j.info("Redirect url for failed case in Security/checkAccessOfRequest: "
                    + redirectUrl);
            redirect(redirectUrl);
        } else if (session.contains("needTNC")) {
            flash.keep(redirectUrlKey);
            String redirectUrl = "/terms";
            redirect(redirectUrl);
        }
        String requestedOrgId = request.params.get("orgId");
        if (session.contains("BLOCKED_ORG_ID") && StringUtils.isNotEmpty(requestedOrgId)
                && requestedOrgId.equals(session.get("BLOCKED_ORG_ID"))) {
            flash.keep(redirectUrlKey);
            Institute.redirectToNoOrgError();
        }
    }

    protected static String getLogoutAccessUrl() {
        flash.keep(redirectUrlKey);
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

    public static void authentifyWithOTP(String username, String orgId, String sectionId,
            String packageDays) throws Throwable {
        Logger.log4j.info("Member Login with OTP Auth Values orgId: " + orgId + " username: "
                + username);
        request.params.put("callingApp", Play.configuration.getProperty("application.name"));
        request.params.put("callingAppId", Play.configuration.getProperty("application.id"));
        authentifyOTPMember(orgId, username);
        Logger.log4j.info("================== AFTER LOGIN WITH OTP COMPLETED ===============> ");
        String redirectUrl = "/library";
        if (!StringUtils.isEmpty(sectionId) && !StringUtils.isEmpty(orgId)
                && !StringUtils.isEmpty(packageDays)) {
            redirectUrl = "/programs?"+"packageDays="+packageDays+"&sectionId="+sectionId;
        }
        flash.put(redirectUrlKey, redirectUrl);
        flash.keep(redirectUrlKey);
        learnpediaLoading(orgId);
    }

    public static void authentify(String username, String password, String orgId) throws Throwable {

        request.params.put("callingApp", Play.configuration.getProperty("application.name"));
        request.params.put("callingAppId", Play.configuration.getProperty("application.id"));
        auth(username, password);
        Logger.log4j.info("================== AFTER LOGIN WITH EMAIL COMPLETED ===============> ");
        String redirectUrl = "/library";
        flash.put(redirectUrlKey, redirectUrl);
        flash.keep(redirectUrlKey);
        learnpediaLoading(orgId);
    }

    public static void authentifyMember(String orgId, String memberId, String password,
            String sectionId) throws Throwable {

        Logger.log4j.info("Member Login Auth Values ========== orgId === " + orgId
                + ", memberId === " + memberId);// + ", password === " +
                                                // password);
        request.params.put("callingApp", Play.configuration.getProperty("application.name"));
        request.params.put("callingAppId", Play.configuration.getProperty("application.id"));
        authMember(orgId, memberId, password, sectionId);
        // if(!StringUtils.isEmpty(sectionId) && !StringUtils.isEmpty(orgId)){
        // String redirectUrl = "/signupsection/"+sectionId+"?orgId="+orgId;
        // flash.put(redirectUrlKey, redirectUrl);
        // }
        flash.keep(redirectUrlKey);
        learnpediaLoading(orgId);
    }

    protected static JSONObject auth(String username, String password) throws Throwable {

        Logger.log4j.info("authentication is being done in Security");
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
                flash.put("loginError", errorCode);
                flash.put("email", request.params.get("username"));
                redirect("/login");
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

        Logger.log4j.info("authentication is being done in Security");
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
                // String instLoginPageUrl = Register._getOrgRefererUrl(orgId);
                flash.put("loginError", errorCode);
                flash.put("lastMemberId", memberId);
                flash.put("lastSectionId", sectionId);
                flash.put("loginType", "MEMBER_LOGIN");
                flash.keep(redirectUrlKey);
                if (StringUtils.isNotEmpty(orgId)) {
                    redirect("/login");
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

        Logger.log4j.info("authentication with OTP is being done in Security");
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
                flash.put("loginError", errorCode);
                flash.put("contactNumber", request.params.get("contactNumber"));
                flash.put("countryCode", request.params.get("countryCode"));
                flash.put("firstName", request.params.get("firstName"));
                if (Boolean.parseBoolean(request.params.get("isOTPlogin"))
                        && Boolean.parseBoolean(request.params.get("isIndexPage"))) {
                    redirect("/");
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
        } catch (IllegalArgumentException e) {
            Logger.log4j.error(e.getLocalizedMessage());
        } catch (JSONException e) {
            Logger.log4j.error(e.getLocalizedMessage());
        }
        return jsonResponse;
    }

    public static void signup(String orgId, String sectionId,String packageDays) throws Throwable {
        request.params.put("callingApp", Play.configuration.getProperty("application.name"));
        request.params.put("callingAppId", Play.configuration.getProperty("application.id"));
        request.params.put("userId", "PUBLIC");
        request.params.put("callingUserId", "PUBLIC");
        request.params.put("profile", "STUDENT");
        if (orgId.equals(Play.configuration.getProperty("learnpedia.id"))) {
            request.params.put("autoAddDemoProgram", "true");
        }
        signupAndAuth(orgId);
        Logger.log4j.info("================== AFTER SIGNUP COMPLETED ===============> ");
        Logger.log4j.info("Display Days "+packageDays+" sectionId  "+sectionId);
        String redirectUrl = "";
        if (!StringUtils.isEmpty(sectionId) && !StringUtils.isEmpty(orgId)
                && !StringUtils.isEmpty(packageDays)) {
            redirectUrl = "/programs?"+"packageDays="+packageDays+"&sectionId="+sectionId;
        }
        else if(!StringUtils.isEmpty(sectionId) && !StringUtils.isEmpty(orgId)){
            redirectUrl = "/programs";
        }
        else {
            redirectUrl = "/library";
        }
        flash.put(redirectUrlKey, redirectUrl);
        flash.keep(redirectUrlKey);
        learnpediaLoading(orgId);
    }

    protected static JSONObject signupAndAuth(String orgId) throws Throwable {

        Logger.log4j.info("Signup is being done in Security signupAndAuth");
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
                flash.put("signupError", errorCode);
                flash.put("contactNumber", request.params.get("contactNumber"));
                flash.put("countryCode", request.params.get("countryCode"));
                flash.put("firstName", request.params.get("firstName"));
                if (Boolean.parseBoolean(request.params.get("isOTPsignup"))) {
                    redirect("/");
                } else {
                    flash.put("email", request.params.get("email"));
                    redirect("/signup");
                }
            } else {
                if (play.Play.configuration.getProperty("learnpedia.id").equals(
                        request.params.get("orgId"))
                        && Play.configuration.getProperty("application.name").equals("learn-app")
                        && play.Play.configuration.getProperty("environment").equalsIgnoreCase(
                                "prod")) {
                    PostToLeadSquared("Email");
                }
                jsonResponse = jsonResponse.getJSONObject("result");
                _setUserSession(jsonResponse);
                session.put("loginOrgId", orgId);
                session.put("autoAddDemoProgram", jsonResponse.getBoolean("autoAddDemoProgram"));
                session.put("contactNumber", jsonResponse.get("contactNumber"));
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

    public static void directaccess() throws Throwable {
        request.params.put("dl", "true");
        String username = request.params.get("u");
        String password = request.params.get("p");
        request.params.put("username", username);
        request.params.put("password", password);
        request.params.put("callingApp", Play.configuration.getProperty("application.name"));
        request.params.put("callingAppId", Play.configuration.getProperty("application.id"));
        auth(username, password);
        session.put("directLogin", true);
        flash.keep(redirectUrlKey);
        learnpediaLoading(null);
    }

    public static void learnpediaLoading(String orgId) {

        if (orgId == null) {
            orgId = session.get("loginOrgId");
        }
        Logger.log4j.info("Inside learnpedia loading");
        JSONArray orgArray = Institute._getUserOrgs(getReqParams(), true);
        boolean retVal = decideFrontEndUserRedirect(orgArray);
        String fetchUrl = "/library";
        if (flash.contains(redirectUrlKey))
            fetchUrl = (flash.get(redirectUrlKey) == null || flash.get(redirectUrlKey).equals("null")) ? "/library"
                    : flash.get(redirectUrlKey);
        if (retVal) {
            if (Boolean.parseBoolean(session.get("autoAddDemoProgram"))) {
                fetchUrl = "/library";
                session.remove("autoAddDemoProgram");
            }
            flash.put(redirectUrlKey, fetchUrl);
            Logger.log4j.info("Redirect url : " + flash.get(redirectUrlKey));
            fetchUrl = decideRedirect(orgId, orgArray);
        }
        flash.remove(redirectUrlKey);
        render("/loading.html", fetchUrl);
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
            leadJSON.addLeadAttribute("Source", "Registered on New Website");

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

    private static String decideRedirect(String orgId, JSONArray orgArray) {

        if (orgId == null || (orgId != null && !orgId.isEmpty())) {
            orgId = _getOrgId(orgArray);
        }
        session.put("loginOrgId", orgId);
        String redirectUrl = flash.get(redirectUrlKey);
        flash.remove(redirectUrlKey);
        if (redirectUrl == null || redirectUrl.equals("/") || redirectUrl.equals("/home")) {
            redirectUrl = "/organization/" + orgId;
        }
        Logger.log4j.info("Going to org page == organization Id --- " + orgId + ", page ========= "
                + redirectUrl);
        return redirectUrl;
    }

    protected static String _getOrgId(JSONArray orgArray) {

        String orgId = "";
        if (orgArray == null) {
            Institute.redirectToNoOrgError();
        }
        try {
            JSONObject org = orgArray.optJSONObject(0);
            orgId = org.getString("id");
        } catch (Exception ex) {
            Logger.log4j.info(ex.getMessage());
            Institute.redirectToNoOrgError();
        }
        return orgId;
    }

    private static Boolean decideFrontEndUserRedirect(JSONArray orgArray) {

        if (orgArray == null) {
            Institute.redirectToNoOrgError();
        }
        try {
            if (orgArray.length() == 1) {
                JSONObject org = orgArray.getJSONObject(0);
                String userRole = org.getString("profile");
                String orgId = org.getString("id");
                if ("FRONT_DESK_USER".equals(userRole)) {
                    Logger.log4j.info("Going to front desk user page ===============" + orgId);
                    return false;
                }
            } else if (orgArray.length() > 1) {
                for (Integer oi = 0; oi < orgArray.length(); oi++) {
                    JSONObject org = orgArray.getJSONObject(oi);
                    String userRole = org.getString("profile");
                    if ("FRONT_DESK_USER".equals(userRole)) {
                        Logger.log4j
                                .info("Going to front desk user SELECTION page ===============");
                        return false;
                    }
                }
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
        }
        return true;
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
            session.put("showWelcomeMessage", true);
        } catch (Exception e) {
            Logger.log4j.info("Error in setting user session params = " + e);
        }
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

    public static void logout(String orgId) {
        session.put("userId", session.get("callingUserId"));
        orgId = StringUtils.isEmpty(orgId) ? session.contains("loginOrgId") ? session
                .get("loginOrgId") : "" : orgId;
        String instLoginPageUrl = Register._getOrgRefererUrl(orgId);
        Logger.log4j.info("Login Page URL " + instLoginPageUrl);
        _logout();
        flash.keep(redirectUrlKey);
        if (!StringUtils.isEmpty(instLoginPageUrl)) {
            redirect(instLoginPageUrl);
        } else if (StringUtils.isNotEmpty(orgId)) {
            redirect("/");
        } else {
            redirect("/");
        }
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

}
