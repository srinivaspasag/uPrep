/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.data.validation.Required;
import play.libs.F.Promise;
import play.mvc.Scope;
import play.mvc.With;
import uicom.util.ClientUtil;
import uicom.util.Validation;

/**
 *
 * @author anirban
 */
@With(Security.class)
    public class UserMessages extends AbstractUIController  {
    public static void inbox(){
        try{
            Application.recordActivity(ClientUtil.ActivityPages.MSG_INBOX,ClientUtil.ActivityAction.OPEN,null,null);
        }catch(Exception ex){}
        render();
    }
    public static void openInbox(String orgId){
        try{
            Application.recordActivity(ClientUtil.ActivityPages.MSG_INBOX,ClientUtil.ActivityAction.OPEN,null,null);
        }catch(Exception ex){}
        JSONObject myOrgInfo = Institute._setOrgParams(orgId);
        String includeInstFile = "UserMessages/header.html";
        String includePgFile = "UserMessages/inbox.html";
        render("Institute/header.html",includePgFile,includeInstFile,myOrgInfo);
    }
    public static void inboxDirect(String orgId){
       JSONObject myOrgInfo = Institute._setOrgParams(orgId);
       flash.put("ENTRY", "DIRECT");
       String includePgFile = "UserMessages/inbox.html";
       String includeInstFile = "UserMessages/header.html";
       String includeName = "Institute/header.html";
       render("Application/myPages.html", includeName,includeInstFile,includePgFile,myOrgInfo);
    }
    public static void getInboxConversations(){
       Map<String, Object> allParams = getReqParams();
       JSONObject msgs = _getConversationSummaries(allParams);
       render("tags/messages/msgs.html",msgs);
    }
    public static void getMoreInboxConversations(){
       Map<String, Object> allParams = getReqParams();
       /*Promise<JSONResponseWrapper> promise = client(ClientUtil.MSG_WEB_SERVICE_URL
                + "/messageCenter/getMoreConversationSummaries", allParams);
       Logger.log4j.info("BEFORE AWAIT");
       await(promise);
       Logger.log4j.info("AFTER AWAIT");
       JSONObject msgs = getJSON(promise);
       msgs = Validation.verifyResponse(msgs);*/
       JSONObject msgs = _getConversationSummaries(allParams);
       render("tags/messages/msgs.html",msgs);
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
    public static void notifyConversations(){
        Map<String, Object> allParams = getReqParams();
        JSONObject resp = _getConversationSummaries(allParams);
        JSONObject unread = _getNotifications();
        render("tags/messages/noti.html",resp,unread);
    }
    public static void postMessage(){
        Map<String, Object> allParams = getReqParams();
        Promise<JSONResponseWrapper> promise = client(ClientUtil.MSG_WEB_SERVICE_URL
                + "/messageCenter/sendMessage", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        renderJSON(resp.toString());
    }
    public static void postMessageUi(){
        String msgStr = Scope.Params.current().get("message");
        JSONObject _msg = null;
        try {
            _msg = new JSONObject(msgStr);
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
        }
        render("tags/messages/postMessage.html",_msg);
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
    public static void conversation(){
        Map<String, Object> allParams = getReqParams();
        try{
            Application.recordActivity(ClientUtil.ActivityPages.MSG_CONVERSATION,ClientUtil.ActivityAction.OPEN,null,null);
        }catch(Exception ex){}
        JSONObject resp = _getOneConversation(allParams);
        JSONObject firstMessageResp = _getFirstMessage(resp);
        JSONObject users = _getConversationUsers(allParams);
        render(resp,firstMessageResp,users);
    }
    public static void openConversation(String orgId){
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
    public static void conversationDirect(String orgId,@Required String userConversationId){
       JSONObject myOrgInfo = Institute._setOrgParams(orgId);
       try{
            Application.recordActivity(ClientUtil.ActivityPages.MSG_CONVERSATION,ClientUtil.ActivityAction.OPEN,null,null);
        }catch(Exception ex){}
       Map<String, Object> allParams = getReqParams();
       JSONObject resp = _getOneConversation(allParams);
       JSONObject firstMessageResp = _getFirstMessage(resp);
        JSONObject users = _getConversationUsers(allParams);
       flash.put("ENTRY", "DIRECT");
       String includeInstFile = "UserMessages/header.html";
       String includePgFile = "UserMessages/conversation.html";
       String includeName = "Institute/header.html";
       render("Application/myPages.html",includeInstFile,includeName,includePgFile,resp,firstMessageResp,users,myOrgInfo);
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
    private static JSONObject _getMessageSummaries(Map<String, Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.MSG_WEB_SERVICE_URL
                + "/messageCenter/getMessageSummaries", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }
    private static JSONObject _getMessageSummariesBefore(Map<String, Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.MSG_WEB_SERVICE_URL
                + "/messageCenter/getMessageSummariesBefore", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }
    /*public static void getFirstMessage(){
        Map<String, Object> allParams = getReqParams();
        JSONObject msg = _getMessage(allParams);
        JSONObject users = _getConversationUsers(allParams);
        render("tags/messages/convOne.html",msg,users);
    }*/
    public static void getTooltipUsers(){
        Map<String, Object> allParams = getReqParams();
        JSONObject users = _getConversationUsers(allParams);
        render("tags/widgets/toolTipUsers.html",users);
    }
    public static void getMessagesBefore(){
        Map<String, Object> allParams = getReqParams();
        JSONObject msgs = _getMessageSummariesBefore(allParams);
        render("UserMessages/getMessages.html",msgs);
    }
    public static void getMessages(){
        Map<String, Object> allParams = getReqParams();
        JSONObject msgs = _getMessageSummaries(allParams);
        render(msgs);
    }
    public static void deleteConversation(){
        Map<String, Object> allParams = getReqParams();
        Promise<JSONResponseWrapper> promise = client(ClientUtil.MSG_WEB_SERVICE_URL
                + "/messageCenter/deleteConversation", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        renderJSON(resp.toString());
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
