/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Scope;
import uicom.response.ErrorInfo;
import uicom.response.JSONResponse;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;
/**
 *
 * @author anirban
 */
public class UIComUserSettings extends AbstractUIController{
    public static void updateUsername(String newUsername,String password){
        try{
            JSONObject loginResp=null;
            String username=session.get("username");
            String orgId=session.get("loginOrgId");
            String memberId=session.get("loginMemberId");
            Boolean isOrgLogin=false;
            if(StringUtils.isNotEmpty(username)){
                request.params.put("username",username);
                loginResp=ResponseUtil.checkResponse(syncCaller(ClientUtil.LOGIN_SERVICE_URL,null));
            }else if(StringUtils.isNotEmpty(orgId)&&StringUtils.isNotEmpty(memberId)){
                request.params.put("memberId",memberId);
                request.params.put("orgId",orgId);
                loginResp=ResponseUtil.checkResponse
                        (syncCaller(ClientUtil.LOGIN_MEMBER_SERVICE_URL,null));
                isOrgLogin=true;
            }
            String loginRespErrorCode=loginResp.getString("errorCode");
            if(StringUtils.isNotEmpty(loginRespErrorCode)){
                renderJSON(new JSONResponse(new ErrorInfo("CURRENT_WRONG_PASSWORD")));
            }

            Scope.Params.current().put("targetUserId", session.get("userId"));
            Scope.Params.current().put("newPassword",password);
            JSONObject resp=syncCaller(ClientUtil.USER_SERVICE_URL
                            + "/users/updateUsername",null);
            if(isOrgLogin){
                resp.put("isOrgLogin",true);
            }
            if(StringUtils.isEmpty(resp.getString("errorCode"))){
                session.put("username",newUsername);
                session.remove("loginMemberId");
                session.remove("loginOrgId");
            }
            renderJSON(resp.toString());
        }catch(Exception e){
            renderJSON(new JSONResponse("","Service Unavailable","SERVICE_ERROR"));
        }
    }
    public static void updateUser(String email){
        Map<String, Object> allParams = getReqParams();
        allParams.put("targetUserId", session.get("userId"));
        Promise<JSONResponseWrapper> promise = client(ClientUtil.USER_SERVICE_URL + "/users/updateUser",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        try {
            if(StringUtils.isEmpty(resp.getString("errorCode"))){
                session.put("username",resp.getJSONObject("result").get("username"));
            }
        } catch (JSONException e) {
            renderJSON(new JSONResponse("","Service Unavailable","SERVICE_ERROR"));
        }
        renderJSON(resp.toString());
    }

    public static void updateUserEmailInOrgMember(String email){
        Map<String, Object> allParams = getReqParams();
        allParams.put("targetUserId", session.get("userId"));
        // This is the dummy field to handle Missing Params. We are not going to use this field
        allParams.put("targetOrgMemberId", session.get("userId"));
        Promise<JSONResponseWrapper> promise = client(ClientUtil.ORGANIZATION_SERVICE_URL + "/members/updateMemberEmail",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        renderJSON(resp.toString());
    }

    public static void changePassword(String newPassword,String currentPass)
            throws Throwable{
        JSONObject loginResp=null;
        request.params.put("password",currentPass);
        String username=session.get("username");
        String orgId=session.get("loginOrgId");
        String memberId=session.get("loginMemberId");
        if(StringUtils.isNotEmpty(username)){
            request.params.put("username",username);
            loginResp=ResponseUtil.checkResponse(syncCaller(ClientUtil.LOGIN_SERVICE_URL,null));
        }else if(StringUtils.isNotEmpty(orgId)&&StringUtils.isNotEmpty(memberId)){
            request.params.put("memberId",memberId);
            request.params.put("orgId",orgId);
            loginResp=ResponseUtil.checkResponse(syncCaller(ClientUtil.LOGIN_MEMBER_SERVICE_URL,null));
        }
        if(StringUtils.isNotEmpty(loginResp.getString("errorCode"))){
            renderJSON(new JSONResponse(new ErrorInfo("CURRENT_WRONG_PASSWORD")));
        }
        Scope.Params.current().put("targetUserId", session.get("userId"));
        JSONObject resp=syncCaller(ClientUtil.USER_SERVICE_URL
                + "/users/updateUserPassword",null);
        renderJSON(resp.toString());
    }

    public static void resendVerificationLink(){
        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.USER_SERVICE_URL + "/users/resendEmailVerification",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void unsetMyEmail(){
        F.Promise<AbstractUIController.JSONResponseWrapper> promise = client(
                ClientUtil.USER_SERVICE_URL + "/users/unsetEmail",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    protected static JSONObject _getUserInfo(Map<String, Object> allParams) {
        if(allParams==null){
            allParams=getReqParams();
        }
        allParams.put("targetUserId",session.get("userId"));
        Promise<JSONResponseWrapper> promise = client(ClientUtil.USER_SERVICE_URL
                + "/users/getUserSelfFullProfile", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject memberResponse = ResponseUtil.checkResponse(getJSON(promise));
        return memberResponse;
    }
    public static void subscribeEmail(){
        Map<String, Object> allParams=getReqParams();
        allParams.put("targetUserId",session.get("userId"));
        Promise<JSONResponseWrapper> promise = client(ClientUtil.USER_SERVICE_URL
                + "/users/subscribeEmail", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void unsubscribeEmail(){
        Map<String, Object> allParams=getReqParams();
        allParams.put("targetUserId",session.get("userId"));
        Promise<JSONResponseWrapper> promise = client(ClientUtil.USER_SERVICE_URL
                + "/users/unsubscribeEmail", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    /* USER PUSH NOTIFICATION */
    public static void getUserPushNotifications(){
        JSONObject userInfo = _getUserInfo(null);
        render("/UIComPushNotifications/home.html",userInfo);
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
            Logger.log4j.info("Removing promp pop up from session");
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
