

package controllers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.libs.F.Promise;
import play.mvc.Scope;
import play.mvc.With;
import uicom.util.ClientUtil;
import uicom.util.ResponseUtil;

import com.ning.http.client.AsyncHttpClient;
import com.ning.http.client.Response;
import com.ning.http.multipart.FilePart;

@With(Security.class)
public class Questions extends AbstractUIController {
    public static void home(){
        JSONObject followBoards = Boards._getFollowingBoards(null);
        render(followBoards);
    }
    public static void homeContent(){
        JSONObject followBoards = Boards._getFollowingBoards(null);
        render(followBoards);
    }
    public static void quesItems(){             
        JSONObject quesns= _getQuestions(null);
        render(quesns);
    }
    public static void submitAnswer(String qId){
        try{
            Application.recordActivity(ClientUtil.ActivityPages.QUESTION,ClientUtil.ActivityAction.ATTEMPTED,ClientUtil.Entity.QUESTION,qId);
        }catch(Exception ex){}
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                +"/analytics/recordAttempt",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject submitResp = ResponseUtil.checkResponse(getJSON(promise));       
        if(Scope.Params.current().get("submitPlace").equals("QUESTION_PAGE")){
            render("Questions/userAnsAndGraphBtn.html",submitResp);
        }
        else{            
            Scope.Params.current().put("solStart","0");
            Scope.Params.current().put("solSize","15");
            request.params.put("id",request.params.get("qId"));
            JSONObject ques= _getQuestionInfo(null);
            render("Questions/questionPage.html",ques);            
        }
    }
    public static void quesAddToPL(){
        JSONObject playlistInfo=Playlists._getPlaylistInfo(null);
        render(playlistInfo);
    }
    public static void homeRightSec(){
        Scope.Params.current().put("start",ClientUtil.DEFAULT_FETCH_START);
        Scope.Params.current().put("size","6");
        Scope.Params.current().put("orderBy","avgRating");
        Map<String, Object> allParams=getReqParams();
        JSONObject frndSuggs=Widgets._getFrndSuggs(allParams);
        JSONObject playlists=Widgets._getPopularPLs(allParams);
        JSONObject docs=Widgets._getPopularDocs(allParams);     
        JSONObject tests=Widgets._getPopularTests(allParams);
        render(frndSuggs,playlists,docs,tests);
    }



    //single Question Page
    public static void questionPage(String id){
        try{
            Application.recordActivity(ClientUtil.ActivityPages.QUESTION,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.QUESTION,id);
        }catch(Exception ex){}
        JSONObject ques=_getQuestionInfo(null);
        render(ques);
    }
    public static void addSolution(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                +"/questions/addSolution",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        renderJSON(resp.toString());
    }
    public static void quesSolutions(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                +"/questions/getSolutions",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject solutions = getJSON(promise);
        render(solutions);
    }
    public static void getQuesDiscs(){
        Map<String, Object> allParams = getReqParams();
        JSONObject comms=Doc.getComments(allParams);
        render("Questions/quesDiscs.html",comms);
    }
    public static void similarTags(){
        JSONObject similarTags=_getSimilarTags(null);
        renderJSON(similarTags.toString());
    }
    public static void similarQuesns(){
        JSONObject similarQuesns=_getSimilarQuestions(null);
        render(similarQuesns);
    }
    public static void relatedContent(){
        JSONObject playlists=Widgets._getPopularPLs(null);
        render(playlists);
    }
    public static void quesGraph(){
        JSONObject quesStats=_getQuestionStats(null);
        render(quesStats);
    }
    public static void challengeGraph(){
        JSONObject quesStats=_getQuestionStats(null);
        render("Questions/quesGraph.html",quesStats);
    }




    

    //add question
    public static void addQuestion(){
        render("UIComQuestions/addQuestion.html");
    }
    /*issue #147
     * public static void addDiagrams(){
        Map<String,Object> allParams=getReqParams();
        allParams.put("size",8);
        //JSONObject diagrams = getSearchDiagrams(allParams);
        render();
    }*/
    public static void searchDiagram(){
        Map<String,Object> allParams=getReqParams();
        JSONObject diagrams = getSearchDiagrams(allParams);
        render("Questions/diagItems.html",diagrams);
    }
    public static void submitQuestion(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                +"/questions/addQuestion",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        renderJSON(resp.toString());
    }
    public static void previewQuestion(List<String> answers,List<String> hints,
            List<String> exams,List<String> options,List<String> topics,List<String> subTopics){
        render("UIComQuestions/previewQuestion.html",answers,hints,exams,options,topics,subTopics);
    }     

    
    //utilities functions
    public static void likeQuestion(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL +"/questions/voteUpQuestion",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        renderJSON(resp.toString());
    }
    public static void voteSolution(){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL +"/questions/voteUpSolution",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        renderJSON(resp.toString());
    }
    public static String getQuesUrl(String qid){
        String url="";
        String orgId = Scope.Params.current().get("orgId");
        if(qid.isEmpty()){
            url="/";
        }else{
            if(orgId!=null && !orgId.isEmpty()){
                url = "/organization/"+orgId+"/question/"+qid;
            }
            else{
                url="/question/"+qid;
            }
        }
        return url;
    }





    //return funcs
    public static JSONObject _getQuestions(Map<String, Object> allParams){        
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                +"/questions/getQuestions",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject questions = getJSON(promise);
        return questions;
    }  
    protected static JSONObject _getQuestionInfo(Map<String, Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL
                +"/questions/getQuestionInfo",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        try{
            Application._markEntityView(Scope.Params.current().get("id"),ClientUtil.Entity.QUESTION);
        }catch(Exception err){}
        resp = ResponseUtil.checkResponse(resp);
        return resp;
    }
    public static JSONObject _getQuestionStats(Map<String, Object> allParams){
        Promise<JSONResponseWrapper> promise = 
                client(ClientUtil.CONTENT_SERVICE_URL 
                +"/analytics/getQuestionAnalytics",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        resp = ResponseUtil.checkResponse(resp);
        Logger.log4j.info(resp);
        return resp;
    }
    public static JSONObject _getSimilarTags(Map<String, Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL 
                +"/questions/getSimilarTags",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        return resp;
    }
    public static JSONObject _getSimilarQuestions(Map<String, Object> allParams){
        Promise<JSONResponseWrapper> promise = client(ClientUtil.CONTENT_SERVICE_URL 
                +"/questions/getSimilarQuestions",allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject questions = getJSON(promise);
        return questions;
    }
    protected static JSONObject getSearchDiagrams(Map<String,Object> allParams){
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.DIAGRAM_WEB_SERVICE_URL + "/Diagrams/searchDiagram", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        return resp;
    }
    
    
    
    public static void uploadImage(String qqfile) {
        String UPLOAD_PATH = Play.getFile("").getAbsolutePath() + File.separator + "uploads";
        
        JSONObject jsonResponse = new JSONObject();
        String reqParams = Scope.Params.current().get("params");
        if (request.isNew) {
            FileOutputStream moveTo = null;

            Logger.info("Name of the file %s", qqfile);
            // Another way I used to grab the name of the file
            String filename = request.headers.get("x-file-name").value();

            Logger.info("Absolute on where to send %s", UPLOAD_PATH + File.separator);
            try {

                InputStream data = request.body;
                File up = new File(UPLOAD_PATH);
                if (!up.exists()) {
                    Logger.log4j.info("making directory " + up.getAbsolutePath() + ", result is : " + up.mkdir());
                }
                File inputDoc = new File(UPLOAD_PATH + File.separator + filename);
                moveTo = new FileOutputStream(inputDoc);
                IOUtils.copy(data, moveTo);
                moveTo.close();
                AsyncHttpClient asynClient = new AsyncHttpClient();            
                Logger.log4j.info("===============");
                Logger.log4j.info(reqParams);
                Future<Response> response = asynClient.preparePost(ClientUtil.DIAGRAM_WEB_SERVICE_URL
                        + "/Diagrams/uploadDiagram?userId="+session.get("userId")+"&appId="+Play.configuration.getProperty("auth.appId")
                        +"&secretKey="+Play.configuration.getProperty("auth.secretKey")+"&"+reqParams)
                        .addHeader("Content-Type", "multipart/form-data;boundary=randomBoundaryNotInAnyOfParts")
                        .addBodyPart(new FilePart("uploadedFile", inputDoc))
                        .execute();
                Response r = null;
                try {
                    r = response.get();
                    String rspString = r.getResponseBody();
                    jsonResponse = new JSONObject(rspString);
                    Logger.log4j.info("uploaded image got deleted from local dir : " + inputDoc.delete());
                    Logger.log4j.info("response for playlist image upload" + jsonResponse);

                } catch (InterruptedException e) {
                    Logger.log4j.error(e.getLocalizedMessage());
                } catch (ExecutionException e) {
                    Logger.log4j.error(e.getLocalizedMessage());
                } catch (JSONException e) {
                    Logger.log4j.error(e.getLocalizedMessage());
                } finally {
                    asynClient.close();
                }

            } catch (Exception ex) {

                Logger.log4j.error(ex.getLocalizedMessage());
                renderJSON("{success: false}");
            }

        }

        try {
            String imageHTML = "";
            if (jsonResponse != null && jsonResponse.get("errorCode").toString().isEmpty()) {
                imageHTML= jsonResponse.getJSONObject("result").getString("html");
                renderJSON("{success: true,imageHTML:'"+imageHTML+"'}");
            }            
            else renderJSON("{success: false}");
        } catch (Exception e) {
            Logger.log4j.error("error " + e);
        }
    }


    //url mapping
    public static void questionDirect(@Required String qid){
        try{
            Application.recordActivity(ClientUtil.ActivityPages.QUESTION,ClientUtil.ActivityAction.OPEN,ClientUtil.Entity.QUESTION,qid);
        }catch(Exception ex){}
        JSONObject ques=_getQuestionInfo(null);
        String includeName="Questions/questionPage.html";
        flash.put("ENTRY", "DIRECT");
        render("Application/myPages.html",includeName,ques);
    }
    public static void addQuestionDirect(){
        String includeName="UIComQuestions/addQuestion.html";
        flash.put("ENTRY", "DIRECT");
        render("Application/myPages.html",includeName);
    }    
}





