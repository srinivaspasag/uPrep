/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.Map;

import org.json.JSONObject;

import play.Logger;
import play.libs.F;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

/**
 *
 * @author ajith
 */
public class QrChannels extends AbstractQRUIController{
    public static void channels(){
        JSONObject channels=_getChannels(null);
        render(channels);
    } 
    public static void channelsvChoose(){
        JSONObject channels=_getChannels(null);
        render(channels);
    }    
    
    public static void createChannel(){
        F.Promise<JSONResponseWrapper> promise =
                client(ClientUtil.CONTENT_SERVICE_URL + "/channels/addChannel",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }    
    protected static JSONObject _getChannels(Map<String,Object> allParams){
        F.Promise<JSONResponseWrapper> promise =
                client(ClientUtil.CONTENT_SERVICE_URL + "/channels/getChannels",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;        
    }
    
    public static void channelsDirect(String orgId){
        JSONObject channels=_getChannels(null);
        String includeName="QrChannels/channels.html";
        flash.put("ENTRY", "DIRECT");      
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html",includeName,channels,currentOrgInfo);         
    }
}
