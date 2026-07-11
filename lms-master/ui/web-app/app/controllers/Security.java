package controllers;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.i18n.Messages;
import play.mvc.Before;
import uicom.response.ErrorInfo;
import uicom.response.JSONResponse;

public class Security extends UIComSecurity {

    @Before(unless = {"login", "logout", "authentify", "authentifyWithOTP", "authentifyMember", "redirectPage", "directaccess", "authAccessCode", "authentifyAccessCodeMember", "signup"})
    static void checkAccess() throws Throwable {
        checkAccessOfRequest();

        String callerUserId = null;
        if (request.params._contains("myUserId")) {
            callerUserId = request.params.get("myUserId");
        }
        String userId = session.get("userId");
        if (callerUserId != null && !callerUserId.equals(userId)) {
            String redirectUrl = request.params.get("newPageOpen");
            redirectUrl = StringUtils.isNotEmpty(redirectUrl) ? redirectUrl : "/home";
            render("UIComRegister/redirectPage.html", redirectUrl);
        }

//        String orgId = request.params.get("orgId");
//        if (StringUtils.isNotEmpty(orgId)) {
//            checkIfBusinessTncAccepted(orgId);
//        }
        /*
         * // * else if (session.get("isVerified") == null || // *
         * !Boolean.parseBoolean(session.get("isVerified"))) { redirectUrl = // *
         * "/register/emailVerifyError"; UIComRegister.redirectPage(redirectUrl); } //
         */
    }

//    public static void browseAsUser(String targetUserId) {
//        String orgId = request.params.get("orgId");
//        UserOrg orgInfo = Widgets.getCurrentOrgInfo(orgId);
//        if (StringUtils.equals(orgInfo.getOrgUserProfile(),
//                Play.configuration.getProperty("impersonation.role"))) {
//            session.put("userId", targetUserId);
//            renderJSON(new JSONResponse(true));
//        } else {
//            renderJSON(new JSONResponse(new ErrorInfo("NO_SUFFICIENT_RIGHTS")));
//        }
//    }
    public static void authentifyAccessCodeMember(String accessCode, String firstName, String lastName, String email, String twitterHandle)
            throws Throwable {

        request.params.put("callingApp", Play.configuration.getProperty("application.name"));
        request.params.put("callingAppId", Play.configuration.getProperty("application.id"));
        _authAccessCodeMember(accessCode, firstName, lastName, email, twitterHandle);
        flash.keep(redirectUrlKey);
        vedantuLoading(null);
    }

    public static void authentifyMember(String orgId, String memberId, String password, String sectionId)
            throws Throwable {

        Logger.log4j.info("Member Login Auth Values ========== orgId === " + orgId
                + ", memberId === " + memberId);// + ", password === " + password);
        request.params.put("callingApp", Play.configuration.getProperty("application.name"));
        request.params.put("callingAppId", Play.configuration.getProperty("application.id"));
        authMember(orgId, memberId, password, sectionId);
        if(!StringUtils.isEmpty(sectionId) && !StringUtils.isEmpty(orgId)){
            String redirectUrl = "/signupsection/"+sectionId+"?orgId="+orgId;
            flash.put(redirectUrlKey, redirectUrl);
        }
        flash.keep(redirectUrlKey);
        vedantuLoading(orgId);
    }

    public static void authentify(String username, String password, String orgId, String sectionId) throws Throwable {

        request.params.put("callingApp", Play.configuration.getProperty("application.name"));
        request.params.put("callingAppId", Play.configuration.getProperty("application.id"));
        auth(username, password, sectionId);
        if(!StringUtils.isEmpty(sectionId) && !StringUtils.isEmpty(orgId)){
            String redirectUrl = "/signupsection/"+sectionId+"?orgId="+orgId;
            flash.put(redirectUrlKey, redirectUrl);
        }
        flash.keep(redirectUrlKey);
        vedantuLoading(orgId);
    }

    public static void authentifyWithOTP(String username, String orgId, String sectionId) throws Throwable {

        Logger.log4j.info("Member Login with OTP Auth Values orgId: " + orgId+" username: "+username);
        request.params.put("callingApp", Play.configuration.getProperty("application.name"));
        request.params.put("callingAppId", Play.configuration.getProperty("application.id"));
        authentifyOTPMember(orgId,username,sectionId);
        if(!StringUtils.isEmpty(sectionId) && !StringUtils.isEmpty(orgId)){
            String redirectUrl = "/signupsection/"+sectionId+"?orgId="+orgId;
            flash.put(redirectUrlKey, redirectUrl);
        }
        flash.keep(redirectUrlKey);
        vedantuLoading(orgId);
    }

    public static void signup(String orgId, String sectionId) throws Throwable{
        request.params.put("callingApp", Play.configuration.getProperty("application.name"));
        request.params.put("callingAppId", Play.configuration.getProperty("application.id"));
        request.params.put("userId", "PUBLIC");
        request.params.put("callingUserId", "PUBLIC");
        request.params.put("profile", "STUDENT");
        boolean isOTPsignup = Boolean.parseBoolean(request.params.get("isOTPsignup"));
        if (request.params._contains("campaignAddProgram")
                && !request.params.get("campaignAddProgram").isEmpty()
                && orgId.equals(Play.configuration.getProperty("learnpedia.id"))) {
            request.params.put("autoAddCampaignProgram", "true");
            Logger.log4j.info("=== Auto Add campaign program ===");
        } else if (isOTPsignup && orgId.equals(Play.configuration.getProperty("learnpedia.id"))) {
            request.params.put("autoAddDemoProgram", "true");
            Logger.log4j.info("=== Auto Add demo program ===");
        } else if (request.params._contains("progType")
                && !request.params.get("progType").isEmpty()) {
            if (request.params.get("progType").equalsIgnoreCase("JEE")
                    || request.params.get("progType").equalsIgnoreCase("NEET")) {
                request.params.put("autoAddDemoProgram", "true");
            }
        }
        signupAndAuth(orgId);
        Logger.log4j.info("================== AFTER SIGNUP COMPLETED ===============> SECTION = "+sectionId);
        String redirectUrl = flash.get(redirectUrlKey);
        if(!StringUtils.isEmpty(sectionId) && !StringUtils.isEmpty(orgId)){
            redirectUrl = "/signupsection/"+sectionId+"?orgId="+orgId;
        }
        flash.put(redirectUrlKey, redirectUrl);
        flash.keep(redirectUrlKey);
        vedantuLoading(orgId);
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
        vedantuLoading(null);
    }

    public static void vedantuLoading(String orgId) {

        if(orgId == null)
            orgId = session.get("loginOrgId");
        Logger.log4j.info("Inside vedantu loading");
        JSONArray orgArray = Institute._getUserOrgs(getReqParams(), true);
        boolean retVal = decideFrontEndUserRedirect(orgArray);
        String fetchUrl = "/home";
        if(flash.contains(redirectUrlKey))
            fetchUrl = (flash.get(redirectUrlKey).equals("null") || flash.get(redirectUrlKey) == null)? "/home":flash.get(redirectUrlKey);
        if (retVal) {
            if(Boolean.parseBoolean(session.get("isSignup"))){
                if(Boolean.parseBoolean(session.get("autoAddDemoProgram")) && orgId.equals(Play.configuration.getProperty("learnpedia.id"))){
                    if(!session.get("progType").equalsIgnoreCase("ALL") && orgId.equals(Play.configuration.getProperty("learnpedia.id"))){
                        // GO to Home Page
                    }else if(!session.get("progType").equalsIgnoreCase("ALL")){
                        fetchUrl = "/organization/"+orgId+"/myprograms?category="+session.get("progType");
                    }else{
                        fetchUrl = "/organization/"+orgId+"/myprograms";
                    }
                } else if (Boolean.parseBoolean(session.get("autoAddCampaignProgram"))
                        && orgId.equals(Play.configuration.getProperty("learnpedia.id"))) {
                    // Go to Home Page
                }
            }
            flash.put(redirectUrlKey, fetchUrl);
            Logger.log4j.info("Redirect url : "+flash.get(redirectUrlKey));
            fetchUrl = decideRedirect(orgId, orgArray);
        }
        session.remove("isOTPsignup");
        session.remove("isOTPlogin");
        render("/loading.html", fetchUrl);
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

    private static Boolean decideFrontEndUserRedirect(JSONArray orgArray) {

        if (orgArray == null) {
            Institute.redirectToNoOrgError();
        }
        try {
            if (orgArray.length() == 1) {
                JSONObject org = orgArray.getJSONObject(0);
                String userRole = org.getString("profile");
                String orgId = org.getString("id");
                if ("FRONT_DESK_USER".equals(userRole)) {// FRONT_DESK_USER
                    Logger.log4j.info("Going to front desk user page ===============" + orgId);
                    Subscription.signUp(orgId);
                    return false;
                }
            } else if (orgArray.length() > 1) {
                for (Integer oi = 0; oi < orgArray.length(); oi++) {
                    JSONObject org = orgArray.getJSONObject(oi);
                    String userRole = org.getString("profile");
                    if ("FRONT_DESK_USER".equals(userRole)) {
                        Logger.log4j
                                .info("Going to front desk user SELECTION page ===============");
                        Subscription.postLogin(orgArray);
                        return false;
                    }
                }
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
        }
        return true;
    }

    public static void browseAsUser(String targetUserId) {
        boolean canImpersonate = false;
        try {
            request.params.put("targetUserId", session.get("callingUserId"));
            JSONObject resp = Institute._getMemberInfo(null);
            canImpersonate = resp.getJSONObject("result").getJSONObject("info").getBoolean("canImpersonate");
        } catch (Exception ex) {
        }
        if (canImpersonate) {
            session.put("userId", targetUserId);
            flash.put(redirectUrlKey, getLogoutAccessUrl());
            renderJSON(new JSONResponse(true));
        } else {
            renderJSON(new JSONResponse(new ErrorInfo("NO_SUFFICIENT_RIGHTS", Messages.get("NO_SUFFICIENT_RIGHTS"))));
        }
    }

    public static void removeBrowseAsUser() {
        session.put("userId", session.get("callingUserId"));
        flash.put(redirectUrlKey, getLogoutAccessUrl());
        renderJSON(new JSONResponse(true));
    }

    private static void postAuthentify() {
        session.put("domain", "WEB_APP");
        session.put("callingApp", "WEB_APP");
        session.put("callingAppId", play.Play.configuration.getProperty("auth.appId"));
        // _recordLogin();
    }
}
