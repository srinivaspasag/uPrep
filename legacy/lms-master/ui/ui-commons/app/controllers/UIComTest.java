/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package controllers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.cache.Cache;
import play.data.validation.Required;
import play.i18n.Messages;
import play.libs.F.Promise;
import play.mvc.Scope;
import pojos.TestCacheData;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;
import uicom.util.TestsCacheHandler;
import uicom.util.Validation;
/**
 *
 * @author anirban
 */
public class UIComTest extends AbstractUIController {
    private static String _getUserAttemptKey(){
        String userId = session.get("userId");
        String key = "testAttempt/"+userId;
        return key;
    }
    protected static void _setTestSessions(String testAttemptId,String scheduleId,String testIdValidated){
        String key = _getUserAttemptKey();
        JSONObject data = new JSONObject();
        try{
            if(testAttemptId!=null){
                data.put("testAttemptId",testAttemptId);
            }
            if(scheduleId!=null){
                data.put("scheduleId",scheduleId);
            }
            if(testIdValidated!=null){
                data.put("testIdValidated",testIdValidated);
            }
            Logger.log4j.info("user attempt test data ========================= "+data);
            Cache.safeAdd(key, data.toString(), null);
        }catch(JSONException ex){
            Logger.log4j.error(ex.getMessage());
        }
    }
    protected static void _setTestSessions(){
        String key = _getUserAttemptKey();
        Cache.delete(key);
    }
    public static String _getAttemptId(){
        String key = _getUserAttemptKey();
        String dataStr = Cache.get(key,String.class);
        String testAttemptId = "";
        try{
            JSONObject data = new JSONObject(dataStr);
            testAttemptId = data.getString("testAttemptId");
        }catch(JSONException ex){
            Logger.log4j.error(ex.getMessage());
        }
        return testAttemptId;
    }
    protected static Map<String,Object> _putTestSessions(Map<String,Object> allParams){
        String key = _getUserAttemptKey();
        String dataStr = Cache.get(key,String.class);
        try{
            if(dataStr==null || dataStr.isEmpty()){
                if(session.contains("testAttemptId"))
                    allParams.put("attemptId",session.get("testAttemptId"));
                return allParams;
            }
            JSONObject data = new JSONObject(dataStr);
            if(data.has("testAttemptId")){
                String testAttemptId = data.getString("testAttemptId");
                if(testAttemptId!=null){
                    allParams.put("attemptId",testAttemptId);
                }
            }
            if(data.has("scheduleId")){
                String scheduleId = data.getString("scheduleId");
                allParams.put("scheduleId",scheduleId);
            }
        }catch(JSONException ex){
            Logger.log4j.error(ex.getMessage());
        }
        return allParams;
    }
    protected static boolean _isReAttemptTest(@Required String id){
        Map<String,Object> allParams=getReqParams();
        allParams.put("entity.id", id);
        allParams.put("entity.type", "TEST");
        Promise<JSONResponseWrapper> promise =
                client(ClientUtil.TEST_SERVICE_URL + "/analytics/getUserEntityAttemptStatusInfo", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT END TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT END TEST");
        JSONObject resp = getJSON(promise);
        boolean isAttempted = false;
        try {
            if(resp != null && resp.has("result") && resp.getString("errorCode").isEmpty()){
                JSONObject result = resp.getJSONObject("result");
//                String type = result.getString("type");
                isAttempted = result.getBoolean("attempted");
//                if("OFFLINE".equals(type)){
//                    isAttempted = true;
//                }
            }
         } catch (JSONException ex) {
                Logger.log4j.error(ex.getMessage());
                isAttempted = false;
            }
        return isAttempted;
    }
    protected static JSONObject _getTestResponseJSON(@Required String errorCode, String errorMsg,JSONObject result){
        errorMsg = errorMsg.isEmpty()?Messages.get(errorCode):errorMsg;
         String errStr = "{'errorCode':'"+errorCode+"','errorMessage':'"+errorMsg+"','result':{noData:''}}";
         JSONObject err=null;
            try {
                err = new JSONObject(errStr);
                if(result!=null){
                    err.put("result", result);
                }
            } catch (JSONException ex) {
                Logger.log4j.error(ex.getMessage());
            }
         return err;
    }
    private static JSONObject _getTestCache(@Required String userId){
        TestCacheData cacheData = TestsCacheHandler._getCurrentCache(userId);
        JSONObject result = null;
        try {
            result = new JSONObject(cacheData);
            long timeLeft = TestsCacheHandler._getTimeLeft(cacheData);
            result.put("timeLeft", timeLeft);
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
        }
        return result;
    }
    protected static JSONObject _verifyTestCache(String testId){
        Logger.log4j.info("VERIFYING Test Cache data >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> for test = "+testId);
        String userId = session.get("userId");
        String strResponse = TestsCacheHandler._verifyBeforeTest(userId, testId, session.getId());
        if(strResponse!=null && !strResponse.isEmpty()){
            JSONObject result = _getTestCache(userId);
            return _getTestResponseJSON(strResponse,"",result);
        }else{
            return null;
        }
    }
    protected static JSONObject _setTestCache(JSONObject testData,
            String testId,String testName){
        Logger.log4j.info("Setting Test Cache data >>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> for test = "+testId);
        String startTimeStr = request.params.get("startTime");
        long startTime = Long.parseLong(startTimeStr);
        long duration = 0;
        if(testData!=null && testData.has("duration")){
            duration = testData.optLong("duration");
            if(duration>0){
                duration += ClientUtil.TEST_CACHE_GRACE_TIME_MILLISEC;
            }
        }
        String testAttemptId = _getAttemptId();
        long extraTime = 0;
        if(session.contains("extraTime")){
            String extraTimeStr = session.get("extraTime");
            extraTime = Long.parseLong(extraTimeStr);
        }
        String userId = session.get("userId");
        String strResponse = TestsCacheHandler._setTestCache(userId, startTime,
                duration, testAttemptId, testId, testName, extraTime, session.getId());
        if(strResponse!=null && !strResponse.isEmpty()){
            JSONObject result = _getTestCache(userId);
            return _getTestResponseJSON(strResponse,"",result);
        }else{
            return null;
        }
    }
    public static void submitTestAnswer(){
        Map<String,Object> allParams=getReqParams();
        String testId = Scope.Params.current().get("testId");
        recordActivity(ClientUtil.ActivityPages.TEST_QUESTION_ATTEMPT, ClientUtil.ActivityAction.ATTEMPTED);
//        String testAttemptId = "";
        try{
            _getAttemptId();
        }catch(NullPointerException e){
//            if(session.contains("testAttemptId"))
//                testAttemptId = session.get("testAttemptId");
        }
//        String canResp = TestsCacheHandler._verifyTestTime(session.get("userId"), testId, testAttemptId, session.getId());
//        Logger.log4j.info("TEST TIME VERIFICATION DONE RETURN ============= "+canResp);
//        canResp = "";
//        if(!canResp.isEmpty()){
//            JSONObject err = _getTestResponseJSON(canResp,"",null);
//            renderJSON(err.toString());
//            return;
//        }
        allParams = _putTestSessions(allParams);
        allParams.put("entityId",testId);
        allParams.put("entityType","TEST");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.TEST_SERVICE_URL + "/analytics/recordAttempt", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT SUMIT ANSWER");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : ATTEMPT SUMIT ANSWER");
        if(promise!=null){
            JSONObject responseData = ResponseUtil.checkResponse(getJSON(promise));
            renderJSON(responseData.toString());
        }
    }
    public static void resetTestAnswer(){
        Map<String,Object> allParams=getReqParams();
//        String testId = Scope.Params.current().get("testId");
        recordActivity(ClientUtil.ActivityPages.TEST_QUESTION_ATTEMPT, ClientUtil.ActivityAction.RESET);
        String testAttemptId = "";
        try{
            testAttemptId = _getAttemptId();
        }catch(NullPointerException e){
            if(session.contains("testAttemptId"))
                testAttemptId = session.get("testAttemptId");
        }
//        String canResp = TestsCacheHandler._verifyTestTime(session.get("userId"), testId, testAttemptId, session.getId());
//        Logger.log4j.info("TEST TIME VERIFICATION DONE RETURN ============= "+canResp);
//        if(!canResp.isEmpty()){
//            JSONObject err = _getTestResponseJSON(canResp,"",null);
//            renderJSON(err.toString());
//            return;
//        }
        allParams = _putTestSessions(allParams);
        allParams.put("attemptId",testAttemptId);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.TEST_SERVICE_URL + "/analytics/resetQuestionAttempt", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT SUMIT ANSWER");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : ATTEMPT SUMIT ANSWER");
        if(promise!=null){
            JSONObject responseData = ResponseUtil.checkResponse(getJSON(promise));
            renderJSON(responseData.toString());
        }
    }
    public static void getQuestionsJson(){
        Map<String,Object> allParams=getReqParams();
    	JSONObject data = _getQuestionsData(allParams);
        renderJSON(data.toString());
    }
    protected static JSONObject _getQuestionsData(Map<String,Object> allParams){
        allParams = _putTestSessions(allParams);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.TEST_SERVICE_URL + "/tests/getTestQuestions", allParams);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET Test Question");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET Test Question");
        JSONObject quesData = ResponseUtil.checkResponse(getJSON(promise));
            try {
                quesData = quesData.getJSONObject("result");
            } catch (JSONException ex) {
                quesData = null;
                Logger.log4j.error(ex.getMessage());
            }
            return quesData;
    }
    protected static JSONObject _createTestAnalytics(Map<String,Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.TEST_SERVICE_URL + "/analytics/startAttempt", allParams);
        Logger.log4j.info("BEFORE AWAIT : CREATE USER TEST ANALYTICS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT : CREATE USER TEST ANALYTICS");
        JSONObject userAnalyticsData = ResponseUtil.checkResponse(getJSON(promise));
        String testAttemptId = null;
        try {
            testAttemptId = userAnalyticsData.getJSONObject("result").getJSONObject("info").getString("id");
            _setTestSessions(testAttemptId,null,null);
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getMessage());
        }
        return userAnalyticsData;
    }
    protected static JSONObject _getTestDetails(Map<String,Object> allParams){
//        Logger.log4j.info("Key :: "+allParams.get("entity.id") +"_getTestDetails");
//        String resp = Cache.get(allParams.get("entity.id") +"_getTestDetails", String.class);
//        if(!StringUtils.isEmpty(resp)){
//            Logger.log4j.info("Inside cache getTestDetails");
//            JSONObject data = getCacheDataInJsonObject(resp);
//            return data;
//        }else{
            Promise<JSONResponseWrapper> promise = client(ClientUtil.TEST_SERVICE_URL + "/tests/getTestInfo", allParams);
            Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET TOPICS");
            await(promise);
            Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET TOPICS");
            JSONObject data = ResponseUtil.checkResponse(getJSON(promise));
            data = Validation.verifyResponse(data);
            String error = null;
            String errorMsg = null;
            try {
                error = data.getString("errorCode");
                errorMsg = data.getString("errorMessage");
                if(error.length()>0){
                    //data=null;
                }
            } catch (JSONException ex) {
                Logger.log4j.error(ex.getMessage());
                return null;
            }
            try {
                data = data.getJSONObject("result");
                data.put("errorCode", error);
                data.put("errorMessage", errorMsg);
            } catch (JSONException ex) {
                Logger.log4j.error(ex.getMessage());
                //data = null;
            }
//            if(data != null)
//                Cache.set(allParams.get("entity.id") +"_getTestDetails", data.toString(), "1h");
            return data;
//        }
    }
    public static void terminateTest(@Required String testId){
        Map<String,Object> allParams=getReqParams();
        allParams.put("entityId",testId);
        allParams.put("entityType","TEST");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.TEST_SERVICE_URL + "/analytics/endAttempt", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT END TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT END TEST");
        JSONObject postTestResp = ResponseUtil.checkResponse(getJSON(promise));
        uicom.util.TestsCacheHandler._clearCache(session.get("userId").toString());
        _setTestSessions();
        renderJSON(postTestResp.toString());
    }
    public static void ping(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.TEST_SERVICE_URL + "/application/ping",null);
        Logger.log4j.info("BEFORE AWAIT : PING");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  PING");
        JSONObject pingResp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(pingResp.toString());
    }
}
