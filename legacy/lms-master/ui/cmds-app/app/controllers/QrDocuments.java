package controllers;

import java.util.Map;

import org.json.JSONObject;

import play.Logger;
import play.libs.F;
import play.mvc.With;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;


@With(Security.class)
public class QrDocuments extends AbstractQRUIController {
    public static void videoPage(String id) {
        recordActivity(ClientUtil.ActivityPages.VIDEO,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.VIDEO,id);
        Map<String, Object> allParams = getReqParams();
        JSONObject docInfo = _getVideoInfo(null);
        allParams.put("entity.id",allParams.get("id") );
        allParams.put("entity.type","CMDSVIDEO" );
        JSONObject entityRatingCMDSInfo = _getCMDSEntityRatingInfo(allParams);
        render(docInfo,entityRatingCMDSInfo);
    }
    private static JSONObject _getVideoInfo(Map<String, Object> allParams) {
        F.Promise<JSONResponseWrapper> promise = clientWithAllHeaders(ClientUtil.CMDS_SERVICE_URL +"/cmdsVideos/getVideo", allParams, null);
        Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - VIDEOS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - VIDEOS");
        JSONObject data = getJSON(promise);
        data = ResponseUtil.checkResponse(data);
        return data;
    }
    private static JSONObject _getCMDSEntityRatingInfo(Map<String, Object> allParams) {
        F.Promise<JSONResponseWrapper> promise = clientWithAllHeaders(ClientUtil.CONTENT_SERVICE_URL +"/contents/getCMDSEntityInfo", allParams, null);
        Logger.log4j.info("BEFORE AWAIT : GET ENTITY INFO - VIDEOS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET ENTITY INFO - VIDEOS");
        JSONObject data = getJSON(promise);
        data = ResponseUtil.checkResponse(data);
        return data;
    }
    public static void videoPageDirect(String id,String orgId){
        recordActivity(ClientUtil.ActivityPages.VIDEO,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.VIDEO,id);
        String includeName="QrDocuments/videoPage.html";
        JSONObject docInfo = _getVideoInfo(null);  
        Map<String, Object> allParams = getReqParams();
        allParams.put("entity.id",allParams.get("id") );
        allParams.put("entity.type","CMDSVIDEO" );
        JSONObject entityRatingCMDSInfo = _getCMDSEntityRatingInfo(allParams);
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html",includeName,docInfo,currentOrgInfo,entityRatingCMDSInfo);               
    }
    public static void docPage(String id) {
        recordActivity(ClientUtil.ActivityPages.DOCUMENT,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.DOCUMENT,id);
        Map<String, Object> allParams = getReqParams();
        JSONObject docInfo = _getDocInfo(null);
        allParams.put("entity.id",allParams.get("id") );
        allParams.put("entity.type","CMDSDOCUMENT" );
        JSONObject entityRatingCMDSInfo = _getCMDSEntityRatingInfo(allParams);
        render("UIComDocuments/docPage.html",docInfo,entityRatingCMDSInfo);
    }
    private static JSONObject _getDocInfo(Map<String, Object> allParams) {
        F.Promise<JSONResponseWrapper> promise = clientWithAllHeaders(ClientUtil.CMDS_SERVICE_URL +"/cmdsDocuments/get", allParams,null);
        Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - Documents");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - Documents");
        JSONObject data = getJSON(promise);
        data = ResponseUtil.checkResponse(data);
        return data;
    }
    public static void docPageDirect(String id,String orgId){
        recordActivity(ClientUtil.ActivityPages.DOCUMENT,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.DOCUMENT,id);
        String includeName="UIComDocuments/docPage.html";
        Map<String, Object> allParams = getReqParams();
        JSONObject docInfo = _getDocInfo(null);
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        allParams.put("entity.id",allParams.get("id") );
        allParams.put("entity.type","CMDSDOCUMENT" );
        JSONObject entityRatingCMDSInfo = _getCMDSEntityRatingInfo(allParams);
        render("Application/mapper.html",includeName,docInfo,currentOrgInfo,entityRatingCMDSInfo);               
    }
    public static void filePage(String id) {
        recordActivity(ClientUtil.ActivityPages.FILE,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.FILE,id);
        JSONObject fileInfo = _getFileInfo(null);
        render("UIComDocuments/filePage.html",fileInfo);
    }
    private static JSONObject _getFileInfo(Map<String, Object> allParams) {
        F.Promise<JSONResponseWrapper> promise = clientWithAllHeaders(ClientUtil.CMDS_SERVICE_URL +"/cmdsFiles/get", allParams, null);
        Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - File Info");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - Files Info");
        JSONObject data = getJSON(promise);
        data = ResponseUtil.checkResponse(data);
        return data;
    }
    public static void filePageDirect(String id,String orgId){      
        recordActivity(ClientUtil.ActivityPages.FILE,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.FILE,id);
        String includeName="UIComDocuments/filePage.html";
        JSONObject fileInfo = _getFileInfo(null);
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html",includeName,fileInfo,currentOrgInfo);               
    }
    public static void editDocTocs() {
        Map<String,Object> allParams=getReqParams();
        JSONObject docInfo =UIComDocuments._getDoc(allParams);
        JSONObject docTocs = UIComDocuments._getTocs(allParams);
        render(docInfo,docTocs);     
    }
    public static void publishDocVideos(){
        F.Promise<JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL + "/CmdsDocuments/publishDoc", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }    
    public static void docContents() {
        recordActivity(ClientUtil.ActivityPages.DOCUMENT,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.DOCUMENT,request.params.get("docId"));
        JSONObject docTocs = UIComDocuments._getTocs(null);
        String docId = request.params.get("docId");
        render(docTocs, docId);
    }        
    
    
    public static String getDocUrl(String title,String docId){
        String url="";
        if(title.isEmpty()||docId.isEmpty()){
            url="/";
        }
        else{
            url="/document/"+docId;
        }
        return url;
    }
    public static String getVideoUrl(String title,String docId){
        String url="";
        if(title.isEmpty()||docId.isEmpty()){
            url="/";
        }
        else{
            url="/video/"+docId;
        }
        return url;
    }
    //for direct access
    public static void docVideoDirect(String docId,String orgId){
        recordActivity(ClientUtil.ActivityPages.DOCUMENT,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.DOCUMENT,docId);
        JSONObject docInfo = UIComDocuments._getDoc(null);
        String includeName="QrDocuments/docVideoPage.html";
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html",includeName,docInfo,currentOrgInfo);               
    }      
}