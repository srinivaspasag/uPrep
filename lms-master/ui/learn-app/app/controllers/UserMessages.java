/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import controllers.AbstractUIController.JSONResponseWrapper;
import play.Logger;
import play.data.validation.Required;
import play.libs.F.Promise;
import play.mvc.Scope;
import play.mvc.With;
import util.ClientUtil;
import util.Validation;

@With(Security.class)
public class UserMessages extends AbstractUIController {

    static final String className = UserMessages.class.getSimpleName();

    public static void getInboxConversations(){
        Map<String, Object> allParams = getReqParams();
        JSONObject msgs = _getConversationSummaries(allParams);
        render("tags/msgs.html",msgs);
     }

    public static void inbox(){
        try{
            Application.recordActivity(ClientUtil.ActivityPages.MSG_INBOX,ClientUtil.ActivityAction.OPEN,null,null);
        }catch(Exception ex){}
        render(renderTheme(getHTMLFilePath(className)));
    }

    public static void inboxDirect(String orgId){
        orgId = session.get("loginOrgId");
        JSONObject myOrgInfo = Institute._setOrgParams(orgId);
        flash.put("ENTRY", "DIRECT");
        String includeInstFile = renderTheme(orgId,getHTMLFilePath(className,"inbox"));
        render(renderTheme(orgId, getHTMLFilePath(null,"header")),includeInstFile,myOrgInfo);
     }

    public static void currentMessages(){
        Map<String, Object> allParams = getReqParams();
        JSONObject resp = _getConversationSummaries(allParams);
        JSONObject unread = _getNotifications();
        render(renderTheme(getHTMLFilePath(className)),resp,unread);
    }

    private static JSONObject _getConversationSummaries(Map<String, Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.MSG_WEB_SERVICE_URL
                + "/messageCenter/getConversationSummaries", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }

    public static void openConversation(String orgId){
        orgId = session.get("loginOrgId");
        JSONObject myOrgInfo = Institute._setOrgParams(orgId);
        Map<String, Object> allParams = getReqParams();
        try{
            Application.recordActivity(ClientUtil.ActivityPages.MSG_CONVERSATION,ClientUtil.ActivityAction.OPEN,null,null);
        }catch(Exception ex){}
        JSONObject resp = _getOneConversation(allParams);
        JSONObject firstMessageResp = _getFirstMessage(resp);
        JSONObject users = _getConversationUsers(allParams);
        String includeInstFile = "UserMessages/header.html";
        String includePgFile = "UserMessages/conversation.html";
        render("Institute/header.html",includeInstFile,includePgFile,resp,firstMessageResp,users,myOrgInfo);
    }

    public static void conversation(){
//        String orgId = session.get("loginOrgId");
        Map<String, Object> allParams = getReqParams();
        try{
            Application.recordActivity(ClientUtil.ActivityPages.MSG_CONVERSATION,ClientUtil.ActivityAction.OPEN,null,null);
        }catch(Exception ex){}
        JSONObject resp = _getOneConversation(allParams);
        JSONObject firstMessageResp = _getFirstMessage(resp);
        JSONObject users = _getConversationUsers(allParams);
        render(renderTheme(getHTMLFilePath(className)),resp,firstMessageResp,users);
    }

    public static void conversationDirect(String orgId,@Required String userConversationId){
        orgId = session.get("loginOrgId");
        JSONObject myOrgInfo = Institute._setOrgParams(orgId);
        try{
             Application.recordActivity(ClientUtil.ActivityPages.MSG_CONVERSATION,ClientUtil.ActivityAction.OPEN,null,null);
         }catch(Exception ex){}
        Map<String, Object> allParams = getReqParams();
        JSONObject resp = _getOneConversation(allParams);
        JSONObject firstMessageResp = _getFirstMessage(resp);
         JSONObject users = _getConversationUsers(allParams);
        flash.put("ENTRY", "DIRECT");
        String includeInstFile = renderTheme(orgId,getHTMLFilePath(className,"conversation"));
        render(renderTheme(orgId, getHTMLFilePath(null,"header")),includeInstFile,resp,firstMessageResp,users,myOrgInfo);
    }

    private static JSONObject _getConversationUsers(Map<String, Object> allParams){
        allParams.put("start",0);
        allParams.put("size",100);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.MSG_WEB_SERVICE_URL
                + "/messageCenter/getConversationUsers", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }

    private static JSONObject _getOneConversation(Map<String, Object> allParams){
        allParams.put("status", "READ");
        Promise<JSONResponseWrapper> promise1 = client(ClientUtil.MSG_WEB_SERVICE_URL
                + "/messageCenter/markConversation", allParams);
        await(promise1);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.MSG_WEB_SERVICE_URL
                + "/messageCenter/getConversationSummary", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        return resp;
    }
    private static JSONObject _getFirstMessage(JSONObject convResp){
        try {
            JSONObject summary = convResp.getJSONObject("result").getJSONObject("summary");
            String firstMessageId = summary.getString("firstMessageId");
            String convId = summary.getString("conversationId");
            Map<String, Object> allParams = new HashMap<String, Object>();
            allParams.put("messageId", firstMessageId);
            allParams.put("conversationId", convId);
            JSONObject resp = _getMessage(allParams);
            return resp;
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getLocalizedMessage());
        }
        return null;
    }

    private static JSONObject _getMessage(Map<String, Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.MSG_WEB_SERVICE_URL
                + "/messageCenter/getMessage", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }

    protected static JSONObject _getNotifications(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.MSG_WEB_SERVICE_URL
                + "/messageCenter/getUserMailBoxInfo", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }

}
