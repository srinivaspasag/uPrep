package controllers;


import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import controllers.AbstractUIController.JSONResponseWrapper;
import play.Logger;
import play.libs.F;
import play.libs.F.Promise;
import play.mvc.Scope;
import play.mvc.With;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

@With(Security.class)
public class QrTests extends AbstractQRUIController {

    public static void createTest(){
        recordActivity(ClientUtil.ActivityPages.TEST_CREATION,ClientUtil.ActivityAction.OPEN);
        request.params.put("testType","TEST");
        Map<String, Object> allParams=getReqParams();
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsTests/createTest",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        JSONObject finalResp=ResponseUtil.getCommonErrorResponse();
        try{
            String errorCode=resp.getString("errorCode");
            if(StringUtils.isEmpty(errorCode)){
                String testId=resp.getJSONObject("result").getString("id");
                allParams.put("id",testId);
                finalResp=_getTestDetails(allParams);
            }else{
                finalResp=resp;
            }
        }catch(Exception e){
            Logger.log4j.error("Error in fetching test details");
        }
        boolean autoGenerateFlag = Boolean.parseBoolean(request.params.get("autoGenerateFlag"));
        if(autoGenerateFlag){
            render("QrTests/autoGenerateTestChaptersList.html",finalResp);
        }
        render("QrTests/testPage.html",finalResp);
    }


    /**
     * This function is to auto generate test
     */
    public static void createTestAuto(){
        recordActivity(ClientUtil.ActivityPages.TEST_CREATION,ClientUtil.ActivityAction.OPEN);
        request.params.put("testType","TEST");
        Map<String, Object> allParams=getReqParams();
        // Removing body due to heavy payload.
        allParams.remove("body");
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsTests/createTestAuto",allParams);
        Logger.log4j.info("DIVESH BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("DIVESH AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        JSONObject finalResp=ResponseUtil.getCommonErrorResponse();
        try{
            String errorCode=resp.getString("errorCode");
            if(StringUtils.isEmpty(errorCode)){
                String testId=resp.getJSONObject("result").getString("id");
                allParams.put("id",testId);
                finalResp=_getTestDetails(allParams);
            }else{
                finalResp=resp;
            }
        }catch(Exception e){
            Logger.log4j.error("Error in fetching test details");
        }
        render("QrTests/testPage.html",finalResp);
    }

    public static void generateTest() {
        render();
    }

    public static void generateTestDirect(String orgId) {
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        String includeName = "QrTests/generateTest.html";
        flash.put("ENTRY", "DIRECT");
        render("Application/mapper.html", includeName, currentOrgInfo);
    }

    public static void testPage(){
        recordActivity(ClientUtil.ActivityPages.TEST,ClientUtil.ActivityAction.OPEN);
        request.params.put("testType","TEST");
        JSONObject finalResp=ResponseUtil.checkResponse(syncCaller(ClientUtil.CMDS_SERVICE_URL
                +"/cmdsTests/getTestInfo",null));
        JSONObject questions=null;
        Map<String, Object> allParams = getReqParams();
        allParams.put("entity.id",allParams.get("id") );
        allParams.put("entity.type","CMDSTEST" );
        JSONObject entityRatingCMDSInfo = _getCMDSEntityRatingInfo(allParams);
        try{
            JSONObject testResult=finalResp.getJSONObject("result");
            Boolean isPublished=testResult.getBoolean("published");
            if(isPublished){
                String brdId=testResult.getJSONArray("metadata")
                        .getJSONObject(0).getString("id");
                request.params.put("brdId", brdId);
                request.params.put("target","TEST_PREVIEW");
                questions=_previewTest(null);
            }
        }catch(Exception e){
            Logger.log4j.error("Error is fetching published field of test");
            Logger.log4j.error("Response is: "+finalResp);
        }
        render("QrTests/testPageRouter.html",finalResp,questions,entityRatingCMDSInfo);
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

    public static void getCMDSEntityReviews(String entityId){
        Map<String, Object> allParams = getReqParams();
        allParams.put("entity.id", entityId);
        allParams.put("entity.type",allParams.get("entityType"));
        allParams.put("entity.name",allParams.get("entityName"));
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CONTENT_SERVICE_URL +"/contents/getEntityReviews",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject reviews = ResponseUtil.checkResponse(getJSON(promise));
        render("QrTests/reviews.html",reviews);
    }

    public static void getCMDSEntityReviewsDirect(String entityId,String orgId){
        flash.put("ENTRY", "DIRECT");
        Map<String, Object> allParams = getReqParams();
        allParams.put("entity.id", entityId);
        allParams.put("entity.type", allParams.get("entityType"));
        allParams.put("entity.name",allParams.get("entityName"));
        String includeName = "QrTests/reviews.html";
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CONTENT_SERVICE_URL +"/contents/getEntityReviews",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject reviews = ResponseUtil.checkResponse(getJSON(promise));
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html",reviews,includeName,currentOrgInfo);
    }

    public static void getTestQuesns(){
        request.params.put("resultType","ALL");
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsQuestions/getQuestions",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject questions = ResponseUtil.checkResponse(getJSON(promise));
        render("QrQuestions/qrQuesns.html",questions);
    }
    public static void addQuesToTest(){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsTests/addQuestion",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void removeQuesFromTest(){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsTests/removeQuestion",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void previewTest(){
        recordActivity(ClientUtil.ActivityPages.TEST,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.TEST,request.params.get("testId"));
        request.params.put("testType","TEST");
        request.params.put("id", request.params.get("testId"));
        request.params.put("hideSectionLocking","true");
        request.params.put("hideAutoResumeTest","true");
        Map<String, Object> allParams=getReqParams();
        JSONObject finalResp=_getTestDetails(allParams);
        JSONObject questions= _previewTest(allParams);
        render(questions,finalResp);
    }
    public static void previewTestQuesns(){
        request.params.put("testType","TEST");
        JSONObject questions= _previewTest(null);
        render("QrQuestions/qrQuesns.html",questions);
    }

    public static void getTestStatus(){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsTests/getStatus",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp= ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void regenerateAnalytics(){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsTests/regenerateAnalytics",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp= ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }


    public static void createAssignment(){
        recordActivity(ClientUtil.ActivityPages.ASSIGNMENT_CREATION,ClientUtil.ActivityAction.OPEN);
        request.params.put("testType","ASSIGNMENT");
        Map<String, Object> allParams=getReqParams();
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsAssignments/createAssignment",
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        JSONObject finalResp=ResponseUtil.getCommonErrorResponse();
        try{
            String errorCode=resp.getString("errorCode");
            if(StringUtils.isEmpty(errorCode)){
                String assignmentId=resp.getJSONObject("result").getString("id");
                allParams.put("id",assignmentId);
                finalResp=_getAssignmentDetails(allParams);
            }else{
                finalResp=resp;
            }
        }catch(Exception e){
            Logger.log4j.error("Error in fetching test details");
        }
        render("QrTests/testPage.html",finalResp);
    }

    public static void assignmentPage(){
        recordActivity(ClientUtil.ActivityPages.ASSIGNMENT,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.ASSIGNMENT,request.params.get("id"));
        request.params.put("testType","ASSIGNMENT");
        JSONObject finalResp=ResponseUtil.checkResponse(syncCaller(ClientUtil.CMDS_SERVICE_URL
                +"/cmdsAssignments/getAssignmentInfo",null));
        JSONObject questions=null;
        Map<String, Object> allParams = getReqParams();
        allParams.put("entity.id",allParams.get("id") );
        allParams.put("entity.type","CMDSASSIGNMENT" );
        JSONObject entityRatingCMDSInfo = _getCMDSEntityRatingInfo(allParams);
        try{
            JSONObject testResult=finalResp.getJSONObject("result");
            Boolean isPublished=testResult.getBoolean("published");
            if(isPublished){
                String brdId=testResult.getJSONArray("metadata")
                        .getJSONObject(0).getString("id");
                request.params.put("brdId", brdId);
                request.params.put("target","TEST_PREVIEW");
                questions=_previewAssignment(null);
            }
        }catch(Exception e){
            Logger.log4j.error("Error is fetching published field of test");
            Logger.log4j.error("Response is: "+finalResp);
        }
        render("QrTests/testPageRouter.html",finalResp,questions,entityRatingCMDSInfo);
    }
    public static void addQuesToAssignment(){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsAssignments/addQuestion",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void removeQuesFromAssignment(){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsAssignments/removeQuestion",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void changeTestResultVisibility(){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsTests/updateTestResultVisibility",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void previewAssignment(){
        recordActivity(ClientUtil.ActivityPages.ASSIGNMENT,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.ASSIGNMENT,request.params.get("assignmentId"));
        request.params.put("testType","ASSIGNMENT");
        request.params.put("id", request.params.get("assignmentId"));
        Map<String, Object> allParams=getReqParams();
        JSONObject finalResp=_getAssignmentDetails(allParams);
        JSONObject questions= _previewAssignment(allParams);
        render("QrTests/previewTest.html",questions,finalResp);
    }
    public static void previewAssignmentQuesns(){
        request.params.put("testType","ASSIGNMENT");
        JSONObject questions= _previewAssignment(null);
        render("QrQuestions/qrQuesns.html",questions);
    }




    //test series
    public static void createTestSeries(){
        recordActivity(ClientUtil.ActivityPages.TEST_SERIES_CREATION,ClientUtil.ActivityAction.OPEN);
        Scope.Params.current().put("isTestSeries","true");
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsTests/createTest",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void testSeriesPage(){
        recordActivity(ClientUtil.ActivityPages.TEST_SERIES,ClientUtil.ActivityAction.OPEN);
        Map<String, Object> allParams=getReqParams();
        JSONObject info=_getTestDetails(allParams);
        JSONObject tests=_getTestsInTestSeries(allParams);
        render(info,tests);
    }

    //return funtions
    protected static JSONObject _getTestDetails(Map<String, Object> allParams){
        Map<String, Object> params = new HashMap<String, Object>();
        for(String key: allParams.keySet()){
            if(key.contains("metadata")){
                Logger.log4j.info("Record Activity Key is : "+key);
            }else{
                params.put(key, allParams.get(key));
            }
        }
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsTests/getTestInfo",params);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    protected static JSONObject _getTestsInTestSeries(Map<String, Object> allParams){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsTests/getTestsInTestSeries",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    protected static JSONObject _previewTest(Map<String, Object> allParams){
        if(allParams==null){
            allParams=getReqParams();
        }
        allParams.put("needCBox", "false");
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsTests/getTestQuestions",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp= ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    protected static JSONObject _previewAssignment(Map<String, Object> allParams){
        if(allParams==null){
            allParams=getReqParams();
        }
        allParams.put("needCBox", "false");
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsAssignments/getAssignmentQuestions",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp= ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    protected static JSONObject _getTests(Map<String, Object> allParams) {
        F.Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsTests/getTests",
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    protected static JSONObject _getAssignmentDetails(Map<String, Object> allParams){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsAssignments/getAssignmentInfo",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    public static void saveAndAddLaterTest(){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsTests/finishTestEditing",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void saveAndAddLaterAssignment(){
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsAssignments/finishAssignmentEditing",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }


    //for direct access
    public static void testDirect(String testId,String orgId){
        recordActivity(ClientUtil.ActivityPages.TEST,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.TEST,testId);
        request.params.put("testType","TEST");
        request.params.put("id", testId);
        request.params.put("testId", testId);
        JSONObject finalResp=ResponseUtil.checkResponse(syncCaller(ClientUtil.CMDS_SERVICE_URL
                +"/cmdsTests/getTestInfo",null));
        String includeName="QrTests/testPage.html";
        JSONObject questions=null;
        Map<String, Object> allParams = getReqParams();
        allParams.put("entity.id",allParams.get("id") );
        allParams.put("entity.type","CMDSTEST" );
        JSONObject entityRatingCMDSInfo = _getCMDSEntityRatingInfo(allParams);
        try{
            JSONObject testResult=finalResp.getJSONObject("result");
            boolean isPublished=testResult.getBoolean("published");
            if(isPublished){
                String brdId=testResult.getJSONArray("metadata")
                        .getJSONObject(0).getString("id");
                request.params.put("brdId", brdId);
                questions=_previewTest(null);
                request.params.put("target","TEST_PREVIEW");
                includeName="QrTests/previewTest.html";
            }
        }catch(Exception e){
            Logger.log4j.error("Error is fetching published field of test");
        }
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html",includeName,finalResp,questions,currentOrgInfo,entityRatingCMDSInfo);
    }
    public static void assignmentDirect(String assignmentId,String orgId){
        recordActivity(ClientUtil.ActivityPages.ASSIGNMENT,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.ASSIGNMENT,assignmentId);
        request.params.put("testType","ASSIGNMENT");
        request.params.put("id", assignmentId);
        request.params.put("assignmentId", assignmentId);
        JSONObject finalResp=ResponseUtil.checkResponse(syncCaller(ClientUtil.CMDS_SERVICE_URL
                +"/cmdsAssignments/getAssignmentInfo",null));
        String includeName="QrTests/testPage.html";
        JSONObject questions=null;
        Map<String, Object> allParams = getReqParams();
        allParams.put("entity.id",allParams.get("id") );
        allParams.put("entity.type","CMDSASSIGNMENT" );
        JSONObject entityRatingCMDSInfo = _getCMDSEntityRatingInfo(allParams);
        try{
            JSONObject testResult=finalResp.getJSONObject("result");
            boolean isPublished=testResult.getBoolean("published");
            if(isPublished){
                String brdId=testResult.getJSONArray("metadata")
                        .getJSONObject(0).getString("id");
                request.params.put("brdId", brdId);
                questions=_previewAssignment(null);
                request.params.put("target","TEST_PREVIEW");
                includeName="QrTests/previewTest.html";
            }
        }catch(Exception e){
            Logger.log4j.error("Error is fetching published field of test");
        }
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html",includeName,finalResp,questions,currentOrgInfo,entityRatingCMDSInfo);
    }
    public static void testSeriesDirect(String testSeriesId,String orgId){
        recordActivity(ClientUtil.ActivityPages.TEST_SERIES,ClientUtil.ActivityAction.OPEN);
        Scope.Params.current().put("testId", testSeriesId);
        Map<String, Object> allParams=getReqParams();
        JSONObject info=_getTestDetails(allParams);
        JSONObject tests=_getTestsInTestSeries(allParams);
        String includeName="QrTests/testSeriesPage.html";
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html",includeName,info,tests,currentOrgInfo);
    }

    public static void addPdfToTestPopup(){
        render();
    }

    public static void addPdfToTest(String orgId) {

        recordActivity(ClientUtil.ActivityPages.DOCUMENT, ClientUtil.ActivityAction.ADD);
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsDocuments/confirm", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void testPasswordPopup(){
        render();
    }

    public static void simplifyBoardNames(){
        Map<String, Object> allParams = getReqParams();
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsTests/simplifyBoardNames", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        render(resp);
    }

    public static void addSimplifiedBoardNames(){
        Map<String, Object> allParams = getReqParams();
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsTests/addSimplifiedBoardNames", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void removeSimplifiedBoardNames(){
        Map<String, Object> allParams = getReqParams();
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsTests/removeSimplifiedBoardNames", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void savePasswordForTest(){
        Map<String, Object> allParams = getReqParams();
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsTests/setPasswordForTest", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }

    public static void enableOrDisablePartialMarks(){
        Map<String, Object> allParams = getReqParams();
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsTests/enableOrDisablePartialMarks", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void enableOrDisableSectionLocking(){
        Map<String, Object> allParams = getReqParams();
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsTests/enableOrDisableSectionLocking", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void enableAutoResumeTest(){
        Map<String, Object> allParams = getReqParams();
        Promise<AbstractQRUIController.JSONResponseWrapper> promise = client(
                ClientUtil.CMDS_SERVICE_URL + "/cmdsTests/enableAutoResumeTest", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }
    public static void autoGenerateTestChaptersListDirect(String orgId, String testId) {
        recordActivity(ClientUtil.ActivityPages.TEST,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.TEST,testId);
        request.params.put("testType","TEST");
        request.params.put("id", testId);
        request.params.put("testId", testId);
        JSONObject finalResp=ResponseUtil.checkResponse(syncCaller(ClientUtil.CMDS_SERVICE_URL
                +"/cmdsTests/getTestInfo",null));
        String includeName="QrTests/autoGenerateTestChaptersList.html";
        JSONObject questions=null;
        try{
            JSONObject testResult=finalResp.getJSONObject("result");
            boolean isPublished=testResult.getBoolean("published");
            if(isPublished){
                String brdId=testResult.getJSONArray("metadata")
                        .getJSONObject(0).getString("id");
                request.params.put("brdId", brdId);
                questions=_previewTest(null);
                request.params.put("target","TEST_PREVIEW");
                includeName="QrTests/previewTest.html";
            }
        }catch(Exception e){
            Logger.log4j.error("Error is fetching published field of test");
        }
        flash.put("ENTRY", "DIRECT");
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html",includeName,finalResp,questions,currentOrgInfo);
    }

    public static void autoGenerateTestChaptersList(){
        recordActivity(ClientUtil.ActivityPages.TEST,ClientUtil.ActivityAction.OPEN);
        request.params.put("testType","TEST");
        JSONObject finalResp=ResponseUtil.checkResponse(syncCaller(ClientUtil.CMDS_SERVICE_URL
                +"/cmdsTests/getTestInfo",null));
        JSONObject questions=null;
        try{
            JSONObject testResult=finalResp.getJSONObject("result");
            Boolean isPublished=testResult.getBoolean("published");
            if(isPublished){
                String brdId=testResult.getJSONArray("metadata")
                        .getJSONObject(0).getString("id");
                request.params.put("brdId", brdId);
                request.params.put("target","TEST_PREVIEW");
                questions=_previewTest(null);
            }
        }catch(Exception e){
            Logger.log4j.error("Error is fetching published field of test");
            Logger.log4j.error("Response is: "+finalResp);
        }
        render("QrTests/autoGenerateTestChaptersList.html",finalResp,questions);
    }

    public static void getQuestionPaper() {
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        Map<String, Object> allParams = getReqParams();
        allParams.put("test.type", "TEST");
        allParams.put("qTypeDistribution", true);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/tests/getTestQuestions", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject response = getJSON(promise);
        Logger.log4j.info("response getQuestionPaper : "+response);
        render(response, currentOrgInfo);
    }

    public static void getDraftPaper() {
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        Map<String, Object> allParams = getReqParams();
        allParams.put("id", allParams.get("testId"));
        JSONObject finalResp=_getTestDetails(allParams);
        JSONObject questions= _previewTest(null);
        render(questions,finalResp,currentOrgInfo);
    }
    public static void getDraftSolutionPaper() {
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        Map<String, Object> allParams = getReqParams();
        allParams.put("id", allParams.get("testId"));
        JSONObject finalResp=_getTestDetails(allParams);
        JSONObject questions= _previewTest(null);
        render(questions,finalResp,currentOrgInfo);
    }

    public static void getSolutionPaper() {
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        Map<String, Object> allParams = getReqParams();
        allParams.put("test.type", "TEST");
        allParams.put("qTypeDistribution", true);
        allParams.put("needSolution",true);
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL + "/tests/getTestQuestions", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject response = getJSON(promise);
        render(response, currentOrgInfo);
    }
}
