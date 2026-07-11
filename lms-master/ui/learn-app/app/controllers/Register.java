package controllers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.cache.Cache;
import play.data.validation.Required;
import play.data.validation.Validation;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Scope;
import play.mvc.Scope.Params;
import util.ClientUtil;
import util.ResponseUtil;

public class Register extends AbstractUIController {

    public static JSONObject sendOTPToUser(String contactNumber, String countryCode){
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
                session.put("contactNumber",obj.get("contactNumber"));
                session.put("OTP", obj.get("OTP"));
                obj.put("isUserLoggedIn", true);
            }else{
                // clearing the session to remove any old persisting values.
                session.clear();
                Cache.clear();
                Logger.log4j.info("The user is not logged in. Setting user OTP in session");
                Logger.log4j.info("The user is not logged in. Setting isUserLoggedIn to false");
                session.put("contactNumber",obj.get("contactNumber"));
                session.put("OTP", obj.get("OTP"));
                obj.put("isUserLoggedIn", false);
            }
            obj.remove("OTP");
            resp.remove("result");
            resp.put("result", obj);
        } catch (JSONException e) {
            Logger.log4j.error("Error while removing OTP from response", e);
        }
        Logger.log4j.info("Response in sendOTP : "+ resp.toString());
        return resp;
    }

    public static JSONObject getCategoriesForOuterProgramsPage(Map<String,Object> allParams){
        String orgId = play.Play.configuration.getProperty("learnpedia.id");
        allParams.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getCategories", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static void getCategories(){
        String orgId = session.get("loginOrgId");
        if(orgId == null){
            orgId = play.Play.configuration.getProperty("learnpedia.id");
        }
        request.params.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/organizations/getCategories", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void getOrgBoards(){
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        JSONObject resp=_getOrgBoards(null);
        renderJSON(resp.toString());
    }

    protected static JSONObject _getOrgBoards(Map<String, Object> allParams){
        String ownerId=request.params.get("orgId");
        if(StringUtils.isEmpty(ownerId)){
            ownerId=session.get("orgId");
        }
        if(allParams==null){
            allParams=getReqParams();
        }
        allParams.put("context", "ORG");
        allParams.put("ownerId",ownerId);
        JSONObject resp=_getBoards(allParams);
        return resp;
    }

    protected static JSONObject _getBoards(Map<String, Object> allParams){
        if(allParams != null){
            allParams.put("recordState", "ACTIVE");
        }else{
            request.params.put("recordState", "ACTIVE");
        }
        F.Promise<JSONResponseWrapper> promise =
                client(ClientUtil.BOARDS_SERVICE_URL + "/boards/getChildren",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static JSONObject verifyPhoneNumber(String countryCode, String contactNumber){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/verifyContactNumber", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }

    public static void verifyContactNumber(){
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/verifyContactNumber", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void authoriseContactNumber(){
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        request.params.put("userId", session.get("userId"));
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL
                + "/members/validateContactNumber", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void sendOTP(String contactNumber, String countryCode){
        JSONObject resp = new JSONObject();
        try {
            String internalVerificationRequest = request.params._contains("internalVerificationRequest") ? request.params.get("internalVerificationRequest") : "false";
            boolean verifyPhoneNumberResponse = verifyPhoneNumber(countryCode, contactNumber).getString("errorCode").isEmpty();
            if(verifyPhoneNumberResponse || Boolean.valueOf(internalVerificationRequest)){
                resp.put("isVerifiedUser", true);
                if(request.params.get("contactNumber").equals(session.get("contactNumber"))){
                    if(session.contains("OTP")){
                        request.params.put("existingOTP", session.get("OTP"));
                    }else{
                        session.clear();
                    }
                }else{
                    session.clear();
                }
                JSONObject sendOTPResp = sendOTPToUser(contactNumber, countryCode);
                resp.put("isNewPhone", sendOTPResp.getJSONObject("result").get("isNewPhone"));
            }else{
                resp.put("isVerifiedUser", false);
            }
        } catch (JSONException e) {
            Logger.log4j.error("Error while sending OTP", e);
        }
        renderJSON(resp.toString());
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
            render(renderTheme(getHTMLFilePath("errors","404")));
        }
        Security._logout();
        render(renderTheme(null,getHTMLFilePath("Register","changePasswordPage")), emailId, userId, code);
    }

    public static void verifyEmail(@Required String email,
            @Required String userId,
            @Required String code) throws JSONException {
        if (Validation.hasErrors()) {
            Logger.log4j.error("trying to verify email without key");
            render(renderTheme(getHTMLFilePath("errors","404")));
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
                if (StringUtils.isNotEmpty(orgId)) {
                    redirect("/");
                } else {
                    redirect("/login");
                }
            }
        } else {
            redirect("/");
        }
    }

    public static void setPassword(String orgId) {
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
                render(renderTheme(orgId, getHTMLFilePath("errors","404")));
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage(), ex);
        }
    }

    private static void _setAppParams() {
        Params p = Scope.Params.current();
        p.put("callingApp", Play.configuration.getProperty("application.name"));
        p.put("callingAppId", Play.configuration.getProperty("application.id"));
    }

    public static String _getOrgRefererUrl(String orgId){
        if(StringUtils.isEmpty(orgId)){
            return null;
        }
        flash.keep(Security.redirectUrlKey);
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

    public static JSONObject _getOrgInfoSyncCaller(@Required String orgId) {
        request.params.put("orgId", orgId);
        JSONObject instituteResponse = syncCaller(ClientUtil.ORGANIZATION_SERVICE_URL+"/organizations/getOrganization", null);
        instituteResponse = ResponseUtil.checkResponse(instituteResponse);
        flash.keep(Security.redirectUrlKey);
        return instituteResponse;
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

    public static void resendVerifyLink() {
        String orgId = session.get("loginOrgId");
        request.params.put("orgId", orgId);
        String userId = session.get("userId");
        request.params.put("userId", userId);
        _setAppParams();
        Promise<JSONResponseWrapper> promise = client(ClientUtil.USER_SERVICE_URL
                + "/users/resendEmailVerification", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        renderJSON(resp.toString());
    }

}
