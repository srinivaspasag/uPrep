
package controllers;

import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Scope;
import play.mvc.With;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;
import uicom.util.Validation;

@With(Security.class)
public class Challenges extends AbstractUIController{


    public static void addChallenge(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL +"/challenges/addChallenge",null);
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
    
    
    
    public static void challenges(){
        request.params.put("resultType", "ALL");
        JSONObject channels=_getChannels(null);
        JSONObject challenges=_getChallengesPageResp(null);
        render(challenges,channels);
    }
    public static void activeChallenges(){
        Scope.Params.current().put("status","ACTIVE");
        Scope.Params.current().put("resultType","ALL");
        JSONObject challenges=_getChallenges(null);
        render(challenges);
    }
    public static void closedChallenges(){
        Scope.Params.current().put("status","ENDED");
        Scope.Params.current().put("resultType","ALL");
        JSONObject challenges=_getChallenges(null);
        render(challenges);
    }
    public static void attemptedChallenges(){
        Scope.Params.current().put("resultType","ATTEMPTED");
        Scope.Params.current().put("status","ACTIVE");
        Map<String,Object> allParams=getReqParams();
        JSONObject activeChallenges=_getChallenges(allParams);
        allParams.put("status","ENDED");
        JSONObject closedChallenges=_getChallenges(allParams);
        render(activeChallenges,closedChallenges);
    }
    public static void challengeItems(){
        JSONObject challenges=_getChallenges(null);
        render(challenges);
    }
    public static void challengeStats(){
        JSONObject stats=_getUserChallengeStats(null);
        renderJSON(stats.toString());
    }
    public static void userChallengeInfo(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL +"/challenges/getChallengeUserAttemptInfo",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject challengeInfo = ResponseUtil.checkResponse(getJSON(promise));
        render(challengeInfo);
    }



    
    //take challenge
    public static void takeChallenge(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL +"/challenges/getChallengeInfo",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject challengeInfo = ResponseUtil.checkResponse(getJSON(promise));
        render(challengeInfo);
    }
    public static void startChallenge(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL +"/challenges/getChallengeDetails",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject challengeStart = ResponseUtil.checkResponse(getJSON(promise));
        String entityId = request.params.get("id");
        Application.recordActivity(ClientUtil.ActivityPages.CHALLENGES,ClientUtil.ActivityAction.ATTEMPTED,ClientUtil.Entity.CHALLENGE,entityId);
        render("Challenges/challengeQues.html",challengeStart);
    }
    public static void submitAnswer(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL +"/challenges/attemptChallenge",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void challengeHint(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL +"/challenges/getHint",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }


    //home page widget
    public static JSONObject _getStats(){
        Scope.Params.current().put("targetUserId", session.get("userId"));
        Map<String, Object> allParams=getReqParams();
        JSONObject statsInfo=_getChallengeStats(allParams);
        JSONObject myPointsInfo=Profile._getMyTotalPoints(allParams);
        JSONObject stats=new JSONObject();       
        try {           
            stats.put("myPointsInfo", myPointsInfo);
            stats.put("statsInfo", statsInfo);
        } catch (JSONException ex) {            
            Logger.log4j.error(ex.getMessage(),ex);
        }                
        return stats;
    }
    public static void homePageStats(){
        JSONObject stats= _getStats();
        renderJSON(stats.toString());
    }
    public static void globalLeaderBoard(){
        JSONObject leaders =_getChallengeGlobalLeaderBoard(null);
        render(leaders);
    }
    public static void globalLeaderBoardItems()throws JSONException{       
        JSONArray leaders;
        leaders = _getChallengeGlobalLeaderBoard(null).getJSONObject("result").getJSONArray("list");
        String target="";
        if(params._contains("target")){
            target=params.get("target");
        }                
        render("tags/challenges/leaderBoardItems.html",leaders,target);
    }    
    public static void challengeLeaderBoard(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL +"/challenges/getChallengeLeaderBoard",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject leaders = ResponseUtil.checkResponse(getJSON(promise));
        render("Challenges/globalLeaderBoard.html",leaders);
    }



    //return functions
    protected static JSONObject _getChallengesPageResp(Map<String, Object> allParams){
        Scope.Params.current().put("status","ACTIVE");
        Scope.Params.current().put("start",ClientUtil.DEFAULT_FETCH_START);
        Scope.Params.current().put("size",ClientUtil.DEFAULT_FETCH_SIZE_10);
        Scope.Params.current().put("orderBy","timeCreated");
        JSONObject challenges=_getChallenges(allParams);
        Application.recordActivity(ClientUtil.ActivityPages.CHALLENGES,ClientUtil.ActivityAction.OPEN,null,null);
        return challenges;
    }
    protected static JSONObject _getChallenges(Map<String, Object> allParams){
        if(allParams!=null){
            allParams.put("type","FIXED_TIME");
        }else{
            request.params.put("type","FIXED_TIME");
        }
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL +"/challenges/getChallenges",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }
    protected static JSONObject _getUserChallengeStats(Map<String, Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL +"/challenges/getUserChallengeInfo",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    protected static JSONObject _getChallengeStats(Map<String, Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL +"/challenges/getChallengeStats",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }
    protected static JSONObject _getChallengeGlobalLeaderBoard(Map<String, Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL +"/challenges/getChallengeGlobalLeaderBoard",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        resp = ResponseUtil.checkResponse(resp);
        return resp;
    }

    //routes
    public static void challengesDirect(){
        request.params.put("resultType", "ALL");
        JSONObject channels=_getChannels(null);
        JSONObject challenges=_getChallengesPageResp(null);
        String includeName="Challenges/challenges.html";
        flash.put("ENTRY", "DIRECT");
        render("Application/myPages.html",includeName,challenges,channels);
    }
}
