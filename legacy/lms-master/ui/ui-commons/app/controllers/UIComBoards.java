/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.libs.F;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

/**
 *
 * @author ajith
 */
public class UIComBoards extends AbstractUIController{
    public static void getOrgBoards(){
        JSONObject resp=_getOrgBoards(null);
        renderJSON(resp.toString());
    }
    public static void getConsumerBoards(){
        JSONObject resp=_getBoards(null);
        renderJSON(resp.toString());
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
        boolean showSharedSubjects = _getShowSharedSubjects(ownerId);
        if(showSharedSubjects){
            allParams.put("showSharedSubjects", "show");
        }
        JSONObject resp=_getBoards(allParams);
        return resp;
    }
    private static boolean _getShowSharedSubjects(String ownerId) {
        Map<String, Object> allParams = new HashMap<String, Object>();
        allParams.put("orgId", ownerId);
        F.Promise<JSONResponseWrapper> promise =
                client(ClientUtil.ORGANIZATION_SERVICE_URL + "/organizations/getShowSharedSubjects",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        boolean showSharedSubjects = false;
        try {
            showSharedSubjects = resp.getJSONObject("result").getBoolean("showSharedSubjects");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return showSharedSubjects;
    }
    protected static JSONObject _getConsumerBoards(Map<String, Object> allParams){
        if(allParams==null){
            allParams=getReqParams();
        }        
        allParams.put("context","CONSUMER");
        allParams.put("ownerId", "SYSTEM");        
        JSONObject resp=_getBoards(allParams);
        return resp;
    }
    protected static JSONObject _getGlobalBoards(Map<String, Object> allParams){
        if(allParams==null){
            allParams=getReqParams();
        }        
        allParams.put("context","GLOBAL");
        allParams.put("ownerId", "SYSTEM");        
        JSONObject resp=_getBoards(allParams);
        return resp;
    }    
    
    //here depth can be given to any level,the upper apis only give the children or one level
    protected static JSONObject _getChildrenOfRootNodes(Map<String, Object> allParams){
        F.Promise<JSONResponseWrapper> promise =
                client(ClientUtil.BOARDS_SERVICE_URL + "/boards/getTreesOfBoards",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;        
    }
    protected static JSONObject _getChildrenOfRootNodesForGlobalBoards
            (Map<String, Object> allParams){
        if(allParams==null){
            allParams=getReqParams();
        }        
        allParams.put("context","GLOBAL");
        allParams.put("ownerId", "SYSTEM");        
        JSONObject resp=_getChildrenOfRootNodes(allParams);
        return resp;
    }            
    protected static JSONObject _getChildrenOfRootNodesForConsumerBoards
            (Map<String, Object> allParams){
        if(allParams==null){
            allParams=getReqParams();
        }        
        allParams.put("context","CONSUMER");
        allParams.put("ownerId", "SYSTEM");        
        JSONObject resp=_getChildrenOfRootNodes(allParams);
        return resp;
    } 
    protected static JSONObject _getChildrenOfRootNodesForOrgBoards
            (Map<String, Object> allParams){
        String ownerId=request.params.get("orgId");
        if(StringUtils.isEmpty(ownerId)){
            ownerId=session.get("orgId");
        }          
        if(allParams==null){
            allParams=getReqParams();
        }        
        allParams.put("context","ORG");
        allParams.put("ownerId", ownerId);        
        JSONObject resp=_getChildrenOfRootNodes(allParams);
        return resp;
    }     
}
