
package controllers;

import java.util.Map;

import org.json.JSONObject;

import play.Logger;
import play.data.validation.Required;
import play.libs.F.Promise;
import play.mvc.Scope;
import play.mvc.With;
import uicom.util.ClientUtil;

@With(Security.class)
public class Boards extends AbstractUIController{
    
    public static void boardExam(){        
        JSONObject examInfo=_getBoardInfo(null);
        render(examInfo);
    }
    public static void boardSubject(){
        Scope.Params.current().put("addChildren","true");
        Scope.Params.current().put("childStart","0");
        Scope.Params.current().put("childSize","25");
        Scope.Params.current().put("childOfChildFacet","true");
        Scope.Params.current().put("targets","true");
        Map<String, Object> allParams= getReqParams();
        JSONObject subjectInfo=_getBoardInfo(allParams);
        render(subjectInfo);
    }
    public static void boardTopic(){
        Scope.Params.current().put("addChildren","true");
        Scope.Params.current().put("childStart","0");
        Scope.Params.current().put("childSize","25");
        Scope.Params.current().put("targets","true");
        JSONObject topicInfo=_getBoardInfo(null);
        render(topicInfo);
    }
    public static void boardSubTopic(){
        Scope.Params.current().put("addChildren","true");
        Scope.Params.current().put("childStart","0");
        Scope.Params.current().put("childSize","25");
        Scope.Params.current().put("targets","true");
        JSONObject topicInfo=_getBoardInfo(null);
        flash.put("pageType","BOARD_SUB_TOPIC");
        render("Boards/boardTopic.html",topicInfo);
    }
    public static void getBSContents(){
        Scope.Params.current().put("addChildren","true");
        Scope.Params.current().put("childStart","0");
        Scope.Params.current().put("childSize","25");
        Scope.Params.current().put("childOfChildFacet","true");
        Scope.Params.current().put("targets","true");
        Map<String, Object> allParams= getReqParams();
        JSONObject subjectInfo=_getBoardInfo(allParams);
        render("/Boards/BSContents.html",subjectInfo);
    }
    public static void getBoards(){
        Map<String, Object> allParams = getReqParams();
        allParams.put("context", "ORG");
        allParams.put("ownerId", Scope.Params.current().get("orgId"));
        JSONObject resp=Boards._getBoards(allParams);
        renderJSON(resp.toString());
    }
    public static void getBoardInfo(){
        Map<String, Object> allParams=getReqParams();
        JSONObject resp=Boards._getBoardInfo(allParams);
        renderJSON(resp.toString());
    }
    public static void getCommonBoards(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.BOARDS_SERVICE_URL +"/boards/getCommonBoards",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        renderJSON(resp.toString());
    }
    public static void getFollowingBoards(){
        JSONObject resp=_getFollowingBoards(null);
        renderJSON(resp.toString());
    }



    //return functions
    public static JSONObject _getBoards(Map<String, Object> allParams){
        if(allParams != null){
            allParams.put("recordState", "ACTIVE");
        }else{
            request.params.put("recordState", "ACTIVE");
        }
        Promise<JSONResponseWrapper> promise = client(ClientUtil.BOARDS_SERVICE_URL +"/boards/getChildren",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        return resp;
    }
    public static JSONObject _getBoardInfo(Map<String, Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.BOARDS_SERVICE_URL +"/boards/getBoardInfo",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        return resp;
    }
    public static JSONObject _getFollowingBoards(Map<String, Object> allParams){
        Scope.Params.current().put("followingOnly", "true");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.BOARDS_SERVICE_URL 
                +"/boards/getBoards",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        return resp;
    }

    //utilities
    public static String getBoardUrl(String boardType,String brdId){
        String url="";
        if(brdId.isEmpty()){
            url="/";
        }
        else{
            url="/"+boardType+"/"+brdId;
        }
        url = "/";
        return url;
    }
    public static void examDirect(@Required String brdId){
        JSONObject examInfo=_getBoardInfo(null);
        String includeName="Boards/boardExam.html";
        flash.put("ENTRY", "DIRECT");
        render("Application/myPages.html",includeName,examInfo);
    }
    public static void subjectDirect(@Required String docId){
        Scope.Params.current().put("addChildren","true");
        Scope.Params.current().put("childStart","0");
        Scope.Params.current().put("childSize","25");
        Scope.Params.current().put("childOfChildFacet","true");
        Scope.Params.current().put("targets","true");
        JSONObject subjectInfo=_getBoardInfo(null);
        String includeName="Boards/boardSubject.html";
        flash.put("ENTRY", "DIRECT");
        render("Application/myPages.html",includeName,subjectInfo);
    }
    public static void topicDirect(@Required String brdId){
        Scope.Params.current().put("addChildren","true");
        Scope.Params.current().put("childStart","0");
        Scope.Params.current().put("childSize","25");
        Scope.Params.current().put("targets","true");
        JSONObject topicInfo=_getBoardInfo(null);
        String includeName="Boards/boardTopic.html";
        flash.put("ENTRY", "DIRECT");
        render("Application/myPages.html",includeName,topicInfo);
    }
    public static void subTopicDirect(@Required String brdId){
        Scope.Params.current().put("addChildren","true");
        Scope.Params.current().put("childStart","0");
        Scope.Params.current().put("childSize","25");
        Scope.Params.current().put("targets","true");
        JSONObject topicInfo=_getBoardInfo(null);
        flash.put("pageType","BOARD_SUB_TOPIC");
        String includeName="Boards/boardTopic.html";
        flash.put("ENTRY", "DIRECT");
        render("Application/myPages.html",includeName,topicInfo);
    }
}
