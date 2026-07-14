/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;
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
public class Assignments extends AbstractUIController {
    public static void studentPage(@Required String id){
        Map<String, Object> allParams = getReqParams();
        String asnId = id;
        JSONObject data = _getDetails(allParams);
        allParams.put("entity.id", id);
        allParams.put("entity.type", "ASSIGNMENT");
        allParams.put("orgId",allParams.get("parent.id"));
        JSONObject ratings = MyContents._getEntityRatingsAndFeedback(allParams);
        try{
                Application._markEntityView(id,ClientUtil.Entity.ASSIGNMENT);
        }catch(Exception err){}
        JSONObject myOrgInfo = Institute._setOrgParams(null);
        String includeInstFile = "Assignments/home.html";
        render("Institute/header.html",includeInstFile,asnId,data,myOrgInfo,ratings);
    }
    protected static JSONObject _getAssignments(Map<String,Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL +"/assignments/getAssignments", allParams);
        String isExploreContent = Scope.Params.current().get("exploreContentPage");
        Logger.log4j.info("BEFORE AWAIT : GET MY CONTENTS - VIDEOS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : GET MY CONTENTS - VIDEOS");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        return data;
    }
    public static void studentPageDirect(@Required String orgId,@Required String id){
        JSONObject myOrgInfo = Institute._setOrgParams(orgId);
        Map<String, Object> allParams = getReqParams();
        String asnId = id;
        JSONObject data = _getDetails(allParams);
        allParams.put("entity.id", id);
        allParams.put("entity.type", "ASSIGNMENT");
        allParams.put("orgId",orgId);
        JSONObject ratings = MyContents._getEntityRatingsAndFeedback(allParams);
        try{
                Application._markEntityView(id,ClientUtil.Entity.ASSIGNMENT);
        }catch(Exception err){}
        flash.put("ENTRY", "DIRECT");
        String includeName="Institute/header.html";
        String includeInstFile = "Assignments/home.html";
        render("Application/myPages.html",asnId, includeName, includeInstFile, data, myOrgInfo,ratings);
    }
    public static void studentsPerformance(){
        render();
    }
    private static JSONObject _getTeacherQuestions(Map<String,Object> allParams){
        allParams = _genAsnParams(allParams);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/analytics/getEntityQuestionAttempts", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT END TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT END TEST");
        JSONObject userAnalyticsData = getJSON(promise);
        userAnalyticsData = Validation.verifyResponse(userAnalyticsData);
        return userAnalyticsData;
    }
    public static void teacherAnalytics(){
        Map<String, Object> allParams = getReqParams();
        JSONObject data = _getDetails(allParams);
        render(data);
    }
    public static void teacherQuestions(){
        Map<String,Object> allParams=getReqParams();
        JSONObject questionsData = _getTeacherQuestions(allParams);
        render(questionsData);
    }
    public static void studentAnalytics(){
        Map<String, Object> allParams = getReqParams();
        JSONObject data = _getDetails(allParams);
        render(data);
    }
    private static Map<String,Object> _genAsnParams(Map<String,Object> allParams){
        allParams.put("entity.type", "ASSIGNMENT");
        allParams.put("entity.id", Scope.Params.current().get("id"));
        return allParams;
    }
    private static JSONObject _getQuestionsAnalytics(Map<String,Object> allParams){
        allParams = _genAsnParams(allParams);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/analytics/getUserEntityQuestionAttempts", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT END getUserEntityQuestionAttempts");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT END getUserEntityQuestionAttempts");
        JSONObject userAnalyticsData = getJSON(promise);
        userAnalyticsData = Validation.verifyResponse(userAnalyticsData);
        return userAnalyticsData;
    }
    public static void studentQuestions(){
        Map<String,Object> allParams=getReqParams();
        JSONObject questions = _getQuestionsAnalytics(allParams);
        render(questions);
    }
    public static void studentAttemptAnalytics(){
        Map<String,Object> allParams = _genAsnParams(getReqParams());
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/analytics/getEntityMeasures", allParams);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET TOPICS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET TOPICS");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        render("/tags/assignments/studentsAnalytics.html",data);
    }
    private static JSONObject _getDetails(Map<String,Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/assignments/getAssignmentInfo", allParams);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET TOPICS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET TOPICS");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        return data;
    }
    public static void teacherPage(@Required String id){
        String asnId = id;
        Map<String,Object> allParams=getReqParams();
        JSONObject data = _getDetails(allParams);
        try{
                Application._markEntityView(id,ClientUtil.Entity.ASSIGNMENT);
        }catch(Exception err){}
        JSONObject myOrgInfo = Institute._setOrgParams(null);
        String includeInstFile = "Assignments/home.html";
        render("Institute/header.html",includeInstFile,asnId,data,myOrgInfo);
    }
    public static void teacherPageDirect(@Required String orgId,@Required String id){
        JSONObject myOrgInfo = Institute._setOrgParams(orgId);
        Map<String, Object> allParams = getReqParams();
        JSONObject data = _getDetails(allParams);
        try{
                Application._markEntityView(id,ClientUtil.Entity.ASSIGNMENT);
        }catch(Exception err){}
        String asnId = id;
        flash.put("ENTRY", "DIRECT");
        String includeName = "Institute/header.html";
        String includeInstFile = "Assignments/home.html";
        render("Application/myPages.html",asnId, includeName, includeInstFile, data,myOrgInfo);
    }
    public static void direct(@Required String orgId,@Required String id){
        JSONObject myOrgInfo = Institute._setOrgParams(orgId);
        String userRole = "STUDENT";
        try {
            userRole = myOrgInfo.getString("userRole");
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getLocalizedMessage());
            userRole = "STUDENT";
        }
        if(("STUDENT").equals(userRole)){
            studentPageDirect(orgId, id);
        }else{
            teacherPageDirect(orgId, id);
        }
    }
    public static void submitAnswer(){
        Map<String,Object> allParams=getReqParams();
        String assignmentId = Scope.Params.current().get("assignmentId");
        allParams.put("entityId",assignmentId);
        allParams.put("entityType","ASSIGNMENT");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/analytics/recordAttempt", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT SUMIT ANSWER");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : ATTEMPT SUMIT ANSWER");
        if(promise!=null){
            JSONObject responseData = Validation.verifyResponse(getJSON(promise));
            renderJSON(responseData.toString());
        }
    }
    public static void studentMiniAnalytics(){
        Map<String,Object> allParams=getReqParams();
        allParams = _genAsnParams(allParams);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/analytics/getUserEntityMeasures", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT SUMIT ANSWER");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : ATTEMPT SUMIT ANSWER");
        if(promise!=null){
            JSONObject resp = Validation.verifyResponse(getJSON(promise));
            render(resp);
        }
    }
}
