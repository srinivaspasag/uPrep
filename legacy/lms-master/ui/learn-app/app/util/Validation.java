/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package util;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;

/**
 *
 * @author anirbandutta
 */
public class Validation{
     protected static JSONObject getErrorJSON(String errorCode,String errorMsg){
        JSONObject errorJSON=null;
        String errorJSONStr = "{result:{},errorCode:'',errorMessage:''}";
        try {
            errorJSON = new JSONObject(errorJSONStr);
            errorJSON.put("errorCode", errorCode);
            errorJSON.put("errorMessage", errorMsg);
        } catch (JSONException ex) {
            Logger.log4j.info(ex.getMessage());
            try {
                errorJSON = new JSONObject("{result:{noData:''},errorCode:404,errorMessage:NO DATA RECIEVED}");
            } catch (JSONException ex1) {
                Logger.log4j.info(ex1);
            }
        }
        return errorJSON;
    }
    public static JSONObject verifyResponse(JSONObject response){
        if(response == null || !response.has("result")){
                response = getErrorJSON("NO_DATA_RECIEVED","NO DATA RECIEVED");
        }else if(response.has("errorCode")){
            response = ResponseUtil.checkResponse(response);
            try {
                String errorCode = response.getString("errorCode");
                if(errorCode!=null && errorCode.length()>0){
                    String errorMsg = "Un-Known Error";
                    if(response.has("errorMessage")){
                        errorMsg = response.getString("errorMessage");
                    }
                    response = getErrorJSON(errorCode,errorMsg);
                }
            } catch (JSONException ex) {
                response = getErrorJSON("NO_DATA_RECIEVED","NO DATA RECIEVED");
            }
        }
        return response;
    }
    public static boolean isEmpty(String str){
        if(str == null || str.equals(JSONObject.NULL) || StringUtils.isEmpty(str)){
            return true;
        }
        return false;
    }
    public static boolean isEmpty(Object str){
        if(str == null || str.equals(JSONObject.NULL)){
            return true;
        }
        return false;
    }
}
