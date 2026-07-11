package controllers;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.libs.F.Promise;
import play.libs.Images;
import play.mvc.Http.Header;
import play.mvc.Http.Request;
import play.mvc.Scope;
import play.mvc.Scope.Params;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;

public class UIComRegister extends AbstractUIController {

    public static final String redirectUrlKey = Play.configuration.getProperty("redirect.url.key");

    public static void register(String username, String code, String randomId)
            throws IOException, JSONException {
        Logger.log4j.info("cache" + Cache.get(randomId));
        Logger.log4j.info("i sent" + code);
        if (validation.hasErrors()) {
            Logger.log4j.error("trying to sign up without invite key");
            render("/Application/error.html");
        }
        if (Cache.get(randomId) == null
                || !code.toLowerCase().equals(Cache.get(randomId))) {
            renderJSON(new JSONObject("{'captcha':false}").toString());
        }
        AsyncHttpClient asynClient = new AsyncHttpClient();
        Logger.log4j.info("sending user data for sign up");
        Map<String, String[]> reqParams = Scope.Params.current().all();
        Future<Response> rsp = asynClient
                .preparePost(ClientUtil.CREATE_USER_SERVICE_URl)
                .setParameters(convert(reqParams))
                .addParameter("appId", Play.configuration.getProperty("auth.appId"))
                .addParameter("secretKey",
                Play.configuration.getProperty("auth.secretKey")).execute();
        Response r = null;
        JSONObject signUpResponse = null;
        try {
            r = rsp.get();
            String rspString = r.getResponseBody();
            asynClient.close();
            signUpResponse = new JSONObject(rspString);
            Logger.log4j.info("response after signup" + signUpResponse);
            if (!signUpResponse.get("errorMessage").toString().isEmpty()) {
                flash.put("SignUpError", signUpResponse.get("errorMessage"));
                Cache.delete(randomId);
                renderJSON(new JSONObject(rspString).toString());
            } else {
                session.put("username", username);
                signUpResponse = signUpResponse.getJSONObject("result");
                String userSessionAuthKey = signUpResponse
                        .getString("userSessionAuthKey");
                String authKey = signUpResponse.getString("authKey");
                String userId = signUpResponse.getString("userId");
                String fullname = signUpResponse.getString("fullname");
                String profilePic = signUpResponse.getString("profilePic");
                String isVerified = signUpResponse.getString("isVerified");
                session.put("authKey", authKey);
                session.put("userId", userId);
                session.put("userSessionAuthKey", userSessionAuthKey);
                session.put("fullname", fullname);
                session.put("profilePic", profilePic);
                session.put("isVerified", isVerified);
                Cache.delete(randomId);
                renderJSON(new JSONObject(rspString).toString());
            }

        } catch (InterruptedException e) {
            Logger.log4j.error(e.getLocalizedMessage());
        } catch (ExecutionException e) {
            Logger.log4j.error(e.getLocalizedMessage());
        } catch (JSONException e) {
            Logger.log4j.error(e.getLocalizedMessage());
        }
    }

    public static void createProfile() {
        Map<String, Object> allParams = getReqParams();
        Promise<JSONResponseWrapper> promise = client(ClientUtil.PROFILE_WEB_SERVICE_URL
                + "/profiles/createprofile", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject createProfile = getJSON(promise);
        Promise<JSONResponseWrapper> promiseEdu = client(
                ClientUtil.PROFILE_WEB_SERVICE_URL + "/profiles/addEducational",
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promiseEdu);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject saveEduProfile = getJSON(promiseEdu);
    }

    public static void userNameForgotPass() {
        _setAppParams();
        Promise<JSONResponseWrapper> promise = client(ClientUtil.USER_SERVICE_URL
                + "/users/sendForgotPasswordMail", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject forgot = getJSON(promise);
        renderJSON(forgot.toString());
    }

    public static void memberForgotPass() {
        _setAppParams();
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/sendForgotPasswordMail", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject forgot = getJSON(promise);
        renderJSON(forgot.toString());
    }

    public static void resetpassword(@Required String email,
            @Required String userId,
            @Required String code,
            String orgId) {
        changePasswordPage(email, userId, code, orgId);
    }

    public static void changePasswordPage(@Required String emailId, @Required String userId,
            @Required String code, String orgId) {
        if (Validation.hasErrors()) {
            Logger.log4j
                    .error("trying to access change password illegally through url alone");
            render("/Application/error.html");
        }
        UIComSecurity._logout();
        render("UIComRegister/changePasswordPage.html", emailId, userId, code);
    }

    public static void unsubscribeEmail(@Required String mailCategory, @Required String user) {
        String userId = user;
        if (Validation.hasErrors()) {
            Logger.log4j
                    .error("trying to access unsubscribe email illegally through url alone");
            render("/Application/error.html");
        }
        render("UIComRegister/unsubscribeEmail.html", mailCategory, userId);
    }

    public static void unsubscribeFromEmail() {
        _setAppParams();
        Promise<JSONResponseWrapper> promise = client(
                    ClientUtil.USER_SERVICE_URL + "/users/unsubscribeEmail",
                    null);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
            renderJSON(resp.toString());
    }

    public static void setPassword() {
        _setAppParams();
        try {
            Promise<JSONResponseWrapper> promise = client(
                    ClientUtil.USER_SERVICE_URL + "/users/updateUserForgottenPassword",
                    null);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            JSONObject resp = getJSON(promise);
            if (resp != null && resp.has("errorCode")
                    && StringUtils.isNotEmpty(resp.getString("errorCode"))) {
                renderJSON(resp.toString());
            } else if (resp != null && resp.has("result")
                    && StringUtils.isNotEmpty(resp.getString("result"))) {
                renderJSON(resp.toString());
            } else {
                render("/Application/error.html");
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage(), ex);
        }
    }

    public static void index(String orgId) {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control",
                "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        JSONObject org = null;
        if (StringUtils.isEmpty(orgId)) {
            org = null; //TODO Get Org Basic Information
            boolean allowMultiOrgLogin = true;
            allowMultiOrgLogin = Boolean.parseBoolean(play.Play.configuration.getProperty("allow.multiple.org.login"));
            if(!allowMultiOrgLogin){
                String msg = play.i18n.Messages.get("MULTI_ORG_LOGIN_NOT_ALLOWED");
                render("UIComRegister/msgPage.html",msg);
            }
        } else {
            org = _getOrgInfo(orgId);
        }
        flash.keep(UIComSecurity.redirectUrlKey);
        render(org);
    }

    public static void indexSlug(@Required String orgSlugId) {
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control",
                "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        JSONObject org = null;
        if (StringUtils.isEmpty(orgSlugId)) {
            org = null; //TODO Get Org Basic Information
        } else {
            org = _getOrgInfoBySlug(orgSlugId);
        }
        flash.keep(UIComSecurity.redirectUrlKey);
        render(org);
    }
    private static JSONObject _getOrgInfoByReferer(@Required Header referer) throws UnsupportedEncodingException {
        _setAppParams();
        //referer = URLDecoder.decode(referer, "UTF-8");
        Map<String, String> allHeaders = new HashMap<String, String>();
        allHeaders.put("referer", referer.value());
        /*TEST CODE - allHeaders.put("referer", "https://www.vedantu.com");*/
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getOrganizationByReferer", null, allHeaders);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject org = getJSON(promise);
        org = ResponseUtil.checkResponse(org);
        return org;
    }

    private static JSONObject _getOrgInfoBySlug(@Required String orgSlugId) {
        _setAppParams();
        request.params.put("slug", orgSlugId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getOrganizationBySlug", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject org = getJSON(promise);
        org = ResponseUtil.checkResponse(org);
        return org;
    }

    public static void getOrgsList() {
        Map<String, Object> allParams = getReqParams();
        allParams.put("status", "APPROVED");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getOrganizations", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject orgs = getJSON(promise);
        orgs = ResponseUtil.checkResponse(orgs);
        flash.keep(UIComSecurity.redirectUrlKey);
        render("UIComRegister/orgList.html", orgs);
    }

    private static JSONObject _getOrgInfo(@Required String orgId) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getOrganization", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject instituteResponse = getJSON(promise);
        instituteResponse = ResponseUtil.checkResponse(instituteResponse);
        flash.keep(UIComSecurity.redirectUrlKey);
        return instituteResponse;
    }

    public static void askUserEmail() {
        render("UIComRegister/userEmailId.html");
    }

    public static void verifyEmail(@Required String email,
            @Required String userId,
            @Required String code) throws JSONException {
        if (Validation.hasErrors()) {
            Logger.log4j.error("trying to verify email without key");
            render("/Application/error.html");
        }
        _setAppParams();
        Scope.Params.current().put("isVerified", "true");
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.USER_SERVICE_URL
                + "/users/validateEmail", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        if (resp.getString("errorCode").isEmpty()) {
            if (session.contains("userId")) {
                redirect("/emailVerified/" + userId);
            } else {
                flash.put("emailVerified", "done");
                String orgId = request.params.get("orgId");
                String instLoginPageUrl = UIComRegister._getOrgRefererUrl(orgId);
                if(!StringUtils.isEmpty(instLoginPageUrl)){
                    String redirectUrl = instLoginPageUrl + "?emailVerified=true";
                    render("UIComRegister/redirectPage.html", redirectUrl);
                } else if (StringUtils.isNotEmpty(orgId)) {
                    redirect("/login/organization/" + orgId);
                } else {
                    redirect("/login");
                }
            }
        } else {
            render("UIComRegister/verifyEmail.html", resp);
        }
    }

    public static void userCreatePassword(@Required String email,
            @Required String emailVerificationKey) {
        render("UIComRegister/userCreatePassword.html", email, emailVerificationKey);
    }

    public static void emailVerifyError() {
        //render("UIComRegister/userCreatePassword.html");
        //render("/UIComRegister/verifyEmail.html");
        render("/UIComRegister/emailVerifyError.html");
    }

    public static void resendVerifyLink() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.USER_SERVICE_URL
                + "/users/resendEmailVerification", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        renderJSON(resp.toString());
    }

    public static void captcha(String id) {
        Images.Captcha captcha = Images.captcha();
        String code = captcha.getText("#6e6e6e").toLowerCase();
        Cache.set(id, code, "10mn");
        renderBinary(captcha);
    }

    protected static Map<String, Collection<String>> convert(Map<String, String[]> reqParams) {
        Map<String, Collection<String>> convertedParams = new HashMap<String, Collection<String>>();
        if (null != reqParams && !reqParams.isEmpty()) {
            for (Entry<String, String[]> entry : reqParams.entrySet()) {
                List<String> value = null != entry.getValue() ? Arrays.asList(entry
                        .getValue()) : null;
                convertedParams.put(entry.getKey(), value);
            }
        }
        return convertedParams;
    }

    private static void _setAppParams() {
        Params p = Scope.Params.current();
        p.put("callingApp", Play.configuration.getProperty("application.name"));
        p.put("callingAppId", Play.configuration.getProperty("application.id"));
    }
    public static void showUserBlockMsg(String orgId){
        String msg = play.i18n.Messages.get("org.MEMBER_BLOCKED_BY_ADMIN");
        if(StringUtils.isNotEmpty(orgId)){
            String orgName = "";
            JSONObject org = _getOrgInfo(orgId);
            try {
                if(org!=null && org.has("result") && org.has("errorCode") && StringUtils.isEmpty(org.getString("errorCode"))){
                    orgName = org.getJSONObject("result").getString("fullName");
                    msg = "<div class='centerText fontRoboto' style='padding-bottom:10px;'>"+orgName+"</div>" + msg;
                }
            } catch (JSONException ex) {
                Logger.log4j.error(ex.getLocalizedMessage());
            }
        }
        render("UIComRegister/msgPage.html",msg);
    }
    public static void getCategories(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getCategories", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void microsite(@Required String orgSlugId){
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control",
                "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        Request request = Request.current();
        String categoryParam = request.params.get("category");
        if (categoryParam != null && !categoryParam.isEmpty()) {
            renderArgs.put("categoryName", categoryParam);
        }
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        JSONObject org = null;
        if (StringUtils.isEmpty(orgSlugId)) {
            org = null; //TODO Get Org Basic Information
        } else {
            org = _getOrgInfoBySlug(orgSlugId);
        }
        flash.keep(UIComSecurity.redirectUrlKey);
        render("UIComMicrosite/index.html",org);
    }
    public static void micrositeByOrgId(@Required String orgId){
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control",
                "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        JSONObject org = null;
        if (StringUtils.isEmpty(orgId)) {
            org = null; //TODO Get Org Basic Information
        } else {
            org = _getOrgInfo(orgId);
        }
        flash.keep(UIComSecurity.redirectUrlKey);
        render("UIComMicrosite/index.html",org);
    }

    public static void downloadApp(@Required String orgId){
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control",
                "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        JSONObject org = null;
        if (StringUtils.isEmpty(orgId)) {
            org = null; //TODO Get Org Basic Information
        } else {
            org = _getOrgInfo(orgId);
        }
        render(org);
    }
    public static void micrositeByReferer() throws UnsupportedEncodingException{
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control",
                "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        Header referer = request.headers.get("referer");
        String myDomain = play.Play.configuration.getProperty("mydomain.url");
        if(referer==null || myDomain.equals(referer.value())){
            String redirectUrl = "/login";
            render("UIComRegister/redirectPage.html", redirectUrl);
            return;
        }
        Logger.log4j.info("REFERER IN HEADER ======== "+referer);
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            String redirectUrl = "/home";
            render("UIComRegister/redirectPage.html", redirectUrl);
            return;
        }
        JSONObject org = null;
        org = _getOrgInfoByReferer(referer);
        flash.keep("signupError");
        flash.keep("loginError");
        flash.keep("lastUsername");
        flash.keep("loginType");
        flash.keep("lastMemberId");
        flash.keep(UIComSecurity.redirectUrlKey);
        render("UIComMicrosite/index.html",org);
    }
    public static JSONObject _getAdditionalSignupFields(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getOrgMemberExtraInputFields", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    public static void signupForm(){
        request.params.put("targetOrgMemberProfile", "STUDENT");
        request.params.put("checkIfSignupAllowed", "true");
        JSONObject resp = _getAdditionalSignupFields();
        render("UIComMicrosite/signup.html",resp);
    }
    public static void referralSignUpForm(){
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control",
                "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        }
        JSONObject resp = new JSONObject();
        if(request.params._contains("type")){
            try {
                resp.put("showCourseTab", false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            try {
                resp.put("showCourseTab", true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        request.params.put("targetOrgMemberProfile", "STUDENT");
        request.params.put("orgId",play.Play.configuration.getProperty("learnpedia.id"));
        request.params.put("checkIfSignupAllowed", "true");
        JSONObject additionalResp = _getAdditionalSignupFields();
        render("UIComMicrosite/newsignup.html",resp,additionalResp);
    }
    public static void quickSignup(){
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control",
                "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        JSONObject resp = new JSONObject();
        request.params.put("orgId",play.Play.configuration.getProperty("learnpedia.id"));
        String programKey = "";
        if(request.params._contains("mx_campaignAddProgram")){
            programKey = "mx_campaignAddProgram";
        }else if(request.params._contains("mx_Program")){
            programKey = "mx_Program";
        }

        if (StringUtils.isNotEmpty(session.get("userId"))) {
            Logger.log4j.info("The user is already logged in.");
            redirect("/home");
            return;
        } else if (StringUtils.isNotEmpty(request.params.get(programKey))) {
            Logger.log4j.info("UiComRegister: Auto add campaign program for signup");
            try {
                resp.put("directUser", false);
                resp.put("showCategoryFields", false);
                String contactNumber = request.params.get("Phone");
                String campaignAddProgramName = request.params.get(programKey);
                String countryCode = "91";
                if (verifyPhoneNumber(countryCode, contactNumber).getString("errorCode").isEmpty()) {
                    resp.put("isVerifiedUser", true);
                    JSONObject sendOTPResp = sendOTPToUser(contactNumber, countryCode, "");
                    resp.put("isNewPhone", sendOTPResp.getJSONObject("result").get("isNewPhone"));
                } else {
                    resp.put("isVerifiedUser", false);
                }
                resp.put("campaignAddProgram", campaignAddProgramName);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            request.params.put("targetOrgMemberProfile", "STUDENT");
            request.params.put("checkIfSignupAllowed", "true");
            JSONObject additionalResp = _getAdditionalSignupFields();
            render("UIComMicrosite/quickSignup.html",resp,additionalResp);
        } else if (request.params._contains("type")
                && request.params._contains("contactnumber")
                && request.params._contains("countrycode")) {
            String contactNumber = request.params.get("contactnumber");
            String countryCode = request.params.get("countrycode");
            String progType = request.params.get("type");
            try {
                resp.put("directUser", false);
                if(verifyPhoneNumber(countryCode, contactNumber).getString("errorCode").isEmpty()){
                    resp.put("isVerifiedUser", true);
                    JSONObject sendOTPResp = sendOTPToUser(contactNumber, countryCode, progType);
                    resp.put("isNewPhone", sendOTPResp.getJSONObject("result").get("isNewPhone"));
                }else{
                    resp.put("isVerifiedUser", false);
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
            request.params.put("targetOrgMemberProfile", "STUDENT");
            request.params.put("checkIfSignupAllowed", "true");
            JSONObject additionalResp = _getAdditionalSignupFields();
            render("UIComMicrosite/quickSignup.html",resp,additionalResp);
        }else{
            try {
                resp.put("directUser", true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            request.params.put("targetOrgMemberProfile", "STUDENT");
            request.params.put("checkIfSignupAllowed", "true");
            JSONObject additionalResp = _getAdditionalSignupFields();
            render("UIComMicrosite/quickSignup.html",resp,additionalResp);
        }
    }
    public static JSONObject getCategorySectionsForOuter(Map<String,Object> allParams){
        String orgId = play.Play.configuration.getProperty("learnpedia.id");
        allParams.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getCategorySections", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
//        render("UIComMicrosite/categorySections.html",resp);
    }
    public static void getCategorySections(){
        if(session.contains("userId") && !StringUtils.isEmpty(session.get("userId"))){
            request.params.put("excludeSubscribed", "true");
        }
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getCategorySections", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render("UIComMicrosite/categorySections.html",resp);
    }
    public static void getSignleProgramPopup(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getCategorySection", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render("UIComMicrosite/singleProgramPopup.html",resp);
    }

    public static void verifyContactNumber(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/verifyContactNumber", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static JSONObject verifyPhoneNumber(String countryCode, String contactNumber){
        request.params.put("countryCode", countryCode);
        request.params.put("contactNumber", contactNumber);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/verifyContactNumber", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static void contactNumberPopup(){
        render("UIComMicrosite/contactNumberPopup.html");
    }

    public static void authoriseContactNumber(){
        request.params.put("userId", session.get("userId"));
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/validateContactNumber", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void sendOTP(){
        String respJSON = request.params.get("respJSON");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/sendOTP", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        try {
            JSONObject obj = (JSONObject) resp.get("result");
            if (StringUtils.isNotEmpty(session.get("userId"))) {
                Logger.log4j.info("The user is already logged in. Setting user OTP in session");
                Logger.log4j.info("The user is already logged in. Setting isUserLoggedIn to true");
                obj.put("isUserLoggedIn", true);
                session.put("OTP", obj.get("OTP"));
            }else{
                // clearing the session to remove any old persisting values.
                session.clear();
                Cache.clear();
                Logger.log4j.info("The user is not logged in. Setting user OTP in session");
                session.put("fullname",obj.get("fullname"));
                session.put("contactNumber",obj.get("contactNumber"));
                session.put("OTP", obj.get("OTP"));
                Logger.log4j.info("The user is not logged in. Setting isUserLoggedIn to false");
                obj.put("isUserLoggedIn", false);
                if(request.params._contains("campaignCode")){
                    obj.put("campaignCode",request.params.get("campaignCode"));
                }
            }
            obj.remove("OTP");
            resp.remove("result");
            resp.put("result", obj);
        } catch (JSONException e) {
            Logger.log4j.error("Error while removing OTP from response", e);
        }
        Logger.log4j.info(resp.toString());
        if(!StringUtils.isEmpty(respJSON)){
            renderJSON(resp.toString());
        }
        else{
            render("UIComMicrosite/otpPopup.html",resp);
        }
    }

    public static JSONObject sendOTPToUser(String contactNumber, String countryCode, String progType){
        request.params.put("contactNumber", contactNumber);
        request.params.put("countryCode", countryCode);
        request.params.put("progType", progType);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/sendOTP", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        try {
            JSONObject obj = (JSONObject) resp.get("result");
            if (StringUtils.isNotEmpty(session.get("userId"))) {
                Logger.log4j.info("The user is already logged in. Setting user OTP in session");
                Logger.log4j.info("The user is already logged in. Setting isUserLoggedIn to true");
                obj.put("isUserLoggedIn", true);
                session.put("OTP", obj.get("OTP"));
            }else{
                // clearing the session to remove any old persisting values.
                session.clear();
                Cache.clear();
                Logger.log4j.info("The user is not logged in. Setting user OTP in session");
                session.put("contactNumber",obj.get("contactNumber"));
                session.put("OTP", obj.get("OTP"));
                Logger.log4j.info("The user is not logged in. Setting isUserLoggedIn to false");
                obj.put("isUserLoggedIn", false);
            }
            obj.remove("OTP");
            resp.remove("result");
            resp.put("result", obj);
        } catch (JSONException e) {
            Logger.log4j.error("Error while removing OTP from response", e);
        }
        Logger.log4j.info("Response in sendOTPToUser : "+ resp.toString());
        return resp;
    }

    public static void validateOTP(){
        request.params.put("sessionOTP",session.get("OTP"));
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/validateOTP", null);
        Logger.log4j.info("BEFORE AWAIT in validate OTP");
        await(promise);
        Logger.log4j.info("AFTER AWAIT in validate OTP");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        try {
            if(StringUtils.isEmpty(resp.get("errorMessage").toString())){
                session.remove("OTP");
                if (StringUtils.isNotEmpty(session.get("userId"))) {
                    Logger.log4j.info("The user is already logged in. Setting isUserLoggedIn to true");
                    ((JSONObject)resp.get("result")).put("isUserLoggedIn", true);
                }else{
                    Logger.log4j.info("The user is not logged in. Setting isUserLoggedIn to false");
                    ((JSONObject)resp.get("result")).put("isUserLoggedIn", false);
                }
            }
        } catch (JSONException e) {
            Logger.log4j.error("Error while removing OTP from session", e);
        }
        renderJSON(resp.toString());
    }

    public static void checkUsernameExist(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.USER_SERVICE_URL
                + "/users/doesUserExists", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void checkContactNumberExist(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/doesContactNumberExists", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void isValidReferralCode(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/isValidReferralCode", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void isValidPromoCode(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/campaignCodes/isValidCampaignCode", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void getiframe(String loginError,String lastUsername,
            String lastMemberId,String signupError,String emailVerified){

        String myDomain = play.Play.configuration.getProperty("mydomain.url");
        String url = myDomain + "microsite";
        String joinChar = "&";
        if(loginError != null){
            url = UIComSecurity._getMicrositeUrlWithError(url,loginError,lastUsername,lastMemberId);
        }else if(signupError != null){
            url = UIComSecurity._getMicrositeUrlWithError(url,signupError);
        }else{
            joinChar = "?";
        }
        if(emailVerified!=null){
            url += joinChar+"emailVerified=true";
        }
        flash.keep("signupError");
        flash.keep("loginError");
        flash.keep("lastUsername");
        flash.keep("loginType");
        flash.keep("lastMemberId");
        flash.keep(UIComSecurity.redirectUrlKey);
        render(url);
    }
    public static void getFavIconByReferer() throws UnsupportedEncodingException, JSONException{
        response.setHeader("Pragma", "no-cache");
        response.setHeader("Cache-Control",
                "no-cache, max-age=0, must-revalidate, no-store");
        response.setHeader("Expires", "0");
        Header referer = request.headers.get("referer");
        Logger.log4j.info("REFERER IN HEADER ======== "+referer);
        JSONObject org = null;
        JSONObject favIconObj = new JSONObject();
        String defaultFavIconUrl = Play.configuration.getProperty("microsite.default.favIconUrl");
        favIconObj.put("icon", defaultFavIconUrl);
        if (referer==null) {
            org = null; //TODO Get Org Basic Information
        } else {
            org = _getOrgInfoByReferer(referer);
            if(org.getString("errorCode").isEmpty()){
                org = org.getJSONObject("result");
                String slug = org.getString("slug");
                String favIconUrl = Play.configuration.getProperty("microsite.client."+slug+".favIconUrl");
                if(favIconUrl!=null && !StringUtils.isEmpty(favIconUrl)){
                    favIconObj.put("icon", favIconUrl);
                }
            }
        }
        renderJSON(favIconObj.toString());
    }
    private static JSONObject _getOrgInfoSyncCaller(@Required String orgId) {
        request.params.put("orgId", orgId);
        JSONObject instituteResponse = syncCaller(ClientUtil.ORGANIZATION_SERVICE_URL+"/organizations/getOrganization", null);
        instituteResponse = ResponseUtil.checkResponse(instituteResponse);
        flash.keep(UIComSecurity.redirectUrlKey);
        return instituteResponse;
    }
    public static String _getOrgRefererUrl(String orgId){
        if(StringUtils.isEmpty(orgId)){
            return null;
        }
        flash.keep(UIComSecurity.redirectUrlKey);
        JSONObject org = _getOrgInfoSyncCaller(orgId);
        String referer = "";
        try {
            if(org!=null && StringUtils.isEmpty(org.getString("errorCode"))){
                JSONObject result = org.getJSONObject("result");
                String refererStr = result.getString("referer");
                if(refererStr!=null && !StringUtils.isEmpty(refererStr) && !refererStr.equals("null")){
                    String protocol = play.Play.configuration.getProperty("microsite.protocol");
                    if(protocol != null){
                        referer = protocol + refererStr;
                    }
                }
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex);
        }
        return referer;
    }
    public static void showWelcomeMessage(){
        JSONObject resp = new JSONObject();
        if(session.contains("showWelcomeMessage")){
            try {
                resp.put("showWelcomeMessage", true);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }else{
            try {
                resp.put("showWelcomeMessage", false);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        render("/UIComPushNotifications/promoPopup.html",resp);
    }
    public static void disableWelcomeMessage(){
        JSONObject resp = new JSONObject();
        if(session.contains("showWelcomeMessage")){
            session.remove("showWelcomeMessage");
        }
        try {
            resp.put("deleted", true);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        renderJSON(resp.toString());
    }
}
