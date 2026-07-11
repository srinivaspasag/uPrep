package controllers;

import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.libs.F.Promise;
import play.mvc.Scope;
import play.mvc.With;
import pojos.UserOrg;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;


@With(Security.class)
public class QrQuestions extends AbstractQRUIController {  
    public static void submitQuestion(){
        recordActivity(ClientUtil.ActivityPages.QUESTION,ClientUtil.ActivityAction.ADD);
        QrResources.checkAndSetFolderId();
        Promise<JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsQuestions/addQuestion",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        renderJSON(resp.toString());
    }
    public static void previewQuestion(List<String> answers,List<String> hints,
            List<String> exams,List<String> options,List<String> topics,List<String> subTopics){
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(request.params.get("orgId"));
        render("UIComQuestions/previewQuestion.html",answers,hints,exams,options,topics,subTopics,currentOrgInfo);
    }    
    public static void addedToInfo(){  
        Scope.Params.current().put("target","QUESTION");
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/CmdsTests/getQrProductsForQuestion",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject info = ResponseUtil.checkResponse(getJSON(promise));
        render("Widgets/addedToInfo.html",info);
    }       
    public static void getSubTopics() throws JSONException{
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL + "/CmdsMetadatas/getSubTopics",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject subTopics = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(subTopics.toString());
    }    
    public static void setQuestionStatus(){
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL + "/cmdsQuestions/setQuestionStatus",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }   
    public static void quesSolns(){
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL + "/cmdsQuestions/getSolutions",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject solutions= ResponseUtil.checkResponse(getJSON(promise));        
        render(solutions);
    }
    public static void addSolution(){
        recordActivity(ClientUtil.ActivityPages.QUESTION_SOLUTION,ClientUtil.ActivityAction.ADD);
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL + "/cmdsQuestions/addSolution",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }    
    
    //flagging
    public static void setFlag(){
        Promise<JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL + "/cmdsQuestions/addFlag",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }    
    
    
    //questionPage
    public static void questionPage(String questionId){
        recordActivity(ClientUtil.ActivityPages.QUESTION,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.QUESTION,questionId);
        request.params.put("id", questionId);
        JSONObject question = _getQuestionInfo(null);
        render(question);
    }    
    public static void editQuesPage(String id){
        recordActivity(ClientUtil.ActivityPages.EDIT_QUESTION,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.QUESTION,id);
        JSONObject question = _getQuestionInfo(null);
        render(question);
    }
    public static void editQuesTags(){
        JSONObject question = _getQuestionInfo(null);
        render(question);        
    }
    public static void editQuestion(String id){
        recordActivity(ClientUtil.ActivityPages.QUESTION,ClientUtil.ActivityAction.EDIT,ClientUtil.Entity.QUESTION,id);
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsQuestions/editQuestion",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        try {
            String errorCode = resp.getString("errorCode");
            if("CONTENT_ASSOCIATED_WITH_QUESTION".equals(errorCode)){
                JSONArray contentList = resp.getJSONObject("result").getJSONArray("contentLists");
                play.mvc.Http.Response.current().setHeader("Response-Type", "html");
                render("UIComQuestions/editQuestionResponse.html",contentList);
            }
        } catch (JSONException ex) {
            Logger.log4j.error(ex.getLocalizedMessage());
        }
        play.mvc.Http.Response.current().setHeader("Response-Type", "json");
        renderJSON(resp.toString());
    }    
    
    //challenge page
    public static void challengePage(){
        recordActivity(ClientUtil.ActivityPages.CHALLENGES,ClientUtil.ActivityAction.OPEN);
        JSONObject question = _getChallengeInfo(null);
        render(question);
    }   
    
        
    public static void publishChallenge(){
        recordActivity(ClientUtil.ActivityPages.CHALLENGE,ClientUtil.ActivityAction.PUBLISH);
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsQuestions/publishQuestionAsChallenge",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = ResponseUtil.checkResponse(getJSON(promise));
        renderJSON(resp.toString());
    }      
    public static void addQuesnsToTest(){
        Scope.Params.current().put("type","TEST");
        Scope.Params.current().put("published","false");
        JSONObject tests = QrTests._getTests(null);
        render(tests);
    }      
    public static void addQuesnsToTestList(){
        Scope.Params.current().put("type","TEST");
        Scope.Params.current().put("isPublished","false");
        JSONObject tests = QrTests._getTests(null);
        render(tests);
    }          
    
    
    //question set
    public static void questionSetPage(){
        recordActivity(ClientUtil.ActivityPages.QUESTION_SET,ClientUtil.ActivityAction.OPEN);
        setQuestionSetParams();
        JSONObject questions=_getQuestionSetQuesns(null);
        render(questions);
    }          
    public static void getQuestionSetQuesns(){
        setQuestionSetParams();
        JSONObject questions=_getQuestionSetQuesns(null);
	render("QrQuestions/qrQuesns.html", questions);
    }
    
    
    //return
    protected static JSONObject _getQuestionInfo(Map<String, Object> allParams){  
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CMDS_SERVICE_URL +
                "/cmdsQuestions/getQuestion",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp= ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }  
    protected static JSONObject _getChallengeInfo(Map<String, Object> allParams){  
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CMDS_SERVICE_URL +
                "/cmdsQuestions/getChallengeInfo",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp= ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }    
    protected static JSONObject _getQuestionSetQuesns(Map<String, Object> allParams){  
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CMDS_SERVICE_URL +
                "/cmdsQuestionSets/getQuestions",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp= ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }          
    
    
    public static void questionDirect(String questionId,String orgId){
        recordActivity(ClientUtil.ActivityPages.QUESTION,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.QUESTION,questionId);
        request.params.put("id", questionId);
        JSONObject question = _getQuestionInfo(null);
        String includeName="QrQuestions/questionPage.html";
        flash.put("ENTRY", "DIRECT");        
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html",includeName,question,currentOrgInfo); 
    }  
    public static void checkDuplicatesDirect(String orgId){
        request.params.put("resultType","CREATED");
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsQuestions/getQuestions",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        flash.put("ENTRY", "DIRECT");
        JSONObject questions = ResponseUtil.checkResponse(getJSON(promise));
        String includeName="QrQuestions/checkDuplicates.html";
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        request.params.put("needCBox", "true");
        render("Application/mapper.html",includeName,questions,currentOrgInfo);
    }

    public static void checkDuplicates(){
        request.params.put("resultType","CREATED");
        Promise<AbstractQRUIController.JSONResponseWrapper> promise =
                client(ClientUtil.CMDS_SERVICE_URL +"/cmdsQuestions/getQuestions",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject questions = ResponseUtil.checkResponse(getJSON(promise));
        request.params.put("needCBox", "true");
        render("QrQuestions/checkDuplicates.html",questions);
    }
    public static void questionSetDirect(String questionSetId,String orgId){
        recordActivity(ClientUtil.ActivityPages.QUESTION_SET,ClientUtil.ActivityAction.OPEN);
        request.params.put("start", "0");
        request.params.put("size", "25");
        setQuestionSetParams();
        JSONObject questions=_getQuestionSetQuesns(null);
        String includeName="QrQuestions/questionSetPage.html";
        flash.put("ENTRY", "DIRECT");        
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html",includeName,questions,currentOrgInfo); 
    }    
    public static void challengeDirect(String challengeId,String orgId){
        recordActivity(ClientUtil.ActivityPages.CHALLENGES,ClientUtil.ActivityAction.OPEN);
        JSONObject question = _getChallengeInfo(null);
        String includeName="QrQuestions/challengePage.html";
        flash.put("ENTRY", "DIRECT");        
        UserOrg currentOrgInfo = Widgets.getCurrentOrgInfo(orgId);
        render("Application/mapper.html",includeName,question,currentOrgInfo); 
    }   
    
    
    private static void setQuestionSetParams(){
        request.params.put("questionSet.id",request.params.get("questionSetId"));
        request.params.put("questionSet.type","CMDSQUESTIONSET");      
        request.params.put("needCBox","true");      
    }
    private static JSONObject _getSolutionInfo(String qId){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CMDS_SERVICE_URL +
                "/cmdsQuestions/getSolutions",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp= ResponseUtil.checkResponse(getJSON(promise));
        return resp;
    }
    public static void getSolnAttachments(String qId,String orgId){
        recordActivity(ClientUtil.ActivityPages.QUESTION_SOLUTION,ClientUtil.ActivityAction.OPEN);
        JSONObject solution = _getSolutionInfo(qId);
        render(solution); 
    }
}
