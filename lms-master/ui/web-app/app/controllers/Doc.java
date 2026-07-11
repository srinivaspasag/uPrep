package controllers;

import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.json.JSONObject;

import play.Logger;
import play.Play;
import play.data.validation.Required;
import play.libs.F.Promise;
import play.mvc.Http;
import play.mvc.Http.Request;
import play.mvc.Scope;
import play.mvc.With;
import uicom.util.ClientUtil;

@With(Security.class)
public class Doc extends AbstractUIController{
    public static void docVideoPage() {
        if(Boolean.parseBoolean(Play.configuration.getProperty("DOC_VIEWER_FEATURE"))){
            JSONObject docInfo =UIComDocuments._getDoc(null);
            render(docInfo);         
        }else{
            error(404, "You do not have permission to view this page.");
        }
    }    
    public static void docComments(){
        Map<String, Object> allParams = getReqParams();
        JSONObject commentsResp = pageComments(allParams);
        render(commentsResp);
    }
    public static void docCommItems(){
        Map<String, Object> allParams = getReqParams();
        JSONObject comments= pageComments(allParams);
        render(comments);
    }

    public static void getReplies() {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL + "/comments/getCommentReplies",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject replies = getJSON(promise);
        renderJSON(replies.toString());
    }

    public static void getCommentInfo() {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL + "/comments/getCommentInfo",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject info = getJSON(promise);
        renderJSON(info.toString());
    }


    public static void getDocPeople(){
        Map<String, Object> allParams=getReqParams();
        JSONObject commons=Widgets._getEntityCommonFollowing(allParams);
        JSONObject allFollowers=Widgets._getEntityFollowers(allParams);
        render("/tags/docPL/people.html",commons,allFollowers);
    }
    public static void docReviews() {
        JSONObject docReviews = getReviews(null);
        String docId = Request.current().params.get("rootId");
        String idName = Request.current().params.get("idName");
        render(docReviews, docId, idName);
    }
    public static void getDocRating() {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL + "/documents/getDocRating",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject rating = getJSON(promise);
        renderJSON(rating.toString());
    }

    public static void docReviewsJson() {
        JSONObject docReviews = getReviews(null);
        renderJSON(docReviews.toString());
    }

    public static void getDocInfo() {
        JSONObject doc = forDocInfo(null);
        renderJSON(doc.toString());
    }

    public static void docInfo() {
        JSONObject docDetails = forDocInfo(null);
        String docId = Request.current().params.get("docId");
        render(docDetails, docId);
    }

    public static void getSimilarDocs() {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL + "/documents/getSimilarDocs",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject docs = getJSON(promise);
        String docRecoType = "SIMILARS";
        render("/Widgets/docRecos.html", docs, docRecoType);
    }

    public static void getCommonPeople() {
        JSONObject common = commonPeople(null);
        renderJSON(common.toString());
    }

    public static void docContents() {
        JSONObject docTocs = UIComDocuments._getTocs(null);
        String docId = request.params.get("docId");
        render(docTocs, docId);
    }    
    public static void docBM() {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL + "/bookmarks/getBookmarks",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject bms = getJSON(promise);
        render(bms);
    }

    public static void addBookmark() {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL + "/bookmarks/addBookmark", null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        renderJSON(jsonResponse.toString());
    }

    public static void removeBookmark() {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL + "/bookmarks/removeBookmark",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        renderJSON(jsonResponse.toString());
    }

    private static final int BBOX_COORDS = 4;

    public static void addHighlight() {
        String[] bBoxes = Request.current().params.getAll("bb[]");
        if (null != bBoxes && bBoxes.length > 0) {
            for (int i = 0; i < bBoxes.length; i++) {
                String[] tokens = StringUtils.splitPreserveAllTokens(bBoxes[i],
                        ",");
                if (null != tokens && tokens.length == BBOX_COORDS) {
                    Scope.Params.current().put(
                            "hocrElements[" + i + "].topLeft.Y",
                            new String[] { tokens[0] });
                    Scope.Params.current().put(
                            "hocrElements[" + i + "].topLeft.X",
                            new String[] { tokens[1] });
                    Scope.Params.current().put(
                            "hocrElements[" + i + "].bottomRight.Y",
                            new String[] { tokens[2] });
                    Scope.Params.current().put(
                            "hocrElements[" + i + "].bottomRight.X",
                            new String[] { tokens[3] });
                }
            }
        }
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL + "/documents/addhighlight",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject addHL = getJSON(promise);
        renderJSON(addHL.toString());
    }

    public static void getTemplateInfo() {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL + "/documents/getTemplateInfo",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject template = getJSON(promise);
        renderJSON(template.toString());
    }

    public static void showVoteUpDetails() {
        JSONObject resp = new JSONObject();
        try {
            resp = resp.put("people", getVoteUpDetails(null).get("result"));
        } catch (Exception e) {
            Logger.log4j.info("problem in extracting followings" + e);
        }
        String entityId = Request.current().params.get("commentId");
        String status = Request.current().params.get("status");
        String heading = "People who upvoted";
        String className = "showVoteUpDetails";
        render("/Widgets/peoplePopup.html", resp, status, entityId, heading,
                className);
    }
    public static void getDocStats() {    
        JSONObject resp = getDocStatInfo(null);
        renderJSON(resp.toString());
    }
    public static void getPublisherVideos() {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.RECOS_WEB_SERVICE_URL
                        + "/recommendations/getMoreDocumentByUser",null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        renderJSON(resp.toString());
    }    
    public static JSONObject getVoteUpDetails(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL
                        + "/comments/getVoteUpUsersDetails", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject voters = getJSON(promise);
        return voters;
    }

    public static JSONObject pageComments(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL
                        + "/comments/getPageHighlightComment", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject comments = getJSON(promise);
        return comments;
    }

    public static JSONObject myPageComments(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL + "/comments/getMyComments",
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject comments = getJSON(promise);
        return comments;
    }

    public static JSONObject pageTags(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL
                        + "/comments/getPagePopularCommentTags", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject tags = getJSON(promise);
        return tags;
    }


    public static JSONObject getDocStatInfo(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL + "/documents/getDocStatInfo", allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject jsonResponse = getJSON(promise);
        return jsonResponse;
    }
    
    


    public static JSONObject getReviews(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL + "/comments/getReview",
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject docReviews = getJSON(promise);
        return docReviews;
    }
    public static JSONObject getComments(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL + "/comments/getComments",
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        return resp;
    }
    public static JSONObject commonPeople(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL + "/documents/getCommonPeople",
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject common = getJSON(promise);
        return common;
    }
    public static JSONObject _getDocuments(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL + "/documents/getDocuments",
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp= getJSON(promise);
        return resp;
    }
    public static JSONObject _getVideos(Map<String, Object> allParams) {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL + "/documents/getDocuments",
                allParams);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp= getJSON(promise);
        return resp;
    }
   public static JSONObject forDocInfo(Map<String, Object> allParams){
                Promise<JSONResponseWrapper> promise = client(ClientUtil.LIB_WEB_SERVICE_URL +"/librarys/getCompleteDocInfo",allParams);
                Logger.log4j.info("BEFORE AWAIT");
                await(promise);
                Logger.log4j.info("AFTER AWAIT");
                JSONObject docDetails = getJSON(promise);
                return docDetails;
   }


    //utilities
    public static void addVideo() {
        Promise<JSONResponseWrapper> promise = client(
                ClientUtil.LIB_WEB_SERVICE_URL + "/documents/addVideo",
                null);
        Logger.log4j.info("BEFORE AWAIT");
        await(promise);
        Logger.log4j.info("AFTER AWAIT");
        JSONObject resp = getJSON(promise);
        renderJSON(resp.toString());
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
    public static void docItems() {
        Scope.Params.current().put("excludeTypes", "Video");
        JSONObject docs=_getDocuments(null);
        if(Scope.Params.current()._contains("target")){
            flash.put("target",Scope.Params.current().get("target"));
        }
        render(docs);
    }
    public static void videoItems() {
        Scope.Params.current().put("includeTypes", "Video");
        JSONObject videos=_getVideos(null);
        if(Scope.Params.current()._contains("target")){
            flash.put("target",Scope.Params.current().get("target"));
        }
        render(videos);
    }
    public static void docDetails() {
        Map<String, Object> allParams = getReqParams();
        JSONObject commentsResp = pageComments(allParams);
        JSONObject tags = pageTags(allParams);
        String docId = Http.Request.current().params.get("docId");
        render(commentsResp, tags, docId);
    }    
    public static void docVideoDirect(@Required String docId){
        if(Boolean.parseBoolean(Play.configuration.getProperty("DOC_VIEWER_FEATURE"))){
            JSONObject docInfo = UIComDocuments._getDoc(null);
            String includeName="Doc/docVideoPage.html";
            flash.put("ENTRY", "DIRECT");
            render("Application/myPages.html",includeName,docInfo);            
        }else{
            error(404, "You do not have permission to view this page.");
        }
    }     
    //url mapping
    public static void uploadDirect(){
        if(Boolean.parseBoolean(Play.configuration.getProperty("UPLOAD_DOC_FEATURE"))){
            Scope.Params.current().put("type","EXAM");
            JSONObject exams=Boards._getBoards(null);
            Scope.Params.current().put("type","COURSE");
            JSONObject subjects=Boards._getBoards(null);  
            flash.put("ENTRY", "DIRECT");
            render(exams,subjects);
        }else{
            error(404, "You do not have permission to view this page.");
        }          
    }  
    
     
    
    
    
    
    @Deprecated
     public static String _getVideoDuration(int secs){
         return "00:10:00";
     }
    @Deprecated
     public static String getVideoDuration(int secs){
         return "";
     }
    @Deprecated
     public static String getTestDuration(int milliseconds){
         return "00:10:00";
     }
    @Deprecated
     public static String getAvgRating(Double avgRating){
        return "5";
     }
    @Deprecated
     public static String getStarsWidth(Double avgRating){
        return "25";
     }    
}
