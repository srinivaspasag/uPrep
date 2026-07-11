/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uicom.util;
import org.json.JSONObject;

import play.Logger;
import play.i18n.Messages;
import play.mvc.Http;
import uicom.response.ErrorInfo;
import uicom.response.JSONResponse;


/**
 *
 * @author ajith
 */
public class ResponseUtil{
    private static final String COMMON_ERROR_MESSAGE = "COMMON_ERROR_MESSAGE";
    private static final String NULL_RESPONSE = "NULL_RESPONSE";
     
    public static JSONObject checkResponse(JSONObject resp){
        String requestAction="";
        try{
            requestAction=Http.Request.current().actionMethod.toUpperCase();
        }catch(Exception e){
            Logger.log4j.error(e.getMessage());
        }
        String err;
        try{
            if((resp==null)||!resp.has("result")){
                err=Messages.get(requestAction+"_"+NULL_RESPONSE);
                if(err.equals(requestAction+"_"+NULL_RESPONSE)){
                    err=Messages.get(COMMON_ERROR_MESSAGE);
                }                
                resp=new JSONObject(new JSONResponse(resp,err,NULL_RESPONSE));
            }else if(resp.has("errorCode")&&!resp.getString("errorCode").isEmpty()){
                String errorCode=resp.getString("errorCode");
                err = _getErrorMessage(errorCode, requestAction, resp.getString("errorMessage"));
                /*String comboErrorCode=requestAction+"_"+errorCode;
                err=Messages.get(comboErrorCode);
                if(err.equals(comboErrorCode)){
                    err=Messages.get(errorCode);
                    if(err.equals(errorCode)){
                        String errorMessage = resp.getString("errorMessage");
                        if(errorMessage.isEmpty()){
                            errorMessage = errorCode;
                        }
                        err=errorMessage;
                    }
                }*/
                resp.put("errorMessage",err);
            }
        }catch(Exception e){
            Logger.log4j.error(e.getMessage());
        }   
        return resp;
    }
    public static String _getErrorMessage(String errorCode){
        String requestAction = "";
        try{
            requestAction=Http.Request.current().actionMethod.toUpperCase();
        }catch(Exception e){
            Logger.log4j.error(e.getMessage());
            requestAction = "";
        }
        return _getErrorMessage(errorCode,requestAction,"");
    }
    public static String _getErrorMessage(String errorCode,String requestAction,String errorMessage){
        String comboErrorCode=requestAction+"_"+errorCode;
        //Logger.log4j.info("ERROR=============="+comboErrorCode);
        String err=Messages.get(comboErrorCode);
        if(err.equals(comboErrorCode)){
            err=Messages.get(errorCode);
            if(err.equals(errorCode)){
                if(errorMessage.isEmpty()){
                    errorMessage = errorCode;
                }
                err=errorMessage;
            }
        }
        return err;
    }
    public static JSONObject getCommonErrorResponse(){
        return new JSONObject(new JSONResponse(
                new ErrorInfo(COMMON_ERROR_MESSAGE,Messages.get(COMMON_ERROR_MESSAGE))));
    }
}
