/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package controllers;


import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.i18n.Messages;
/**
 *
 * @author ajithreddy
 */
public class ErrorStore extends AbstractUIController{
    private static enum ErrorCode{
        MISSING_PARAMETER, SERVICE_ERROR,COMMON_ERROR_MESSAGE;
    }
    public static JSONObject checkRequiredParams(){
        Logger.log4j.info("received request params : " + request.params.allSimple());
        JSONObject error= null;
        if (validation.hasErrors()) {
            Logger.log4j.error("missing params");            
            String s="{'errorCode': "+ErrorCode.MISSING_PARAMETER+",'errorMessage':"+Messages.get(ErrorCode.MISSING_PARAMETER)+",'result':''}";
            try {
                error = new JSONObject(s);
            } catch (JSONException e){
                Logger.error(e.getMessage());
            }          
        }
        return error;
    }
    public static JSONObject getJSONReqError(JSONObject response){
        JSONObject errorCheck = null;
        try {
            Logger.log4j.error("The errorCode is "+response.getString("errorCode"));
            String er = "{'errorCode': " + response.getString("errorCode") + "," + "'errorMessage':" +
                    Messages.get(ErrorCode.COMMON_ERROR_MESSAGE) + ",'result':''}";
            errorCheck = new JSONObject(er);
        } catch (JSONException ex) {
            Logger.error(ex.getMessage());
        }
        return errorCheck;
    }
    public static JSONObject errorResponse(String error){
        JSONObject errorCheck = null;
        try {            
            String er = "{'errorCode': "+error+"," + "'errorMessage':" + error + ",'result':''}";
            errorCheck = new JSONObject(er);
        } catch (JSONException ex) {
            Logger.error(ex.getMessage());
        }
        return errorCheck;
    }
    public static JSONObject wrapJSONObject(JSONObject resp){
        JSONObject jsonObj=new JSONObject();
        try {            
            jsonObj.put("errorCode", "");
            jsonObj.put("errorResponse", "");
            jsonObj.put("result", resp);
        } catch (JSONException ex) {
            Logger.error(ex.getMessage());
        }
        return jsonObj;
    }    
}
