package controllers;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

import org.apache.commons.lang.StringUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import controllers.AbstractUIController.JSONResponseWrapper;
import play.Logger;
import play.cache.Cache;
import play.data.validation.Required;
import play.libs.F.Promise;
import play.mvc.Scope;
import play.mvc.Scope.Params;
import play.mvc.With;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;
import uicom.util.Utilities;
import uicom.util.Validation;


//@With(Security.class)
public class Tests extends UIComTest {

    //--------TAKE TEST------
    protected static JSONObject _getUserTestAnalytics(Map<String,Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.TEST_SERVICE_URL + "/analytics/getUserEntityAnalytics", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT END TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT END TEST");
        JSONObject userAnalyticsData = getJSON(promise);
        userAnalyticsData = Validation.verifyResponse(userAnalyticsData);
        return userAnalyticsData;
    }
    public static void isReAttemptTest(){
        String testId = Scope.Params.current().get("testId");
        boolean isReattempt = _isReAttemptTest(testId);
        renderJSON(isReattempt);
    }
    private static JSONObject _getToppers(Map<String,Object> allParams){
        allParams.put("start",ClientUtil.DEFAULT_FETCH_START);
        allParams.put("size",ClientUtil.DEFAULT_FETCH_SIZE_10);
        JSONObject toppersData = _getToppersData(allParams);
        return toppersData;
    }
    private static JSONObject _getTestMarksDistribution(Map<String,Object> allParams){
        allParams.put("bucketCount", 5);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.TEST_SERVICE_URL + "/analytics/getEntityMarkDistribution", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT END TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT END TEST");
        JSONObject marksDistributionData = getJSON(promise);
        marksDistributionData = Validation.verifyResponse(marksDistributionData);
        return marksDistributionData;
    }
    private static JSONObject _userSubjectiveQuestionAttempts(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/tests/getSubjectiveQuestionUserAttempts", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);
        return resp;
    }
    private static JSONObject _getTestAnalyticsQuestions(Map<String,Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/analytics/getUserEntityQuestionAttempts", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT END TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT END TEST");
        JSONObject userAnalyticsData = getJSON(promise);
        userAnalyticsData = Validation.verifyResponse(userAnalyticsData);
        return userAnalyticsData;
    }
    private static JSONObject _getTeacherTestAnalyticsQuestions(Map<String,Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/analytics/getEntityQuestionAttempts", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT END TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT END TEST");
        JSONObject userAnalyticsData = getJSON(promise);
        userAnalyticsData = Validation.verifyResponse(userAnalyticsData);
        return userAnalyticsData;
    }

    private static JSONObject _getTestSubjectiveQuestions(Map<String,Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/tests/getTestSubjectiveQuestions", allParams);
        Logger.log4j.info("BEFORE AWAIT : GET TEST SUBJECTIVE QUESTIONS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  GET TEST SUBJECTIVE QUESTIONS");
        JSONObject userAnalyticsData = getJSON(promise);
        userAnalyticsData = Validation.verifyResponse(userAnalyticsData);
        return userAnalyticsData;
    }

    protected static JSONObject _getToppersData(Map<String,Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.TEST_SERVICE_URL + "/analytics/getEntityLeaderBoard", allParams);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET LEADER BOARD");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET LEADER BOARD");
        JSONObject toppersData = getJSON(promise);
        toppersData = Validation.verifyResponse(toppersData);
        return toppersData;
    }

    public static void uploadMarkSheets() {
        JSONObject resp = uploadUtil(ClientUtil.CMDS_SERVICE_URL + "/cmdsTests/uploadTestResults2",
                null, null);
        recordActivity(ClientUtil.ActivityPages.MARKS_SHEET, ClientUtil.ActivityAction.UPLOAD);
        renderJSON(resp.toString());
    }

    public static void uploadMarkSheetsStatus(){
        JSONObject resp = _getStatus();
        renderJSON(resp.toString());
    }

	public static void updateQuestionMarkStatus() {
		Promise<JSONResponseWrapper> promise = client(
				ClientUtil.CONTENT_SERVICE_URL + "/tests/updateMarksStatus",
				null);
		Logger.log4j.info("BEFORE AWAIT");
		await(promise);
		Logger.log4j.info("AFTER AWAIT");
		JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
		renderJSON(resp.toString());
	}
    private static JSONObject _getStatus() {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsResources/getStatus", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        try {
            JSONObject result = resp.getJSONObject("result");
            JSONArray list = result.getJSONArray("list");
            for (int i = 0; i < list.length(); i++) {
                JSONObject item = list.getJSONObject(i);
                String errorCode = item.getString("errorCode");
                String errorMessage = "";
                if (!StringUtils.isEmpty(errorCode)) {
                    errorMessage = ResponseUtil._getErrorMessage(errorCode);
                }
                item.put("errorMessage", errorMessage);
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getLocalizedMessage());
        }
        return resp;
    }
    /*public static JSONObject _getTestAttemptData(Map<String,Object> allParams){
        allParams.put("divisions","5");
        //allParams.put("startMarks",ClientUtil.DEFAULT_FETCH_SIZE_10);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.QUESTIONS_WEB_SERVICE_URL + "/attempts/getTestStats", allParams);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET ATEMPT TEST DATA");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET ATEMPT TEST DATA");
        JSONObject testAttemptData = getJSON(promise);
        return testAttemptData;
    }*/
    public static void preTest(Map<String,Object> allParams,Boolean direct){
        JSONObject data = _getTestDetails(allParams);
        JSONObject toppersData = null;
        String testIdStr = Scope.Params.current().get("testId");
        if(direct){
            String includeName="Tests/preTest.html";
            flash.put("ENTRY", "DIRECT");
            render("Application/myPages.html",includeName,data,toppersData,testIdStr);
        }else{
            render("Tests/preTest.html",data,toppersData,testIdStr);
        }
    }

    public static void checkPasswordForTestPopup(){
        render();
    }

    public static void postTest(Map<String,Object> allParams,boolean direct){
            JSONObject data = _getTestDetails(allParams);
            JSONObject testAnalytics = _getUserTestAnalytics(allParams);
            JSONObject toppersData = null;
            String testIdStr = Scope.Params.current().get("testId");
            toppersData = Validation.verifyResponse(toppersData);
            testAnalytics = Validation.verifyResponse(testAnalytics);
            if(direct){
             String includeName="Tests/postTest.html";
             flash.put("ENTRY", "DIRECT");
             render("Application/myPages.html",includeName,data,toppersData,testAnalytics,testIdStr);
       }else{
             render("Tests/postTest.html",data,toppersData,testAnalytics,testIdStr);
       }
       render(data,toppersData,testAnalytics,testIdStr);
    }
    public static void viewTest(){
        Map<String,Object> allParams=getReqParams();
        recordActivity(ClientUtil.ActivityPages.TEST, ClientUtil.ActivityAction.OPEN);
        allParams.put("start",ClientUtil.DEFAULT_FETCH_START);
        allParams.put("size",ClientUtil.DEFAULT_FETCH_SIZE_10);
        String testIdStr = Scope.Params.current().get("testId");
        boolean isReattempt = _isReAttemptTest(testIdStr);
        if(isReattempt == true){
            JSONObject data = _getTestDetails(allParams);
            JSONObject testAnalytics = _getUserTestAnalytics(allParams);
            JSONObject toppersData = _getToppers(allParams);

            toppersData = Validation.verifyResponse(toppersData);
            testAnalytics = Validation.verifyResponse(testAnalytics);

            render("Tests/postTest.html",data,toppersData,testAnalytics,testIdStr);
        }else{
            JSONObject data = _getTestDetails(allParams);
            JSONObject toppersData = _getToppers(allParams);

            toppersData = Validation.verifyResponse(toppersData);

            render("Tests/preTest.html",data,toppersData,testIdStr);
        }
    }
    public static void testDirect(@Required String testId){
        Map<String,Object> allParams=getReqParams();
        recordActivity(ClientUtil.ActivityPages.TEST, ClientUtil.ActivityAction.OPEN);
        allParams.put("start",ClientUtil.DEFAULT_FETCH_START);
        allParams.put("size",ClientUtil.DEFAULT_FETCH_SIZE_10);
        boolean isReattempt = _isReAttemptTest(testId);
        String includeName="";
        if(isReattempt == true){
            JSONObject data = _getTestDetails(allParams);
            JSONObject testAnalytics = _getUserTestAnalytics(allParams);
            JSONObject toppersData = _getToppers(allParams);
            String testIdStr = Scope.Params.current().get("testId");
            toppersData = Validation.verifyResponse(toppersData);
            testAnalytics = Validation.verifyResponse(testAnalytics);
            includeName="Tests/postTest.html";
            flash.put("ENTRY", "DIRECT");
            render("Application/myPages.html",includeName,data,toppersData,testAnalytics,testIdStr);
        }else{
            JSONObject data = _getTestDetails(allParams);
            JSONObject toppersData = _getToppers(allParams);

            String testIdStr = Scope.Params.current().get("testId");
            toppersData = Validation.verifyResponse(toppersData);
            includeName="Tests/preTest.html";
            flash.put("ENTRY", "DIRECT");
            render("Application/myPages.html",includeName,data,toppersData,testIdStr);
        }
    }
    public static void attempt() {
        Map<String,Object> allParams=getReqParams();
        String targetType = Scope.Params.current().get("target.type");
        String targetId = Scope.Params.current().get("target.id");
        String testIdStr = Scope.Params.current().get("testId");
        String pdfIdStr = Scope.Params.current().get("pdfId");
        String orgIdStr = Scope.Params.current().get("orgId");
        String testNameStr = Scope.Params.current().get("testName");
//        recordActivity(ClientUtil.ActivityPages.TEST_ATTEMPT, ClientUtil.ActivityAction.ATTEMPTED);
        JSONObject myOrgInfo = Institute._setOrgParams(orgIdStr);
        boolean isNewUi = true;
        JSONObject docInfo = null;
        Logger.log4j.info("Pdf Id is :: "+pdfIdStr);
        if(!StringUtils.isEmpty(pdfIdStr) && !pdfIdStr.equalsIgnoreCase("null")){
            Logger.log4j.info("About to call _getDocumentInfo");
            Map<String,Object> docParams= new HashMap<String,Object>();
            docParams.put("id",pdfIdStr);
            docParams.put("orgId",orgIdStr);
            docInfo = MyContents._getDocumentInfo(docParams);
        }
        try{
            Application.recordActivity(ClientUtil.ActivityPages.TEST_ATTEMPT,ClientUtil.ActivityAction.ATTEMPTED,ClientUtil.Entity.TEST,testIdStr);
            if(StringUtils.equals("STUDENT", request.params.get("userRole"))){
                Widgets._markContentCompleted(allParams);
            }
        }catch(Exception ex){}
        JSONObject cacheResp = null;
//        cacheResp = _verifyTestCache(testIdStr);
//        if(cacheResp!=null){
//            Logger.log4j.info("Attempt Log :: Render test data from CACHE Handler testId : "+testIdStr);
//            Logger.log4j.info("Attempt Log :: Attempt Id is : "+UIComTest._getAttemptId());
//            render(cacheResp,testIdStr,testNameStr);
//            return;
//        }
        // CLEAR Previous cache if not cleared. This case is for test ending abruptly
        uicom.util.TestsCacheHandler._clearCache(session.get("userId").toString());
        session.remove("testAttemptId");
        _setTestSessions();
        //IF VERIFIED
        allParams.put("entityId",testIdStr);
        allParams.put("entityType","TEST");
        String sectionId = request.params.get("sectionId");
        allParams.put("sectionId",sectionId);
        JSONObject attempt = _createTestAnalytics(allParams);
        try {
            if(!StringUtils.isEmpty(attempt.getString("errorMessage"))){
                render(attempt,isNewUi);
            }
            if(attempt.getJSONObject("result").getBoolean("isReattempt")){
                allParams.put("testState", "RESUMED");
            }else{
                allParams.put("testState", "NOTRESUMED");
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        allParams.put("qTypeDistribution",true);
        allParams.put("id", testIdStr);
        JSONObject data = null;
        if(allParams.get("testState").equals("RESUMED")){
            data = _getQuestionsData(allParams);
        }else{
            String testQuestions = Cache.get(testIdStr+"_NOTRESUMED"+"_attempt", String.class);
            if(!StringUtils.isEmpty(testQuestions)){
                Logger.log4j.info("Attempt Log :: Render testQuestions data from Cache for testId : "+testIdStr);
                data = getCacheDataInJsonObject(testQuestions);
            }else {
                Logger.log4j.info("Attempt Log :: Assigning testQuestions data to Cache for testId : "+testIdStr);
                data = _getQuestionsData(allParams);
                if(data != null)
                    Cache.set(testIdStr+"_NOTRESUMED"+"_attempt", data.toString(), "1h");
            }
        }
//        cacheResp = _setTestCache(data,testIdStr,testNameStr);
        session.put("testAttemptId", UIComTest._getAttemptId());
        Logger.log4j.info("Attempt Log :: Attempt Id is : "+UIComTest._getAttemptId());
        render(data,cacheResp,testIdStr,testNameStr,pdfIdStr,orgIdStr,docInfo,myOrgInfo,isNewUi,targetType,targetId);
    }

    public static void resumeAttempt() {
        Map<String,Object> allParams=getReqParams();
        String targetType = Scope.Params.current().get("target.type");
        String targetId = Scope.Params.current().get("target.id");
        String testIdStr = Scope.Params.current().get("testId");
        String pdfIdStr = Scope.Params.current().get("pdfId");
        String orgIdStr = Scope.Params.current().get("orgId");
        String testNameStr = Scope.Params.current().get("testName");
        recordActivity(ClientUtil.ActivityPages.TEST_ATTEMPT, ClientUtil.ActivityAction.RESUMED);
        JSONObject myOrgInfo = Institute._setOrgParams(orgIdStr);
        boolean isNewUi = true;
        JSONObject docInfo = null;
        Logger.log4j.info("Pdf Id is :: "+pdfIdStr);
        if(!StringUtils.isEmpty(pdfIdStr) && !pdfIdStr.equalsIgnoreCase("null")){
            Logger.log4j.info("About to call _getDocumentInfo");
            Map<String,Object> docParams= new HashMap<String,Object>();
            docParams.put("id",pdfIdStr);
            docParams.put("orgId",orgIdStr);
            docInfo = MyContents._getDocumentInfo(docParams);
        }
        try{
            Application.recordActivity(ClientUtil.ActivityPages.TEST_ATTEMPT,ClientUtil.ActivityAction.RESUMED,ClientUtil.Entity.TEST,testIdStr);
        }catch(Exception ex){}
        JSONObject cacheResp = null;
        // CLEAR Previous cache if not cleared. This case is for test ending abruptly
        uicom.util.TestsCacheHandler._clearCache(session.get("userId").toString());
        session.remove("testAttemptId");
        _setTestSessions();
        //IF VERIFIED
        allParams.put("entityId",testIdStr);
        allParams.put("entityType","TEST");
        allParams.put("testState", "RESUMED");
        String sectionId = request.params.get("sectionId");
        allParams.put("sectionId",sectionId);
        JSONObject attempt = _createTestAnalytics(allParams);
        try {
            if(!StringUtils.isEmpty(attempt.getString("errorMessage"))){
                render("Tests/attempt.html",attempt,isNewUi);
            }
        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        allParams.put("qTypeDistribution",true);
        allParams.put("id", testIdStr);
        allParams.put("attemptId", UIComTest._getAttemptId());
        JSONObject data = _getQuestionsData(allParams);
        cacheResp = _setTestCache(data,testIdStr,testNameStr);
        session.put("testAttemptId", UIComTest._getAttemptId());
        Logger.log4j.info("Attempt Log :: Attempt Id is : "+UIComTest._getAttemptId());
        render("Tests/attempt.html",data,cacheResp,testIdStr,testNameStr,pdfIdStr,orgIdStr,docInfo,myOrgInfo,isNewUi,targetId,targetType);
    }
    public static void endTest(String testId){
        Map<String,Object> allParams=getReqParams();
        allParams.put("start",ClientUtil.DEFAULT_FETCH_START);
        allParams.put("size",ClientUtil.DEFAULT_FETCH_SIZE_10);
        allParams = _putTestSessions(allParams);
        allParams.put("entityId",testId);
        allParams.put("entityType","TEST");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.TEST_SERVICE_URL + "/analytics/endAttempt", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT END TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT END TEST");
        JSONObject resp = getJSON(promise);
        resp = Validation.verifyResponse(resp);

        uicom.util.TestsCacheHandler._clearCache(session.get("userId").toString());
        session.remove("testAttemptId");
        _setTestSessions();

        renderJSON(resp.toString());
        /*if(allParams.containsKey("orgId")){
            String orgId = Scope.Params.current().get("orgId");
            getOrgTest(testId,orgId);
        }else{
            postTest(allParams, true);
        }*/
    }

    //------TEST CREATION----------
    public static enum EntryPage{
    	CREATION,ADDQUESTION,ASSIGN
    }
    private static JSONObject getOtherUiData(){
    	int pageIndex = 0;
    	try{
	    	String entryPageStr = Scope.Params.current().get("entryPage").toString().toUpperCase();
	    	EntryPage value = EntryPage.valueOf(entryPageStr);
	    	switch(value){
	    		case CREATION : pageIndex = 0;
	    						break;
	    		case ADDQUESTION:pageIndex=2;
	    						break;
	    		case ASSIGN : pageIndex = 3;
	    						break;
	    		default : pageIndex = 0;
	    						break;
			}
    	}catch(Exception e){
    		pageIndex = 0;
    	}
    	String str_data = "{'entryPageIndex':0,'minsStrList':['00','05','10','15','20','25','30','35','40','45','50','55']}";
    	JSONObject data = null;
    	try {
			data = new JSONObject(str_data);
		} catch (JSONException e) {
			Logger.log4j.error(e);
		}
    	try {
			data.put("entryPageIndex",pageIndex);
		} catch (JSONException e) {
			Logger.log4j.error(e);
		}
    	return data;
    }
    protected static JSONObject getTargetExams(Map<String,Object> allParams){
                allParams.put("type", "EXAM");
                allParams.put("size","-1");
                Promise<JSONResponseWrapper> promise = client(ClientUtil.BOARDS_WEB_SERVICE_URL + "/Boards/getBoards", allParams);
	        Logger.log4j.info("BEFORE AWAIT GET TARGET EXAMS");
	        await(promise);
	        Logger.log4j.info("AFTER AWAIT  GET TARGET EXAMS");
	        JSONObject targetExam = getJSON(promise);
                return targetExam;
    }
    public static void creation(){
        Map<String,Object> allParams=getReqParams();
    	JSONObject otherUiData = getOtherUiData();
    	JSONObject targetExam = null;
    	int entryPageValue=0;
		try {
			entryPageValue = otherUiData.getInt("entryPageIndex");
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Logger.log4j.error(e);
		}
    	if(entryPageValue == 0){
	        targetExam = getTargetExams(allParams);
        }
        String testIdStr = Scope.Params.current().get("testId");
        targetExam = Validation.verifyResponse(targetExam);
        render(targetExam,otherUiData,testIdStr);
    }
    public static void detailsGetTopics(){
        Map<String,Object> allParams=getReqParams();
        allParams.put("type", "TOPIC");
        allParams.put("size",ClientUtil.DEFAULT_FETCH_SIZE_10);//100->10
    	Promise<JSONResponseWrapper> promise = client(ClientUtil.BOARDS_WEB_SERVICE_URL + "/Boards/getBoards", allParams);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET TOPICS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET TOPICS");
        JSONObject topicsData = getJSON(promise);
        //Logger.log4j.info("exam data = "+topicsData);
        topicsData = Validation.verifyResponse(topicsData);
        render(topicsData);
    }
    public static void createTest(){
    	Promise<JSONResponseWrapper> promise = client(ClientUtil.QUESTIONS_WEB_SERVICE_URL + "/tests/createTest", null);
        Logger.log4j.info("BEFORE AWAIT :CREATE TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :CREATE TEST");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        renderJSON(data.toString());
    }
    public static void examResult() {
        Map<String,Object> allParams=getReqParams();
    	JSONObject data = _getQuestionsData(allParams);
    	render(data);
    }
    public static void getAddQuestions() {
        Map<String,Object> allParams=getReqParams();
    	JSONObject data = _getTestDetails(allParams);
        JSONObject targetExam = getTargetExams(allParams);
        targetExam = Validation.verifyResponse(targetExam);
    	render(data,targetExam);
    }
    public static void _getTestDetailsForAssign(){
        Map<String,Object> allParams=getReqParams();
    	JSONObject data = _getTestDetails(allParams);
    	render(data);
    }
    public static void testScheduleList(){
    	Promise<JSONResponseWrapper> promise = client(ClientUtil.QUESTIONS_WEB_SERVICE_URL + "/tests/getScheduledKeys", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject scheduleList = getJSON(promise);
        render(scheduleList);
    }
    public static void getFriendsList(){
    	String str_data = "{'errorCode':'','errorResponse':'','result':[{'followType':'FOLLOWING','user':{'lastName':'Kumar','username':'geet@vedantu.com','userId':'4f463d25e7deae44f7fbf648','profilePic':'/public/images/profileIcon.jpg','addedOn':1334310979377,'points':0,'firstName':'Geetha','mongoId':'4f463d25e7deae44f7fbf648'}},{'followType':'FOLLOWING','user':{'lastName':'Kumar','username':'geet@vedantu.com','userId':'4f463d25e7deae44f7fbf648','profilePic':'/public/images/profileIcon.jpg','addedOn':1334310979377,'points':0,'firstName':'Amit','mongoId':'4f463d25e7deae44f7fbf648'}},{'followType':'FOLLOWING','user':{'lastName':'Kumar','username':'geet@vedantu.com','userId':'4f463d25e7deae44f7fbf648','profilePic':'/public/images/profileIcon.jpg','addedOn':1334310979377,'points':0,'firstName':'Sunil','mongoId':'4f463d25e7deae44f7fbf648'}},{'followType':'FOLLOWING','user':{'lastName':'Kumar','username':'geet@vedantu.com','userId':'4f463d25e7deae44f7fbf648','profilePic':'/public/images/profileIcon.jpg','addedOn':1334310979377,'points':0,'firstName':'Mohan','mongoId':'4f463d25e7deae44f7fbf648'}}]}";
    	JSONObject data = null;
    	try {
			data = new JSONObject(str_data);
		} catch (JSONException e) {
			Logger.log4j.error(e);
		}
        data = Validation.verifyResponse(data);
    	render(data);
    }
    public static void getFilteredQuestions(){
    	Promise<JSONResponseWrapper> promise = client(ClientUtil.QUESTIONS_WEB_SERVICE_URL + "/Questions/getQuestions", null);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET QUESTIONS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET QUESTIONS");
        JSONObject questionList = getJSON(promise);
        //Logger.log4j.info("exam data = "+topicsData);
        render(questionList);
    }
    public static void addQuestionToList(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.QUESTIONS_WEB_SERVICE_URL + "/Tests/addQuestion", null);
        Logger.log4j.info("BEFORE AWAIT Add Question To Test");
        await(promise);
        Logger.log4j.info("AFTER AWAIT Add Question To Test");
        JSONObject responseJSON = getJSON(promise);
        renderJSON(responseJSON.toString());
    }
     public static void removeQuestionFromList(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.QUESTIONS_WEB_SERVICE_URL + "/Tests/removeQuestion", null);
        Logger.log4j.info("BEFORE AWAIT remove Question from Test");
        await(promise);
        Logger.log4j.info("AFTER AWAIT remove Question from Test");
        JSONObject responseJSON = getJSON(promise);
        renderJSON(responseJSON.toString());
     }
     public static void publishMyTest(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.QUESTIONS_WEB_SERVICE_URL + "/Tests/publishTest", null);
        Logger.log4j.info("BEFORE AWAIT PUBLISH Test");
        await(promise);
        Logger.log4j.info("AFTER AWAIT PUBLISH from Test");
        JSONObject responseJSON = getJSON(promise);
        renderJSON(responseJSON.toString());
     }
     public static void listUserTest(){
        Map<String,Object> allParams=getReqParams();
        allParams.put("start",ClientUtil.DEFAULT_FETCH_START);
        allParams.put("size","100");
        JSONObject allTest=_getTests(allParams);
        allTest = Validation.verifyResponse(allTest);
        render(allTest);
     }
     public static JSONObject _getTests(Map<String,Object> allParams){
//         Logger.log4j.info("Key :: "+Scope.Params.current().get("orgId") + Scope.Params.current().get("programId")
//                         + Scope.Params.current().get("centerId")
//                         + Scope.Params.current().get("sectionId")
//                         + Scope.Params.current().get("userId")
//                         + Scope.Params.current().get("start")
//                         + request.params.get("brdIds[0]") + request.params.get("orderBy")
//                         +"_getTests");
//        String resp = Cache.get(
//                 Scope.Params.current().get("orgId") + Scope.Params.current().get("programId")
//                         + Scope.Params.current().get("centerId")
//                         + Scope.Params.current().get("sectionId")
//                         + Scope.Params.current().get("userId")
//                         + Scope.Params.current().get("start")
//                         + request.params.get("brdIds[0]") + request.params.get("orderBy")
//                         +"_getTests", String.class);
//        if(!StringUtils.isEmpty(resp)){
//            Logger.log4j.info("Served _getTests from cache");
//             JSONObject allTests = getCacheDataInJsonObject(resp);
//             return allTests;
//        }else{
            Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/tests/getTests", allParams);
            Logger.log4j.info("BEFORE AWAIT PUBLISH Test");
            await(promise);
            Logger.log4j.info("AFTER AWAIT PUBLISH from Test");
            JSONObject allTests = getJSON(promise);
//            if(allTests != null)
//                Cache.set(Scope.Params.current().get("orgId") + Scope.Params.current().get("programId")
//                        + Scope.Params.current().get("centerId")
//                        + Scope.Params.current().get("sectionId")
//                        + Scope.Params.current().get("userId")
//                        + Scope.Params.current().get("start")
//                        + request.params.get("brdIds[0]") + request.params.get("orderBy")
//                        +"_getTests", allTests.toString(), "5mn");
            return allTests;
//        }
     }
     //by ajith utilities
    public static void testItems() {
        JSONObject tests=Tests._getTests(null);
        render(tests);
    }
    public static void testTileItems() {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.SEARCH_WEB_SERVICE_URL +"/search/search",null);
        Logger.log4j.info("BEFORE AWAIT - GET SEARCH RESPONSE");
        await(promise);
        Logger.log4j.info("AFTER AWAIT - GET SEARCH RESPONSE");
        JSONObject tests = getJSON(promise);
        render(tests);
    }
    public static void createTestDirect(){
        MyContents.myContentDirect("Tests");
    }
    public static void testWidgets(){
        render();
    }
    private static Map<String,Object> _putTestEntityParams(String testId,Map<String,Object> allParams){
        Params pr = Scope.Params.current();
        String targetUserId = pr.get("targetUserId");
        if(targetUserId==null||targetUserId.isEmpty()){
            targetUserId = session.get("userId");
        }
        if(allParams!=null){
            allParams.put("entity.id", testId);
            allParams.put("entity.type", "TEST");
            allParams.put("targetUserId", targetUserId);
            return allParams;
        }else{
            pr.put("entity.id", testId);
            pr.put("entity.type", "TEST");
            pr.put("targetUserId", targetUserId);
        }
        return null;
    }
    //Org Test Fetch
    public static void drawTestQuestions(){
        Map<String,Object> allParams=getReqParams();
        JSONObject questions = _getTestAnalyticsQuestions(allParams);
        String testId = request.params.get("entity.id");
        allParams.put("id", testId);
        JSONObject data = _getTestDetails(allParams);
        render("tags/test/postTestQues.html",questions,data);
    }
    public static void drawTeacherTestQuestions(){
        Map<String,Object> allParams=getReqParams();
        allParams.put("targetUserId", session.get("userId"));
        JSONObject questionsData = _getTeacherTestAnalyticsQuestions(allParams);
        render("Tests/postTestTeachersQuestions.html",questionsData);
    }

    public static void drawTestSubjectiveQuestions(){
        Map<String,Object> allParams=getReqParams();
        allParams.put("targetUserId", session.get("userId"));
        JSONObject data = _getTestSubjectiveQuestions(allParams);
        render(data);
    }

    public static void subjectiveQuestionPage(String orgId,
			@Required String testId, @Required String id) {
		putSubjectiveQuestionReqParams();
		Map<String, Object> allParams = getReqParams();
		String includeInstFile = "Questions/subjectiveQuestionPage.html";
		JSONObject myOrgInfo = Institute._setOrgParams(orgId);
		JSONObject questionAttempts = _userSubjectiveQuestionAttempts(allParams);
		render("Institute/header.html", includeInstFile, questionAttempts,
				myOrgInfo);
	}

    public static void subjectiveQuestionDirect(String orgId,
			@Required String testId, @Required String id) {
		putSubjectiveQuestionReqParams();
		Map<String, Object> allParams = getReqParams();
		JSONObject myOrgInfo = Institute._setOrgParams(orgId);
		flash.put("ENTRY", "DIRECT");
		String includeName = "Institute/header.html";
		String includeInstFile = "Questions/subjectiveQuestionPage.html";
		JSONObject questionAttempts = _userSubjectiveQuestionAttempts(allParams);
		render("Application/myPages.html", includeName, includeInstFile,
				questionAttempts, myOrgInfo);
	}

	private static void putSubjectiveQuestionReqParams() {
		Params reqParams = request.params;
		if (StringUtils.isEmpty(reqParams.get("start"))) {
			request.params.put("start", "0");
		}
		if (StringUtils.isEmpty(reqParams.get("size"))) {
			request.params.put("size", "10");
		}
		if (StringUtils.isEmpty(reqParams.get("loadQuestionInfo"))) {
			request.params.put("loadQuestionInfo", "true");
		}
	}

    public static void getMoreSubjectiveQuestionAttempts(String orgId,
			@Required String testId, @Required String id) {
		putSubjectiveQuestionReqParams();
		Map<String, Object> allParams = getReqParams();
		JSONObject myOrgInfo = Institute._setOrgParams(orgId);
		JSONObject questionAttempts = _userSubjectiveQuestionAttempts(allParams);
		render("Questions/subjectiveQuestionPage.html", questionAttempts,
				myOrgInfo);
	}

    public static void drawEachQuestionStudentsCorrectWrongList(){
        Map<String,Object> allParams=getReqParams();
        allParams.put("targetUserId", session.get("userId"));
        allParams.put("entityId","UNKNOWN");
        allParams.put("entityType", "UNKNOWN");
        JSONObject queAnalyticsData = questionCorrectWrongData(allParams);
        render("Tests/drawEachQuestionStudentsCorrectWrongList.html",queAnalyticsData);
    }
    public static JSONObject questionCorrectWrongData(Map<String,Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/analytics/getStudentsQuestionsAnsweredList", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT END TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT END TEST");
        JSONObject queAnalyticsData = getJSON(promise);
        queAnalyticsData = Validation.verifyResponse(queAnalyticsData);
        return queAnalyticsData;
    }

    public static void getOrgPreTest(@Required String id,String orgId){
        JSONObject myOrgInfo = Institute._setOrgParams(orgId);
        try{
            Application.recordActivity(ClientUtil.ActivityPages.PRE_TEST,ClientUtil.ActivityAction.ATTEMPTED,ClientUtil.Entity.TEST,id);
        }catch(Exception ex){}
            request.params.put("targetUserId", session.get("userId"));
            boolean isAttempted = _isReAttemptTest(id);
            if(isAttempted){
                getOrgTest(id,orgId);
//                boolean  isSecure=Http.Request.current().secure;
//                if(isSecure){
//
//                }
//                redirect("/tests/getorgtest?orgId="+orgId+"&id="+id);
            }
            Map<String,Object> allParams=getReqParams();
            allParams = _putTestEntityParams(id,allParams);
            Params prms = Scope.Params.current();
            prms.put("targetUserId",(String)allParams.get("targetUserId"));

            JSONObject data = _getTestDetails(allParams);
            JSONObject toppersData = null;
            String testIdStr = id;
            String includeInstFile = "Tests/preTest.html";
            String noInstHeader = Scope.Params.current().get("noInstHeader");
            try{
                Application._markEntityView(id,ClientUtil.Entity.TEST);
            }catch(Exception err){}
	    if(noInstHeader==null || !"true".equals(noInstHeader)){
                render("Institute/header.html",includeInstFile,data,toppersData,testIdStr,myOrgInfo);
            }else{
                render(includeInstFile,data,toppersData,testIdStr,myOrgInfo);
            }
    }
    public static void orgPreTestDirect(@Required String orgId,@Required String id){
        JSONObject myOrgInfo = Institute._setOrgParams(orgId);
        try{
            Application.recordActivity(ClientUtil.ActivityPages.PRE_TEST,ClientUtil.ActivityAction.ATTEMPTED,ClientUtil.Entity.TEST,id);
        }catch(Exception ex){}
            request.params.put("targetUserId", session.get("userId"));
            boolean isAttempted = _isReAttemptTest(id);
            if(isAttempted){
                orgTestDirect(orgId,id);
                return;
            }
            Map<String,Object> allParams=getReqParams();
            allParams = _putTestEntityParams(id,allParams);

            Params prms = Scope.Params.current();
            prms.put("targetUserId",(String)allParams.get("targetUserId"));

            JSONObject data = _getTestDetails(allParams);
            JSONObject toppersData = null;
            String testIdStr = id;
            String includeInstFile = "Tests/preTest.html";

            try{
                Application._markEntityView(id,ClientUtil.Entity.TEST);
            }catch(Exception err){}
            String includeName="Institute/header.html";
            flash.put("ENTRY", "DIRECT");
            render("Application/myPages.html",includeName,includeInstFile,data,toppersData,testIdStr,myOrgInfo);
    }
    public static void getOrgTest(@Required String id,String orgId){
        JSONObject myOrgInfo = Institute._setOrgParams(orgId);
        try{
            Application.recordActivity(ClientUtil.ActivityPages.TEST_ANALYTICS,ClientUtil.ActivityAction.ATTEMPTED,ClientUtil.Entity.TEST,id);
        }catch(Exception ex){}
            /*boolean isAttempted = _isReAttemptTest(id);
            if(!isAttempted){
                getOrgPreTest(id);
                return;
            }*/
            Map<String,Object> allParams=getReqParams();
            allParams = _putTestEntityParams(id,allParams);
            Params prms = Scope.Params.current();
            prms.put("targetUserId",(String)allParams.get("targetUserId"));
            String targetUserRole = prms.get("targetUserRole");
            if(targetUserRole == null || targetUserRole.isEmpty()){
                targetUserRole = prms.get("userRole");
                targetUserRole = targetUserRole==null?"STUDENT":targetUserRole;
            }
            prms.put("callerUserRole",params.get("userRole"));
            prms.put("userRole",targetUserRole);
            //JSONObject data = getOrgTestDetails(allParams);
            JSONObject data = _getTestDetails(allParams);
            JSONObject testStatus = _getTestStatus(allParams);
            JSONObject toppersData = null;
            if(!targetUserRole.equals("STUDENT")){
                toppersData = _getToppers(allParams);
            }
            String testIdStr = id;
            String includeInstFile = "";
            String noInstHeader = Scope.Params.current().get("noInstHeader");
            JSONObject testAnalytics = null;
            JSONObject marksDistribution = _getTestMarksDistribution(allParams);
            JSONObject resultTypeList = webUtils.TemplateHelper._getParamsResultTypeList();
            allParams.put("entity.id", id);
            allParams.put("entity.type", "TEST");
            allParams.put("orgId",orgId);
            JSONObject ratings = MyContents._getEntityRatingsAndFeedback(allParams);
            try{
                Application._markEntityView(id,ClientUtil.Entity.TEST);
            }catch(Exception err){}
            if(targetUserRole.equals("STUDENT")){
                testAnalytics = _getUserTestAnalytics(allParams);
                includeInstFile = "Tests/postTest.html";
            }else{
                includeInstFile = "Tests/postTestTeacher.html";
            }
	    if(noInstHeader==null || !"true".equals(noInstHeader)){
                render("Institute/header.html",includeInstFile,data,testStatus,toppersData,testAnalytics,marksDistribution,testIdStr,resultTypeList,myOrgInfo, ratings);
            }else{
                render(includeInstFile,data,testStatus,toppersData,testAnalytics,testIdStr,marksDistribution,resultTypeList,myOrgInfo, ratings);
            }
    }
    public static void orgTestDirect(@Required String orgId,@Required String id){
        JSONObject myOrgInfo = Institute._setOrgParams(orgId);
        try{
            Application.recordActivity(ClientUtil.ActivityPages.TEST_ANALYTICS,ClientUtil.ActivityAction.ATTEMPTED,ClientUtil.Entity.TEST,id);
        }catch(Exception ex){}
            /*boolean isAttempted = _isReAttemptTest(id);
            if(!isAttempted){
                orgPreTestDirect(orgId, id);
                return;
            }*/
            Map<String,Object> allParams=getReqParams();
            allParams = _putTestEntityParams(id,allParams);
            Params prms = Scope.Params.current();
            prms.put("targetUserId",(String)allParams.get("targetUserId"));
            String targetUserRole = prms.get("targetUserRole");
            if(targetUserRole == null || targetUserRole.isEmpty()){
                targetUserRole = prms.get("userRole");
                targetUserRole = targetUserRole==null?"STUDENT":targetUserRole;
            }
            prms.put("callerUserRole",params.get("userRole"));
            prms.put("userRole",targetUserRole);
            Logger.log4j.info("TARGET USER ROLE ================ "+targetUserRole);
            JSONObject data = _getTestDetails(allParams);
            JSONObject testStatus = _getTestStatus(allParams);
            JSONObject toppersData = null;
            if(!targetUserRole.equals("STUDENT")){
                toppersData = _getToppers(allParams);
            }
            String testIdStr = id;
            String includeInstFile = "";
            JSONObject testAnalytics = null;
            JSONObject marksDistribution = _getTestMarksDistribution(allParams);
            JSONObject resultTypeList = webUtils.TemplateHelper._getParamsResultTypeList();
            allParams.put("entity.id", id);
            allParams.put("entity.type", "TEST");
            allParams.put("orgId",orgId);
            JSONObject ratings = MyContents._getEntityRatingsAndFeedback(allParams);
            try{
                Application._markEntityView(id,ClientUtil.Entity.TEST);
            }catch(Exception err){}
            if(targetUserRole.equals("STUDENT")){
                testAnalytics = _getUserTestAnalytics(allParams);
                includeInstFile = "Tests/postTest.html";
            }else{
                includeInstFile = "Tests/postTestTeacher.html";
            }
            String includeName="Institute/header.html";
            flash.put("ENTRY", "DIRECT");
            render("Application/myPages.html",includeName,testStatus,includeInstFile,data,toppersData,testAnalytics,marksDistribution,testIdStr,myOrgInfo,resultTypeList, ratings);
    }

    private static JSONObject _getTestStatus(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(ClientUtil.TEST_SERVICE_URL + "/analytics/_testStatus", allParams);
        Logger.log4j.info("BEFORE AWAIT : _getTestStatus");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  _getTestStatus");
        JSONObject data = Validation.verifyResponse(getJSON(promise));
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
        return data;
    }
    public static void testResultSheet(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/getEntityResultAnalytics", null);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET TOPICS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET TOPICS");
        JSONObject testFullDetails = Validation.verifyResponse(getJSON(promise));
        render(testFullDetails);
    }

    public static void testStudentsAttemptsList(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/getStudentsListFromEntityAttempts", null);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET TOPICS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET TOPICS");
        JSONObject testFullDetails = Validation.verifyResponse(getJSON(promise));
        render(testFullDetails);
    }

	public static void gradeTestSubjectiveQuestion() {
		Promise<JSONResponseWrapper> promise = client(
				ClientUtil.CONTENT_SERVICE_URL
						+ "/analytics/gradeTestSubjectiveQuestion", null);
		Logger.log4j.info("BEFORE AWAIT GRADE-TEST-SUBJECTIVE QUESTION");
		await(promise);
		Logger.log4j.info("AFTER AWAIT GRADE-TEST-SUBJECTIVE QUESTION");
		JSONObject gradedQuesResp = Validation.verifyResponse(getJSON(promise));
		renderJSON(gradedQuesResp.toString());
	}

    public static void resetStudentTest(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.TEST_SERVICE_URL
                + "/analytics/resetStudentTest", null);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET TOPICS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET TOPICS");
        JSONObject testDetails = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(testDetails.toString());
    }
    public static void regenerateStudentTestAnalytics(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/regenerateStudentTestAnalytics", null);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET TOPICS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET TOPICS");
        JSONObject testDetails = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(testDetails.toString());
    }

    public static void printableAnalytics() {
        Map<String, Object> allParams = getReqParams();
        allParams.put("test.type", "TEST");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/getUserEntityAnalyticsBySubject", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject response = getJSON(promise);
        render(response);
    }

    public static void downloadStudentTestAnalyticsSheet(){
        Map<String, Object> allParams = getReqParams();
        String testId = Scope.Params.current().get("entity.id");
        String targetUserId = Scope.Params.current().get("targetUserId");
        String orgIdStr = Scope.Params.current().get("orgId");
        allParams.put("entity.type","TEST");
        allParams.put("id", testId);
        JSONObject toppersData = _getToppers(allParams);
        JSONObject data = _getTestDetails(allParams);
        JSONObject testAnalytics = _getUserTestAnalytics(allParams);
        JSONObject myOrgInfo = Institute._setOrgParams(orgIdStr);
        render(toppersData,testAnalytics,myOrgInfo,data);
    }

    public static void testResultSheetStudents(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                + "/analytics/getEntityResultAnalytics", null);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET TOPICS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET TOPICS");
        JSONObject testFullDetails = Validation.verifyResponse(getJSON(promise));
        render(testFullDetails);
    }
    protected static String getStudentsMarksHTML
            (JSONArray testItems,JSONArray studentTestItems,
            String tdClassStrip,int paperIndex)throws JSONException{
        String htmlStr="";
        int spStart=0;
        for(int tp=0;tp<testItems.length();tp++){
            String marks="-";
            JSONObject testItem=testItems.getJSONObject(tp);
            for(int sp=spStart;sp<studentTestItems.length();sp++){
                JSONObject studentTestItem=studentTestItems.getJSONObject(sp);

                if(studentTestItem.getJSONObject("entity").getString("id").equals(testItem.getString("id"))){
                    if(testItem.has("metadata")&&studentTestItem.has("boards")){
                        String subjectMarks=getStudentsMarksHTML(testItem.getJSONArray("metadata"),
                                studentTestItem.getJSONArray("boards"),"subject",tp);
                        htmlStr+=subjectMarks;
                    }
                    marks="<div class='subjectMarks'>"+studentTestItem.getJSONObject("measures").getString("score")+"</div>";

                    //For correct wrong and left
                    JSONObject measures=studentTestItem.getJSONObject("measures");
                    int total=measures.getInt("correct")+measures.getInt("incorrect")+
                            measures.getInt("left");
                    if(tdClassStrip.equals("subject")){
                       marks+="<table class=\"testSubjectStats nonner\">" +
                        "<tr><td>Correct</td><td>Wrong</td><td>Left</td><td>Total</td></tr>" +
                        "<tr><td>"+measures.getString("correct")+"</td><td>"+measures.getString("incorrect")
                        +"</td><td>"+measures.getString("left")+"</td><td class=\"boldy color3\">"+total+"</td></tr>" +
                        "</table>";
                    }else{
                        marks+="<div class='testSubjectStats nonner'>"+total+"</div>";
                    }
                    spStart=++sp;
                }
                break;
            }
            if(marks.equals("-")){
                if(testItem.has("metadata")){
                    String subjectMarks=getStudentsMarksHTML(testItem.getJSONArray("metadata"),
                            new JSONArray(),"subject",tp);
                    htmlStr+=subjectMarks;
                }
            }

            int classIndex=paperIndex;
            if(paperIndex==-1){
                classIndex=tp;
            }

            String paperStr="paper paper"+(classIndex+1);

            //checking to hide elements
            String nonnerClass="";

            if(tdClassStrip.equals("subject")){
                if(tp>2){
                    nonnerClass=" nonnerForSubs";
                }
                if(classIndex>1){
                    nonnerClass+=" nonnerForPapers";
                }
            }else{
                if(tp>1){
                    nonnerClass=" nonnerForPapers";
                }
            }

            String className=tdClassStrip+" "+tdClassStrip+(tp+1)+" "+paperStr+nonnerClass;

            htmlStr+="<td class='"+className+"' data-subject='"+(tp+1)+"' data-paper='"+(classIndex+1)+"'>"
                    +marks+"</td>";
        }
        return htmlStr;
    }

    //offline tests
    public static JSONObject getOrgTestDetails(Map<String,Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.PROFILE_WEB_SERVICE_URL + "/organizations/getTestInfo", allParams);
        Logger.log4j.info("BEFORE AWAIT TEST-CREATION:GET TOPICS");
        await(promise);
        Logger.log4j.info("AFTER AWAIT TEST-CREATION:GET TOPICS");
        JSONObject data = getJSON(promise);
        data = Validation.verifyResponse(data);
        try {
            data = data.getJSONObject("result");
        } catch (JSONException ex) {
            java.util.logging.Logger.getLogger(Tests.class.getName()).log(Level.SEVERE, null, ex);
            data = null;
        }
        return data;
    }

    //Independent Leader Board
    public static void leaderBoard(){
         Map<String,Object> allParams=getReqParams();
         JSONObject toppersData = _getToppers(allParams);
         render(toppersData);
    }
    /*public static void getTestLeadersPage() {
        Scope.Params.current().put("start",ClientUtil.DEFAULT_FETCH_START);
        Scope.Params.current().put("size",ClientUtil.DEFAULT_FETCH_SIZE_50);
        Map<String, Object> allParams = getReqParams();
        JSONObject toppersData = _getToppersData(allParams);
        JSONObject data = _getTestDetails(allParams);
	String userRole = Scope.Params.current().get("userRole");
        flash.put("ENTRY", "DIRECT");
        render("Tests/testToppersDirect.html",data,toppersData,userRole);
    }
    public static void getTestLeadersDirect() {
        Scope.Params.current().put("start",ClientUtil.DEFAULT_FETCH_START);
        Scope.Params.current().put("size",ClientUtil.DEFAULT_FETCH_SIZE_50);
        Map<String, Object> allParams = getReqParams();
        JSONObject toppersData = _getToppersData(allParams);
        JSONObject data = _getTestDetails(allParams);
	String userRole = Scope.Params.current().get("userRole");
        flash.put("ENTRY", "DIRECT");
        String includeName = "Tests/testToppersDirect.html";
        render("Application/myPages.html", includeName,data,toppersData,userRole);
    }*/

    public static void endStudentTest(@Required String testId){
        Map<String,Object> allParams=getReqParams();
        allParams.put("entityId",testId);
        allParams.put("entityType","TEST");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.TEST_SERVICE_URL + "/analytics/endStudentAttempt", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT END TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT END TEST");
        JSONObject postTestResp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(postTestResp.toString());
    }

    public static void pauseStudentTest(@Required String testId){
        Map<String,Object> allParams=getReqParams();
        allParams.put("entityId",testId);
        allParams.put("entityType","TEST");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.TEST_SERVICE_URL + "/analytics/pauseStudentTest", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT PAUSE TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT PAUSE TEST");
        JSONObject postTestResp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(postTestResp.toString());
    }

    public static void resumeStudentTest(@Required String testId){
        Map<String,Object> allParams=getReqParams();
        allParams.put("entityId",testId);
        allParams.put("entityType","TEST");
        Promise<JSONResponseWrapper> promise = client(ClientUtil.TEST_SERVICE_URL + "/analytics/resumeStudentTest", allParams);
        Logger.log4j.info("BEFORE AWAIT : ATTEMPT RESUME TEST");
        await(promise);
        Logger.log4j.info("AFTER AWAIT :  ATTEMPT RESUME TEST");
        JSONObject postTestResp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(postTestResp.toString());
    }
}
