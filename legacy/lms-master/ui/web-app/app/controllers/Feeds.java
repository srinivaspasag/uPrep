/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.Map;
import java.util.logging.Level;

import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
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
public class Feeds extends AbstractUIController {
    public static void getAllTickers(){
            JSONObject feeds = _tickers(null, "getAllTickers");
            renderJSON(feeds.toString());
    }
    public static void getTicker(){
            JSONObject feeds = _tickers(null, "getTicker");
            renderJSON(feeds.toString());
    }
    public static void flushTicker(){
            JSONObject flush = _tickers(null, "flushTicker");
            renderJSON(flush.toString());
    }
    public static JSONObject _checkLogout(JSONObject data){
            String userId = session.get("userId");
            String callerUserId = Scope.Params.current().get("callerUserId");
            if(data == null){
                data = new JSONObject();
            }
            if(userId==null || "".equals(userId) || !userId.equals(callerUserId)){
                try {
                    data.put("errorCode","LOGOUT");
                    data.put("result","{}");
                } catch (JSONException ex) {
                    Logger.log4j.info(Application.class.getName()+Level.SEVERE+ex);
                    try {
                        data = new JSONObject("{result:{},errorCode:'LOGOUT',errorMessage:'You have been logout'}");
                    } catch (JSONException ex1) {}
                }
            try {
                data.put("isLogout", true);
            } catch (JSONException ex) {}
                Logger.log4j.info("USER Logged out of user id = "+callerUserId);
            }
            return data;
    }
    protected static JSONObject _tickers(Map<String, Object> allParams,String url){
            JSONObject resp = _checkLogout(null);
            if(resp.optBoolean("isLogout", false) == true){
                return resp;
            }
            Promise<JSONResponseWrapper> promise = client(ClientUtil.NEWSFEED_WEB_SERVICE_URL
                    +"/Tickers/"+url,allParams);
            Logger.log4j.info("BEFORE AWAIT");
            await(promise);
            Logger.log4j.info("AFTER AWAIT");
            resp = getJSON(promise);
            resp = Validation.verifyResponse(resp);
            return resp;
    }
}
