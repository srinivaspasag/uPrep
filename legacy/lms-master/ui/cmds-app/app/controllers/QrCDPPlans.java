package controllers;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import play.Logger;
import play.libs.F.Promise;
import play.mvc.With;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;


@With(Security.class)
public class QrCDPPlans extends AbstractQRUIController {
    
    public static void allCDPs(){
        JSONObject cdps=_getAllCDPs(null);
        render(cdps);
    }
    public static void allCDPsList(){
        JSONObject cdps=_getAllCDPs(null);
        render(cdps);
    }    
    public static void createCDP(){          
        Promise<AbstractUIController.JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsCDPlans/createCDP",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }   
    public static void publishCDP(){          
        Promise<AbstractUIController.JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsCDPlans/publishCDP",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }       
    public static void cdpPage(){
        JSONObject cdpInfo=_getCDPInfo(null);
        render(cdpInfo);
    }
    public static void folderContent(){
        request.params.put("libraryType","CDP_LIBRARY");
        JSONObject contents=_getFolderContent(null);
        render("Widgets/pkgCDPCommons/contentmcWidget.html",contents);
    }
    public static void folderContentTable(){
        request.params.put("libraryType","CDP_LIBRARY");
        JSONObject contents=_getFolderContent(null);
        render("Widgets/pkgCDPCommons/contentTable.html",contents);
    }    
    
    
    
    public static void subjectPlan(){
        Promise<AbstractUIController.JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsCDPlans/getAllSubCDPs",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject subCDPInfo = ResponseUtil.checkResponse(getJSON(promise));
        render(subCDPInfo);
    }
    public static void removeCDP(){          
        Promise<AbstractUIController.JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsCDPlans/remove",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    } 
    public static void removeCDPContent(){          
        Promise<AbstractUIController.JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsCDPlans/removeContents",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }     
    public static void addToCDP(){
        Map<String, Object> allParams=getReqParams();
        JSONArray cdps=getcdpList(allParams);
        render(cdps);
    }    
    public static void addToCDPList(){
        Map<String, Object> allParams=getReqParams();
        JSONArray cdps=getcdpList(allParams);
        render(cdps);
    }        
    public static void addToCDPSubmit(){
        Promise<AbstractUIController.JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsCDPlans/createCDPWithContent",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());        
    }
    public static void addToCDPLibrarySubmit(){
        Promise<AbstractUIController.JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsCDPlans/addToLibrary",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());        
    }    
    
    
    
    
    
    
    
    protected static JSONArray getcdpList(Map<String, Object> allParams){
        JSONArray cdps=new JSONArray();
        try{
            if(allParams.get("cdpId")!=null){
                cdps=_getCDPInfo(allParams).getJSONObject("result").getJSONArray("cdps");
            }else{
                cdps=_getAllCDPs(allParams).getJSONArray("result");
            }
        }catch(Exception e){
            Logger.log4j.error(e.getMessage());
        }
        return cdps;
    }
    protected static JSONObject _getCDPInfo(Map<String, Object> allParams){          
        Promise<AbstractUIController.JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsCDPlans/getCDP",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    } 
    protected static JSONObject _getAllCDPs(Map<String, Object> allParams){          
        Promise<AbstractUIController.JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsCDPlans/getAllCDPs",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }     
    protected static JSONObject _getFolderContent(Map<String, Object> allParams){          
        Promise<AbstractUIController.JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsCDPlans/getLibraryContent",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    } 
    
    
    
    public static void cdpDirect(String cdpId,String orgId){
        JSONObject cdpInfo=_getCDPInfo(null);
        String includeName="QrCDPPlans/cdpPage.html";
        flash.put("ENTRY", "DIRECT");        
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html",includeName,cdpInfo,currentOrgInfo); 
    }         
    public static void cdpsDirect(String orgId){
        JSONObject cdps=_getAllCDPs(null);
        String includeName="QrCDPPlans/allCDPs.html";
        flash.put("ENTRY", "DIRECT");        
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html",includeName,cdps,currentOrgInfo); 
    }             
}